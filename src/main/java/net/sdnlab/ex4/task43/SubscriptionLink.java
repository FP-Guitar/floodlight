package net.sdnlab.ex4.task43;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;

import net.floodlightcontroller.linkdiscovery.Link;
import net.sdnlab.ex4.task43.Subscription.OPERATOR;
import net.sdnlab.ex4.task43.Subscription.TYPE;

public class SubscriptionLink extends Link {
	private Subscription.TYPE filterType;;
	
	private Map<Subscription.TYPE,ArrayList<Subscription>> subscriptions;
	
	public SubscriptionLink(DatapathId srcId, OFPort srcPort, DatapathId dstId, OFPort dstPort, U64 latency) {
		super(srcId, srcPort, dstId, dstPort, latency);
		this.subscriptions = new HashMap<Subscription.TYPE, ArrayList<Subscription>>();
		this.subscriptions.put(Subscription.TYPE.ENERGY,new ArrayList<Subscription>());
		this.subscriptions.put(Subscription.TYPE.POWER,new ArrayList<Subscription>());
	}

	/**
	 * Add a subscription, which messages must go along this link
	 * @param sub A subscription, which messages must go through this link
	 */
	
	public void addSubscription(Subscription sub ) {
		filterType = sub.getFilterType();
		this.subscriptions.get(filterType).add(sub);
	}
	
	public List<Subscription> getSubscriptions( TYPE filterType ) {
		return this.subscriptions.get(filterType);
	}
	


}
