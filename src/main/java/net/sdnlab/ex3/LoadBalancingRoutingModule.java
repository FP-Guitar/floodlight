package net.sdnlab.ex3;

import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.U64;

import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.topology.ITopologyService;
import org.projectfloodlight.openflow.protocol.match.Match.Builder;
/**
 * If possible do routing based on IPv4 and transportlayerinformation TCP/UDP.
 * With a LinkCostCalculator this allows load balancing
 * @author fabian
 *
 */
public class LoadBalancingRoutingModule extends ReactiveRoutingModule {

	public LoadBalancingRoutingModule(ITopologyService topologyService, IOFSwitchService switchService,
			ILinkCostCalculator linkCostCalculator) {
		super( topologyService, switchService, linkCostCalculator, 20, U64.of(0xAFFE),32700);
		
	}
	
	
	// overwrite for different match
	@Override
	protected Match createMatch(OFFactory factoryToUse, SourceDestination sourceDestination) {
		Builder matchBuilder = factoryToUse.buildMatch()
					.setExact(MatchField.ETH_TYPE, EthType.IPv4)
					.setExact(MatchField.IPV4_DST, sourceDestination.destination);
					
		short protocolNumber = sourceDestination.protocol.getIpProtocolNumber();
		matchBuilder.setExact(MatchField.IP_PROTO,sourceDestination.protocol);
		
		if( protocolNumber  == 0x06 ) {
			matchBuilder.setExact(MatchField.TCP_SRC, sourceDestination.sourcePort)
			.setExact(MatchField.TCP_DST, sourceDestination.destinationPort);
		} else if ( protocolNumber == 0x11 ) {
			matchBuilder.setExact(MatchField.UDP_SRC, sourceDestination.sourcePort)
			.setExact(MatchField.UDP_DST, sourceDestination.destinationPort);
		}
		
		return matchBuilder.build();
	}

}
