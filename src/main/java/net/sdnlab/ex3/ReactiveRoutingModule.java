package net.sdnlab.ex3;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.OFPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.linkdiscovery.Link;
import net.floodlightcontroller.core.IListener.Command;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.routing.BroadcastTree;
import net.floodlightcontroller.topology.ITopologyListener;
import net.floodlightcontroller.topology.ITopologyService;
import net.sdnlab.common.dijkstra.Dijkstra;


import static net.sdnlab.common.Helper.updateSwitch;
public class ReactiveRoutingModule implements IOFMessageListener {

	private Logger log;
	// Store Edgeswitches associated with an IP Address in a covinient Map
	private Map<IPv4Address, DatapathId> edgeSwitches;
	private ITopologyService topologyService;
	private IOFSwitchService switchService;
	
	private class SourceDestination {
		// source of the packet in question
		public IPv4Address source;
		// destination of the packet in question
		public IPv4Address destination;
		
		@Override
		public String toString() {
			return "[Source: " + source + ", Destination: " + destination +"]";
		}
	}
	
	public ReactiveRoutingModule(ITopologyService topologyService, IOFSwitchService switchService ) {
		this.topologyService = topologyService;
		this.switchService = switchService;
		log= LoggerFactory.getLogger( ReactiveRoutingModule.class );
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
			
			if( ! checkIfValid( sourceDestination )) {
				// Maybe someone else is interrested....
				return Command.CONTINUE;
			}
			
			computeRoute(sourceDestination);
			injectPacketForHost(sourceDestination.destination, eth);
			return Command.STOP;
		} else {
			return Command.CONTINUE;
		}	
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
	
	private void computeRoute(SourceDestination sourceDestination ) {
		log.info("Computing route: " + sourceDestination);
		DatapathId rootNode = edgeSwitches.get(sourceDestination.source);
		DatapathId destinationNode = edgeSwitches.get(sourceDestination.destination);
		Map<DatapathId, Set<Link>> allLinks = this.topologyService.getAllLinks();
		
		BroadcastTree broadCastTree = Dijkstra.compute(allLinks, rootNode, null, false);
		
		 
		Link nextLink = null;
		DatapathId nextNode = destinationNode;
		while ( ! nextNode.equals(rootNode) ) {
			System.out.println(nextNode);
			nextLink = broadCastTree.getTreeLink(nextNode);
			System.out.println(nextLink);
			nextNode = nextLink.getSrc();
			
			OFPort outputPort = nextLink.getSrcPort();
			IOFSwitch switchToUpdate = this.switchService.getSwitch(nextNode);
			updateSwitch(switchToUpdate, sourceDestination.destination, outputPort.getPortNumber(), 5 );
			
		}
		System.out.println(nextNode);
	}

	private SourceDestination getSourceDestination(IPv4 payload) {	
			SourceDestination sourceDestination = new SourceDestination();
			sourceDestination.source = payload.getSourceAddress();
			sourceDestination.destination = payload.getDestinationAddress();
		return sourceDestination;
	}
	
	private boolean injectPacketForHost(IPv4Address hostAddr, Ethernet packet) {
		// First find out on which switch and port to inject
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

}
