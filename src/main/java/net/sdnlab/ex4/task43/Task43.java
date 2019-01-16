package net.sdnlab.ex4.task43;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.projectfloodlight.openflow.protocol.OFType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.restserver.IRestApiService;
import net.sdnlab.ex3.ARPHandler;
import net.sdnlab.ex3.Task3xStaticFlows;

public class Task43 implements IFloodlightModule, ITask43Service {

	protected IFloodlightProviderService floodlightProvider;
	protected IRestApiService restApiService;
	protected IOFSwitchService switchService;
	
	protected Task43StaticFlows staticFlows;
	protected Task43ArpHandler arpHandler;
	protected static Logger logger;

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
		String jsonSubscriptions = "{}";

		// TODO Implement!

		return jsonSubscriptions;
	}

	@Override
	public String addSubscription(String name /* TODO: add arguments */) {
		logger.info("Adding subscription " + name); // you may change logging to your liking
		String status;

		// TODO Implement!

		status = "Successfully added new subscription " + name;
		// status = "Error! Subscription " + name + " already exists";
		return "{\"status\":\"" + status + "\"}";
	}

	@Override
	public String deleteSubscription(String name) {
		logger.info("Deleting subscription " + name); // you may change logging to your liking
		String status;

		// TODO Implement!

		status = "Successfully deleted subscription " + name;
		// status = "Error! Subscription " + name + " does not exist";
		return "{\"status\":\"" + status + "\"}";
	}

}
