package net.sdnlab.ex4.task43;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.IPv4Address;

import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.sdnlab.ex3.ARPHandler;

public class Task43ArpHandler extends ARPHandler {

	public Task43ArpHandler(IOFSwitchService switchService) {
		super(switchService);
	}
	
	@Override
	protected void fillTopology() {
		// fill topology info by hand
		switches.put(IPv4Address.of("10.1.0.1"), new HostInfo(DatapathId.of("00:00:00:00:00:00:00:01"), 2));
		switches.put(IPv4Address.of("10.10.10.10"), new HostInfo(DatapathId.of("00:00:00:00:00:00:00:01"), 3));
		
		switches.put(IPv4Address.of("10.1.1.1"), new HostInfo(DatapathId.of("00:00:00:00:00:00:00:02"), 3));
		switches.put(IPv4Address.of("10.1.1.3"), new HostInfo(DatapathId.of("00:00:00:00:00:00:00:02"), 4));
		switches.put(IPv4Address.of("10.1.1.4"), new HostInfo(DatapathId.of("00:00:00:00:00:00:00:02"), 5));
		
		switches.put(IPv4Address.of("10.1.1.2"), new HostInfo(DatapathId.of("00:00:00:00:00:00:00:03"), 2));
	}

}
