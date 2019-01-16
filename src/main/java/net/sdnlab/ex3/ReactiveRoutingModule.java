package net.sdnlab.ex3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActions;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TableId;
import org.projectfloodlight.openflow.types.TransportPort;
import org.projectfloodlight.openflow.types.U64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IListener.Command;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.linkdiscovery.Link;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.TCP;
import net.floodlightcontroller.packet.UDP;
import net.floodlightcontroller.routing.BroadcastTree;
import net.floodlightcontroller.topology.ITopologyService;
import net.sdnlab.common.dijkstra.Dijkstra;



public class ReactiveRoutingModule implements IOFMessageListener {

	protected Logger log;
	// Store Edgeswitches associated with an IP Address in a covinient Map
	private Map<IPv4Address, DatapathId> edgeSwitches;
	private ITopologyService topologyService;
	private IOFSwitchService switchService;
	private ILinkCostCalculator linkCostCalculator;
	private int flowTimeOutInSeconds;
	private long flowTimeOutInMillis;
	private U64 flowCookie;
	private int flowPriority;
	private long flowCounter = 0;
	private HashMap<SourceDestination, Long> lastSeenFlow;
	/**
	 * Class for representing a end-to-end connection
	 * actually information is aequivalent to information typically
	 * used for socket identification
	 * @author fabian
	 *
	 */
	protected class SourceDestination {
		// source of the packet in question
		public IPv4Address source;
		public TransportPort sourcePort;
		// destination of the packet in question
		public IPv4Address destination;
		public TransportPort destinationPort;
		
		public IpProtocol protocol;
			
		@Override
		public String toString() {
			String strSourcePort = "noPort";
			String strDestinationPort ="noPort";
			strSourcePort = sourcePort == null ? "noPort" : sourcePort.toString();
			strDestinationPort = destinationPort == null ? "noPort" : destinationPort.toString();
			return "[Source: " + source +":"+strSourcePort + ", Destination: " + destination +":"+strDestinationPort+"]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((destination == null) ? 0 : destination.hashCode());
			result = prime * result + ((destinationPort == null) ? 0 : destinationPort.hashCode());
			result = prime * result + ((protocol == null) ? 0 : protocol.hashCode());
			result = prime * result + ((source == null) ? 0 : source.hashCode());
			result = prime * result + ((sourcePort == null) ? 0 : sourcePort.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SourceDestination other = (SourceDestination) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (destination == null) {
				if (other.destination != null)
					return false;
			} else if (!destination.equals(other.destination))
				return false;
			if (destinationPort == null) {
				if (other.destinationPort != null)
					return false;
			} else if (!destinationPort.equals(other.destinationPort))
				return false;
			if (protocol == null) {
				if (other.protocol != null)
					return false;
			} else if (!protocol.equals(other.protocol))
				return false;
			if (source == null) {
				if (other.source != null)
					return false;
			} else if (!source.equals(other.source))
				return false;
			if (sourcePort == null) {
				if (other.sourcePort != null)
					return false;
			} else if (!sourcePort.equals(other.sourcePort))
				return false;
			return true;
		}

		private ReactiveRoutingModule getOuterType() {
			return ReactiveRoutingModule.this;
		}
		
	}
	
	public ReactiveRoutingModule(ITopologyService topologyService, IOFSwitchService switchService, ILinkCostCalculator linkCostCalculator) {
		this ( topologyService, switchService, linkCostCalculator, 20, U64.of(0xcafe),32700);
	}
	
	public ReactiveRoutingModule(ITopologyService topologyService, IOFSwitchService switchService, ILinkCostCalculator linkCostCalculator, int flowTimeOutInSeconds, U64 flowCookie, int flowPriority ) {
		this.topologyService = topologyService;
		this.switchService = switchService;
		this.linkCostCalculator = linkCostCalculator;
		this.flowTimeOutInSeconds = flowTimeOutInSeconds;
		this.flowTimeOutInMillis = flowTimeOutInSeconds * 1000;
		this.flowCookie = flowCookie;
		this.flowPriority = flowPriority;
		this.lastSeenFlow = new HashMap<SourceDestination, Long>();
		log= LoggerFactory.getLogger( this.getClass() );
		// Fill map of edge Switches (leaf nodes of tree)
		edgeSwitches = new HashMap<IPv4Address, DatapathId>();
		DatapathId switch11 = DatapathId.of("00:00:00:00:00:00:01:01");
		DatapathId switch12 = DatapathId.of("00:00:00:00:00:00:01:02");
		DatapathId switch21 = DatapathId.of("00:00:00:00:00:00:02:01");
		DatapathId switch22 = DatapathId.of("00:00:00:00:00:00:02:02");
		
		// Switch 1.1
		edgeSwitches.put(IPv4Address.of("10.0.1.1"), switch11 );
		edgeSwitches.put(IPv4Address.of("10.0.1.2"), switch11 );
		
		// Switch 1.2
		edgeSwitches.put(IPv4Address.of("10.0.1.3"), switch12 );
		edgeSwitches.put(IPv4Address.of("10.0.1.4"), switch12 );
		
		// Switch 2.1
		edgeSwitches.put(IPv4Address.of("10.0.2.1"), switch21 );
		edgeSwitches.put(IPv4Address.of("10.0.2.2"), switch21 );
		
		// Switch 2.2
		edgeSwitches.put(IPv4Address.of("10.0.2.3"), switch22 );
		edgeSwitches.put(IPv4Address.of("10.0.2.4"), switch22 );	
	}
	
	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		// check if we have a packet of the right type	
		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
	
