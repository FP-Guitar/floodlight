package net.sdnlab.ex4.task43;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;


public class SubscriptionLinkProvider {
	Map<IPv4Address, ArrayList<SubscriptionLink>> links;
	public SubscriptionLinkProvider() {
		links = new HashMap<IPv4Address,ArrayList<SubscriptionLink>>();
		ArrayList<SubscriptionLink> linksToSub = new ArrayList<SubscriptionLink>();
		linksToSub.add( 
			new	SubscriptionLink(DatapathId.of("00:00:00:00:00:00:00:01"), 
					OFPort.of(1), 
					DatapathId.of("00:00:00:00:00:00:00:02"), 
					OFPort.of(1), 
					U64.of(1) ));
		linksToSub.add( 
				new	SubscriptionLink(DatapathId.of("00:00:00:00:00:00:00:02"), 
						OFPort.of(3), 
						DatapathId.of("FF:FF:FF:FF:FF:FF:FF:FF"), 
						OFPort.of(1), 
						U64.of(1) ));
		links.put(IPv4Address.of("10.1.1.1"),linksToSub);
		
		linksToSub = new ArrayList<SubscriptionLink>();
		linksToSub.add( 
			new	SubscriptionLink(DatapathId.of("00:00:00:00:00:00:00:01"), 
					OFPort.of(1), 
					DatapathId.of("00:00:00:00:00:00:00:02"), 
					OFPort.of(1), 
					U64.of(1) ));
		linksToSub.add( 
				new	SubscriptionLink(DatapathId.of("00:00:00:00:00:00:00:02"), 
						OFPort.of(2), 
						DatapathId.of("00:00:00:00:00:00:00:03"), 
						OFPort.of(1), 
						U64.of(1) ));
		linksToSub.add( 
				new	SubscriptionLink(DatapathId.of("00:00:00:00:00:00:00:03"), 
						OFPort.of(2), 
						DatapathId.of("FF:FF:FF:FF:FF:FF:FF:FF"), 
						OFPort.of(1), 
						U64.of(1) ));
		links.put(IPv4Address.of("10.1.1.2"),linksToSub);
		
		linksToSub =new ArrayList<SubscriptionLink>();
		linksToSub.add( 
			new	SubscriptionLink(DatapathId.of("00:00:00:00:00:00:00:01"), 
					OFPort.of(1), 
					DatapathId.of("00:00:00:00:00:00:00:02"), 
					OFPort.of(1), 
					U64.of(1) ));
		linksToSub.add( 
				new	SubscriptionLink(DatapathId.of("00:00:00:00:00:00:00:02"), 
						OFPort.of(4), 
						DatapathId.of("FF:FF:FF:FF:FF:FF:FF:FF"), 
						OFPort.of(1), 
						U64.of(1) ));
		links.put(IPv4Address.of("10.1.1.3"),linksToSub);
		
		linksToSub = new ArrayList<SubscriptionLink>();
		linksToSub.add( 
			new	SubscriptionLink(DatapathId.of("00:00:00:00:00:00:00:01"), 
					OFPort.of(1), 
					DatapathId.of("00:00:00:00:00:00:00:02"), 
					OFPort.of(1), 
					U64.of(1) ));
		linksToSub.add( 
				new	SubscriptionLink(DatapathId.of("00:00:00:00:00:00:00:02"), 
						OFPort.of(5), 
						DatapathId.of("FF:FF:FF:FF:FF:FF:FF:FF"), 
						OFPort.of(1), 
						U64.of(1) ));
		links.put(IPv4Address.of("10.1.1.4"),linksToSub);
	}
	
	List<SubscriptionLink> getLinks( IPv4Address ip) {
		ArrayList<SubscriptionLink> links = new ArrayList<SubscriptionLink>();
		if( this.links.containsKey(ip )) {
			return this.links.get(ip);
		}
		return links;
	}
	
	public boolean knowsTarget( IPv4Address ip) {
		return this.links.containsKey(ip);
	}

}
