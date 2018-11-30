package net.sdnlab.ex2;

import java.util.Collections;
import java.util.List;

import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.OFPort;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;

/**
 * Perform controller based routing to 10.0.0.2
 * only little changes needed to make this generic...
 * @author fabian
 *
 */
public class PacketRouter implements IOFMessageListener {
	IFloodlightProviderService floodlightProvider;
	IOFSwitch  switch1;
	IOFSwitch  switch2;
	OFFactory switch2Factory;
	
	IPv4Address destinationIP = IPv4Address.of("10.0.0.2");
	/**
	 * Construct a PacketRouter which routes packets from switch1 to switch2
	 * if they match the hardcoded destinationIP.
	 * @param floodlightProvider IFloodlightProviderService implementation
	 * @param switch1 The switch where to expect packets
	 * @param switch2 The switch where to put the packets
	 */
	PacketRouter( IFloodlightProviderService floodlightProvider, IOFSwitch switch1, IOFSwitch switch2 ) {
		this.floodlightProvider = floodlightProvider;
		this.switch1 = switch1;
		this.switch2 = switch2;
		// unlikely this will change
		switch2Factory = switch2.getOFFactory();
	}
		
	@Override
	public String getName() {
		return PacketRouter.class.getName();
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
 		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		return false;
	}
	public void startRouting() {
		this.floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
	}
	public void stopRouting() {
		this.floodlightProvider.removeOFMessageListener(OFType.PACKET_IN, this);
	}

	@Override
	public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		// check if we are interested
		if( sw.equals(switch1)) {
			Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
			// check if we have a packet of the right type
			if( eth.getEtherType().equals(EthType.IPv4)) {				
				IPv4 payload = (IPv4) eth.getPayload();
				IPv4Address destination = payload.getDestinationAddress();
				// check if we have a packet of the right destination
				if( destination.equals(destination)) {
					//now we can serialize the packet again, and send it to the second switch
					byte[] serializedPacket= eth.serialize();
					// still ugly, but i don't care
					List<OFAction> actions = Collections.singletonList(
								(OFAction)switch2Factory.actions().output(OFPort.of(1), 0xffFFffFF)
							);
					OFPacketOut po = switch2Factory.buildPacketOut()
							.setData(serializedPacket)
							.setActions(actions)
							.setInPort(OFPort.CONTROLLER)
							.build();
					// finally write to output
					switch2.write(po);
				}
			}
			// we handled this one, no one else should be interested (for now)
			return Command.STOP;
		}
		// if not interested, give other modules the chance to handle it 
		return Command.CONTINUE;
	}

}