		if( eth.getEtherType().equals(EthType.IPv4)) {
			IPv4 payload = (IPv4) eth.getPayload();
			SourceDestination sourceDestination = getSourceDestination(payload);
			
			if( ! checkIfValid( sourceDestination ) ) {
				// Maybe someone else is interrested....
				return Command.CONTINUE;
			}
			if( checkIfFlowAlreadyInstalled(sourceDestination) ) {
				// flow is installed, but we have some packet in
				// because of a fast sending process
				log.debug("already installed" + sourceDestination);
				injectPacketForHost(sourceDestination.destination, eth);
				return Command.CONTINUE;
			}
			
			BroadcastTree route = computeRoute(sourceDestination);
			
			boolean routeIsInstalled = installRoute(route, sourceDestination);
			boolean packetIsInjected = false;
			
			if( routeIsInstalled ) {
				packetIsInjected = injectPacketForHost(sourceDestination.destination, eth);
			}
			
			if( routeIsInstalled && packetIsInjected ) {
				log.info("#{} successfully handled {}",  ++flowCounter, sourceDestination );
				lastSeenFlow.put(sourceDestination, System.currentTimeMillis() );
			} else {
				log.error("failed to handle " + sourceDestination + "route:" + routeIsInstalled +" packetinjected:" + packetIsInjected);
			}
		}
		return Command.CONTINUE;
	}
	// sometimes when a process sends a lot of packets, like iperf
	// we receive more than on packet on packet in before we have installed the flow
	// this is to handle this
	private boolean checkIfFlowAlreadyInstalled(SourceDestination sourceDestination) {
		if( lastSeenFlow.containsKey(sourceDestination) ) {			
			long currentTime = System.currentTimeMillis();
			long lastSeenTime = lastSeenFlow.get(sourceDestination);
			long diff = currentTime - lastSeenTime;
			// The half of the flow timeout seems to be a reasonable
			// timeout... 
			if( diff < (flowTimeOutInMillis / 2 )  ) {
				return true;
			}
		
		}
		return false;
	}
	
	 
	
	// we only want to compute routes for edge links we know
	private boolean checkIfValid(SourceDestination sourceDestination ) {
		if (edgeSwitches.containsKey(sourceDestination.destination)  
				&& edgeSwitches.containsKey(sourceDestination.source) ){
			return true;
		} else {
			log.info("Got invalid SourceDestination pair: " + sourceDestination );
			return false;
		}
	}
	// use dijkstra implementation from floodlight...
	private BroadcastTree computeRoute(SourceDestination sourceDestination ) {
		log.info("Computing route: " + sourceDestination);
		DatapathId rootNode = edgeSwitches.get(sourceDestination.source);
		
		Map<DatapathId, Set<Link>> allLinks = this.topologyService.getAllLinks();
		Map<Link, Integer> linkCost = this.linkCostCalculator.calculateLinkCost(allLinks);
		
		BroadcastTree broadCastTree = Dijkstra.compute(allLinks, rootNode, linkCost, false);
		
		return  broadCastTree;
	}
	
