package net.sdnlab.ex4.task43;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFFlowDelete;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.U64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.linkdiscovery.Link;
import net.floodlightcontroller.restserver.IRestApiService;
import net.sdnlab.ex2.Task23Worker;
import net.sdnlab.ex4.task43.Subscription.TYPE;


public class Task43 implements IFloodlightModule, ITask43Service, IOFFactoryProvider {

	protected IFloodlightProviderService floodlightProvider;
	protected IRestApiService restApiService;
	protected IOFSwitchService switchService;
	
	protected Task43StaticFlows staticFlows;
	protected Task43ArpHandler arpHandler;
	protected static Logger logger;
	private HashMap<String,Subscription> mySubscriptions;
	private HashMap<DatapathId, ArrayList<SubscriptionLink>> switches;
	private SubscriptionLinkConverter linkConverter;
	private SubscriptionLinkProvider linkProvider;
	private FlowCreator flowCreator;
	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		Collection<Class<? extends IFloodlightService>> services = new ArrayList<Class<? extends IFloodlightService>>();
		services.add(ITask43Service.class);
		return services;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		Map<Class<? extends IFloodlightService>, IFloodlightService> impls = new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
		impls.put(ITask43Service.class, this);
		return impls;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		Collection<Class<? extends IFloodlightService>> deps = new ArrayList<Class<? extends IFloodlightService>>();
		deps.add(IFloodlightProviderService.class);
		deps.add(IRestApiService.class);
		deps.add(IOFSwitchService.class);
		// TODO: add any other required service dependencies

		return deps;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
		restApiService = context.getServiceImpl(IRestApiService.class);
		switchService = context.getServiceImpl(IOFSwitchService.class);
		// TODO: initialize any other required services

		logger = LoggerFactory.getLogger(Task43.class);
		mySubscriptions = new HashMap<String,Subscription>();
		linkConverter = new SubscriptionLinkConverter();
		resetSwitches();
		this.linkProvider = new SubscriptionLinkProvider();
		this.flowCreator = new FlowCreator(this);
	}

	private void resetSwitches() {
		this.switches = new HashMap<DatapathId,ArrayList<SubscriptionLink>>();
		this.switches.put(DatapathId.of("00:00:00:00:00:00:01"), new ArrayList<SubscriptionLink>() );
		this.switches.put(DatapathId.of("00:00:00:00:00:00:02"), new ArrayList<SubscriptionLink>() );
		this.switches.put(DatapathId.of("00:00:00:00:00:00:03"), new ArrayList<SubscriptionLink>() );
	}
	
	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		restApiService.addRestletRoutable(new Task43WebRoutable());
		this.staticFlows = new Task43StaticFlows(this.switchService);
		this.switchService.addOFSwitchListener(this.staticFlows);
		
		// create the arp handler and add the messageListener
		this.arpHandler = new Task43ArpHandler( this.switchService );
		this.floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this.arpHandler);
	}

	@Override
	public String listSubscriptions() {
		logger.info("Listing all subscriptions"); // you may change logging to your liking

		String subscriptionsField = "\"subscriptions\": \"";
		for( Map.Entry<String, Subscription> entry : mySubscriptions.entrySet() ) {
			subscriptionsField = subscriptionsField + entry.getKey() + ", ";
		}
		subscriptionsField = subscriptionsField + "\"";
		String jsonSubscriptions = "{" + subscriptionsField + "}";

		return jsonSubscriptions;
	}

	@Override
	public String addSubscription(Subscription subscription) {
		logger.info("Adding subscription " + subscription.getName()); // you may change logging to your liking
		String status;

		if( mySubscriptions.containsKey(subscription.getName()) ) {
			status = "Dupcliate failure " + subscription.getName();
		} else {
			mySubscriptions.put(subscription.getName(), subscription);
			updateFlows();
			status = "Successfully added new subscription " + subscription.getName();
		}

		
		// status = "Error! Subscription " + name + " already exists";
		return "{\"status\":\"" + status + "\"}";
	}
	private void updateFlows() {
		resetSwitches();
		for( DatapathId id: this.switches.keySet() ) {
			IOFSwitch sw = this.switchService.getSwitch(id);
			if( sw != null ) {
				boolean success = removeSubFlows(sw);
				logger.info("Cleared switch :" + id +" " + success);
			}
		}
		for( Map.Entry<String, Subscription> entry: mySubscriptions.entrySet() ) {
			Subscription sub= entry.getValue();
			List<SubscriptionLink> pathToSubscription = linkProvider.getLinks(sub.getDestinationAddress());
			for( SubscriptionLink link : pathToSubscription ) {
				SubscriptionLink processingLink = new SubscriptionLink(link.getSrc(), link.getSrcPort(),link.getDst(),link.getDstPort(), U64.of(1));
				processingLink.addSubscription(sub);
				this.switches.get(processingLink.getSrc()).add(processingLink);
			}
		}
		for( Map.Entry<DatapathId, ArrayList<SubscriptionLink>> entry : this.switches.entrySet()) {		
			List<FlowMod> mods = flowCreator.createFlowMods(entry.getValue());
			processFlowMods( mods );
		}
	}
	
	private void processFlowMods(List<FlowMod> mods) {
		int priority = 32767;
		U64 cookie = U64.of(0xeadbabe);
		for( FlowMod md : mods) {
			IOFSwitch switchToUse = this.switchService.getSwitch(md.getSwitchId());
			OFFactory factory = switchToUse.getOFFactory();
			// create the flow message
			Match match = md.getMatch();
			ArrayList<OFAction> actionList = md.getActionList();
			OFFlowAdd flowAdd = factory.buildFlowAdd()
					.setCookie(cookie)
					.setPriority(priority--)
					/*setIdleTimeout(idleTimeout) */
					.setMatch(match)
					.setActions(actionList)
					.build();
			boolean success = switchToUse.write(flowAdd);
			logger.debug("Writing Flowmod was " + success);
		}
	}
	
	private boolean removeSubFlows(IOFSwitch sw) {
		U64 cookie = U64.of(0xeadbabe);
		
		OFFactory factoryToUse = sw.getOFFactory();
		
		OFFlowDelete flowDelete = factoryToUse.buildFlowDelete()
				.setCookieMask(cookie)
				.setCookie(cookie)
				.build();
		
		// check if it was  successfully written to the switch
		return sw.write(flowDelete);
	}

	@Override
	public String deleteSubscription(String name) {
		logger.info("Deleting subscription " + name); // you may change logging to your liking
		String status;

		if( mySubscriptions.containsKey(name) ) {
			mySubscriptions.remove(name);
			updateFlows();
			status = "Successfully deleted subscription " + name;
		} else {
			status = "Notfound " + name;
		}

		
		// status = "Error! Subscription " + name + " does not exist";
		return "{\"status\":\"" + status + "\"}";
	}

	@Override
	public OFFactory getFactory(DatapathId switchId) throws IllegalArgumentException {
		return this.switchService.getSwitch(switchId).getOFFactory();
	}

}
