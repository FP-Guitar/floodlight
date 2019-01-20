package net.sdnlab.ex3;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.action.OFAction;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.packet.ARP;
import net.floodlightcontroller.packet.Ethernet;
import org.projectfloodlight.openflow.types.ArpOpcode;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
/**
 * ARPHandler does all neccesary actions, to handle arp requests
 * @author fabian
 *
 */

public class ARPHandler implements IOFMessageListener {
	private IOFSwitchService switchService;
	
	protected static Logger logger;
	
	// hardcode topology
	protected Map <IPv4Address, HostInfo> switches = new HashMap<IPv4Address, HostInfo>();
	
	private ARPCache arpCache = new ARPCache();
	public void resetCache() {
		this.arpCache.reset();
	}
	
	public ARPHandler( IOFSwitchService switchService) {
		fillTopology();
		
		this.switchService = switchService;
		
		logger = LoggerFactory.getLogger(this.getClass());
	}
	
	protected void fillTopology() {
		// fill topology info by hand
		switches.put(IPv4Address.of("10.10.1.1"), new HostInfo(DatapathId.of("00:00:00:00:00:00:00:01"), 1));
		switches.put(IPv4Address.of("10.10.1.2"), new HostInfo(DatapathId.of("00:00:00:00:00:00:00:01"), 2));
		switches.put(IPv4Address.of("10.10.1.3"), new HostInfo(DatapathId.of("00:00:00:00:00:00:00:01"), 3));
		
		switches.put(IPv4Address.of("10.10.2.1"), new HostInfo(DatapathId.of("00:00:00:00:00:00:00:02"), 1));
		switches.put(IPv4Address.of("10.10.2.2"), new HostInfo(DatapathId.of("00:00:00:00:00:00:00:02"), 2));
		switches.put(IPv4Address.of("10.10.2.3"), new HostInfo(DatapathId.of("00:00:00:00:00:00:00:02"), 3));
		
		switches.put(IPv4Address.of("10.10.4.1"), new HostInfo(DatapathId.of("00:00:00:00:00:00:00:04"), 1));
		switches.put(IPv4Address.of("10.10.4.2"), new HostInfo(DatapathId.of("00:00:00:00:00:00:00:04"), 2));
		switches.put(IPv4Address.of("10.10.4.3"), new HostInfo(DatapathId.of("00:00:00:00:00:00:00:04"), 3));
	}
	
	
	private HostInfo getSwitch(IPv4Address addr) {
		if( switches.containsKey(addr)) {
			return switches.get(addr);
		} else {
			return null;
		}
	}
	
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
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
		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		// check if we have a packet of the right type
		if( eth.getEtherType().equals(EthType.ARP) ) {				
			ARP payload = (ARP) eth.getPayload();
			ArpOpcode opcode  = payload.getOpCode();
			
			if( opcode == ArpOpcode.REQUEST ) {
				handleARPRequest( payload );
			} else if ( opcode == ArpOpcode.REPLY ) {
				handleARPReply( payload );
			} else {
				logger.info("Unhandeld ARPOpcode of type " + opcode.toString() );
			}
			// no one else should handle arp messages
			return Command.STOP;
		}
		return Command.CONTINUE;
	}
	
	private void handleARPRequest( ARP payload ) {
		logger.info("Handling ARPRequest from " +  payload.getSenderProtocolAddress() );

		ARPEntry requestSender = ARPEntry.of(payload.getSenderProtocolAddress(), payload.getSenderHardwareAddress());
		// check if we want to store the request sender
		if( !arpCache.contains(payload.getSenderProtocolAddress()) ) {
			arpCache.storeEntry(requestSender);
		} 
		
		if( arpCache.contains(payload.getTargetProtocolAddress())) {
			ARPEntry lookedForHost = arpCache.getEntryFromIP(payload.getTargetProtocolAddress());
			ARPEntry destination = requestSender;
			Ethernet packetToInject = createARPPacket( lookedForHost, destination, ArpOpcode.REPLY);
			boolean success = injectPacketForHost(destination.getIpAddress(), packetToInject);
			logger.info("Inject reply::was success " + success );
			logger.debug("Injected Packet: " + packetToInject);
		} else {
			ARPEntry destination = ARPEntry.of( payload.getTargetProtocolAddress(), MacAddress.of("FF:FF:FF:FF:FF:FF"));
			Ethernet packetToInject = createARPPacket(requestSender, destination, ArpOpcode.REQUEST);
			boolean success = injectPacketForHost(destination.getIpAddress(), packetToInject);
			logger.info("Inject request::was success " + success );
			logger.debug("Injected Packet: " + packetToInject);
		}
	}
	