	private boolean installRoute(BroadcastTree broadCastTree, SourceDestination sourceDestination) {
		Link nextLink = null;
		DatapathId rootNode = edgeSwitches.get(sourceDestination.source);
		DatapathId nextNode = edgeSwitches.get(sourceDestination.destination);
		
		// Will hold the complete route as string at the end
		String installedRoute = "["+nextNode+"]";
		
		// Tracks, if a flow as succesfully written on a switch
		boolean success = true;
		
		// as long as we have not reached the rootNode, and
		// as long as we have no write error when pushing a flow
		// we install the flow backwards.. from destination to source...
		int aggregatedLinkCost = 0;
		while ( ! nextNode.equals(rootNode) && success == true ) {
			nextLink = broadCastTree.getTreeLink(nextNode);
			int linkCost = getLinkCost(nextLink);
			aggregatedLinkCost+=linkCost;
			// This can be null, if the controller not has fully negotiated the roles with all switches, or the
			// topology information was incomplete when computing dijkstra
			if( nextLink == null) {
				log.error("Changing topology, Controller not fully initialized?, got null link from node, aborting route installing");
				return false;
			}
			
			// This can be null, if the controller not has fully negotiated the roles with all switches, or the
			// topology information was incomplete when computing dijkstra
			nextNode = nextLink.getSrc();
			if( nextNode == null) {
				log.error("Changing topology, Controller not fully initialized?, got link to a null node, aborting route installing");
				return false;
			}
			OFPort outputPort = nextLink.getSrcPort();
			IOFSwitch switchToUpdate = this.switchService.getSwitch(nextNode);
			
			success &= updateSwitch(switchToUpdate, sourceDestination, outputPort );
			// collect the route information for debug output
			installedRoute =   "["+nextNode +"]\n" + "--" + linkCost +"--" + installedRoute;
		}
		if( success ) {
			log.info("Installed Route: {}", sourceDestination);  
			log.info("Path: \n{}", installedRoute);
			log.info("Total Route Cost:{}",  aggregatedLinkCost);
			return true;
		} else {
			log.info("Failed to install Route, got so far: " + installedRoute + " for " + sourceDestination);
			return false;
		}
	}
	/**
	 * potentially unsafe, no handling of corrupted network traffic
	 * e.g. iperf3 udp packets seem to be not deseriliazable...
	 * @param payload
	 * @return SourceDestination for a given IPv4 payload...
	 */
	private SourceDestination getSourceDestination(IPv4 payload) {	
			SourceDestination sourceDestination = new SourceDestination();
			IpProtocol protocol = payload.getProtocol();
			
			sourceDestination.source = payload.getSourceAddress();
			sourceDestination.destination = payload.getDestinationAddress();
			sourceDestination.protocol = protocol;
			
			if( protocol.getIpProtocolNumber() == 0x06 ) {
				TCP packet = (TCP) payload.getPayload();
				sourceDestination.destinationPort = packet.getDestinationPort();
				sourceDestination.sourcePort = packet.getSourcePort();	
			} else if (protocol.getIpProtocolNumber() == 0x11 ) {
				UDP packet = (UDP) payload.getPayload();
				sourceDestination.destinationPort = packet.getDestinationPort();
				sourceDestination.sourcePort = packet.getSourcePort();
			} else {
				log.error("Unsupported Protocol :" + protocol);
			}
			
		return sourceDestination;
	}
	
	private boolean injectPacketForHost(IPv4Address hostAddr, Ethernet packet) {
		// First find out on which switch and port to inject
		log.debug("Injecting Packet to {} ", hostAddr);
		DatapathId id  = edgeSwitches.get(hostAddr );
		if( id == null) {
			log.info("Could not find a switch for host" + hostAddr);
			return false;
		}
		// get the attached switch and outputport
		IOFSwitch switchToUse = this.switchService.getSwitch(id);

		
		//now we can serialize the packet again, and send it to the second switch
		byte[] serializedPacket= packet.serialize();
		OFFactory switchFactory = switchToUse.getOFFactory();
		

		List<OFAction> actions = Collections.singletonList(
							(OFAction)switchFactory.actions().output(OFPort.TABLE, 0xffFFffFF) );
		OFPacketOut po = switchFactory.buildPacketOut()
				.setData(serializedPacket)
				.setInPort(OFPort.CONTROLLER)
				.setActions(actions)
				.build();
		// finally write to output
		return switchToUse.write(po);
	}
	
	// We use some template pattern here, also the poor mans version,
	// don't tell my colleagues ;)
	private boolean updateSwitch(IOFSwitch switchToUpdate, SourceDestination sourceDestination, OFPort outPutPort) {	
	 	// we want to use a factory, definitely matching to the switch
		// so instead of using generic factory with version, we 
		// always use the factory provided by the switch
		OFFactory factoryToUse = switchToUpdate.getOFFactory();
		
		// create part of flow message
		Match match = createMatch(factoryToUse, sourceDestination);
		ArrayList<OFAction> actionList = createActionList(factoryToUse, sourceDestination, outPutPort);
				
		// create the flow message
		OFFlowAdd flowAdd = factoryToUse.buildFlowAdd()
				.setCookie(flowCookie)
				.setPriority(flowPriority)
				.setIdleTimeout(flowTimeOutInSeconds)
				.setMatch(match)
				.setActions(actionList)
				.build();
		
		// check if it was  successfully written to the switch
		boolean wasSuccess = switchToUpdate.write(flowAdd);
		return wasSuccess;
	}
	
	// overwrite for different match
	protected Match createMatch(OFFactory factoryToUse, SourceDestination sourceDestination) {
		// which packets to match
		Match match = factoryToUse.buildMatch()
				.setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setExact(MatchField.IPV4_DST, sourceDestination.destination)
				.build();
		return match;
	}
	
	// overwrite for different actions...
	protected ArrayList<OFAction> createActionList(OFFactory factoryToUse, SourceDestination sourceDestination, OFPort outputPort) {
		ArrayList<OFAction> actionList = new ArrayList<OFAction>();
		OFActions actions = factoryToUse.actions();
		actionList.add(
				actions.buildOutput()
				.setMaxLen(0xFFffFFff)
				.setPort(outputPort)
				.build());
		return actionList;
	}
	
	private int getLinkCost(Link link) {
		Map <Link, Integer> costs = this.linkCostCalculator.getLastLinkCosts();
		if (  costs != null ) {
			return costs.get(link);
		} else {
			return 1;
		}
	}
}