/**
 * 
 * @param source ARPEntry of the source
 * @param destination ARPEntry of the destination addr
 * @param operation The Operation to do
 * @return returns a Ethernet packet ready for injection
 */
	
	private Ethernet createARPPacket(ARPEntry source, ARPEntry destination, ArpOpcode operation ) {	
		// create the arp Payload
		ARP payload = new ARP();
		// common values
		payload.setHardwareType((short)1);
		payload.setProtocolType((short)0x0800);
		payload.setHardwareAddressLength((byte) 6 );
		payload.setProtocolAddressLength((byte)4);
		//operation to do
		payload.setOpCode(operation);
		// setting the important source and destinatino fields
		payload.setSenderHardwareAddress(source.getMacAddress());
		payload.setSenderProtocolAddress(source.getIpAddress());
		payload.setTargetHardwareAddress(destination.getMacAddress());
		payload.setTargetProtocolAddress(destination.getIpAddress());
		
		// fill the corresponding ethernet packet
		Ethernet packet = new Ethernet();
		packet.setEtherType(EthType.ARP);
		packet.setPayload(payload);
		packet.setDestinationMACAddress(destination.getMacAddress());
		packet.setSourceMACAddress(source.getMacAddress());
		
		return packet;
	}
	
	private boolean injectPacketForHost(IPv4Address hostAddr, Ethernet packet) {
		// First find out on which switch and port to inject
		HostInfo info  = getSwitch(hostAddr);
		if( info == null) {
			logger.info("Could not find a switch for host" + hostAddr);
			return false;
		}
		// get the attached switch and outputport
		IOFSwitch switchToUse = this.switchService.getSwitch(info.getSwitchId());
		int outputPort = info.getOutPutPort();
		
		//now we can serialize the packet again, and send it to the second switch
		byte[] serializedPacket= packet.serialize();
		OFFactory switchFactory = switchToUse.getOFFactory();
		
		// still ugly, but i don't care
		List<OFAction> actions = Collections.singletonList(
					(OFAction)switchFactory.actions().output(OFPort.of(outputPort), 0xffFFffFF)
				);
		OFPacketOut po = switchFactory.buildPacketOut()
				.setData(serializedPacket)
				.setActions(actions)
				.setInPort(OFPort.CONTROLLER)
				.build();
		// finally write to output
		return switchToUse.write(po);
	}
	
	private void handleARPReply( ARP payload  ) {
		logger.info("Handling ARPReply from " +  payload.getSenderProtocolAddress() );
		if( ! arpCache.contains(payload.getSenderProtocolAddress() )) {
			arpCache.storeEntry( ARPEntry.of(payload.getSenderProtocolAddress(), payload.getSenderHardwareAddress()));
		} 
		if( arpCache.contains( payload.getTargetProtocolAddress() )) {
			ARPEntry lookedForHost = arpCache.getEntryFromIP(payload.getSenderProtocolAddress());
			ARPEntry destination = arpCache.getEntryFromIP(payload.getTargetProtocolAddress() );
			Ethernet packetToInject = createARPPacket( lookedForHost, destination, ArpOpcode.REPLY);
			boolean success = injectPacketForHost(destination.getIpAddress(), packetToInject);
			logger.info("Inject reply::was success " + success );
			logger.debug("Injected Packet: " + packetToInject);
		} else {
			logger.error("Receiving an ARP Reply to an Request we have not Seen:\n" + payload);
		}
	}
}
