package net.sdnlab.ex4.task43;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;

import net.sdnlab.ex4.task43.Subscription.OPERATOR;
import net.sdnlab.ex4.task43.Subscription.TYPE;

public class SubscriptionLinkConverterTest {
	private Subscription createDummySubscription(  Subscription.TYPE subType, Subscription.OPERATOR operator,int referenceValue ) {
		int port = 50001;
		IPv4Address ipAddress = IPv4Address.of( "127.0.0.1");
		
		return new Subscription("DummyName", ipAddress, port, subType, operator,referenceValue );
	}
	@Test
	public void test() {
		SubscriptionLink link = new SubscriptionLink(DatapathId.of("00:00:00:00:00:00:00:01"), OFPort.of(1), DatapathId.of("00:00:00:00:00:00:00:02"), OFPort.of(1), U64.of(1));
		link.addSubscription(createDummySubscription(TYPE.POWER,OPERATOR.GREATER_THAN, 500));
		link.addSubscription(createDummySubscription(TYPE.POWER,OPERATOR.GREATER_THAN,100));
		
		SubscriptionLinkConverter converter = new SubscriptionLinkConverter();
		List<LessEqualAction> actions = converter.createActions(link, TYPE.POWER);
		
		assertEquals(2, actions.size());
		
		LessEqualAction le1 = actions.get(0);
		LessEqualAction le2 = actions.get(1);
		
		assertEquals( 64, le1.getUpperBound() );
		assertEquals( LessEqualAction.ACTION.ADD_LINK_FOR_NEXT_RANGE, le1.getAction());
		assertEquals( DatapathId.of("00:00:00:00:00:00:00:02"),le1.getLinkToAdd().getDst());
		assertEquals( 256, le2.getUpperBound() );
		
	}
	
	@Test
	public void test2() {
		SubscriptionLink link = new SubscriptionLink(DatapathId.of("00:00:00:00:00:00:00:01"), OFPort.of(1), DatapathId.of("00:00:00:00:00:00:00:02"), OFPort.of(2), U64.of(1));
		link.addSubscription(createDummySubscription(TYPE.ENERGY,OPERATOR.GREATER_THAN, 128));
		link.addSubscription(createDummySubscription(TYPE.ENERGY,OPERATOR.LESS_EQUAL,30));
		
		SubscriptionLinkConverter converter = new SubscriptionLinkConverter();
		List<LessEqualAction> actions = converter.createActions(link, TYPE.ENERGY);
		
		assertEquals(3, actions.size());
		
		LessEqualAction le1 = actions.get(0);
		LessEqualAction le2 = actions.get(1);
		LessEqualAction le3 = actions.get(2);
		
		assertEquals( 0, le1.getUpperBound() );
		assertEquals( LessEqualAction.ACTION.ADD_LINK_FOR_NEXT_RANGE, le1.getAction());
		assertEquals( 30, le2.getUpperBound() );
		assertEquals( LessEqualAction.ACTION.REMOVE_LINK_FOR_NEXT_RANGE, le2.getAction());
		assertEquals( 128, le3.getUpperBound() );
		assertEquals( LessEqualAction.ACTION.ADD_LINK_FOR_NEXT_RANGE, le3.getAction());	
		assertEquals( "Link [src=00:00:00:00:00:00:00:01 outPort=1, dst=00:00:00:00:00:00:00:02, inPort=2, latency=1]",le1.getLinkToAdd().toString());
	}
	
	@Test
	public void test_situation_at_s2() {
		SubscriptionLink link5 = new SubscriptionLink(DatapathId.of("00:00:00:00:00:00:00:02"), OFPort.of(5), DatapathId.of("FF:00:00:00:00:00:00:01"), OFPort.of(4), U64.of(1));
		link5.addSubscription(createDummySubscription(TYPE.ENERGY,OPERATOR.GREATER_THAN, 136));
		
		SubscriptionLink link4 = new SubscriptionLink(DatapathId.of("00:00:00:00:00:00:00:02"), OFPort.of(4), DatapathId.of("FF:00:00:00:00:00:00:02"), OFPort.of(5), U64.of(1));
		link4.addSubscription(createDummySubscription(TYPE.ENERGY,OPERATOR.LESS_EQUAL,30));
		
		SubscriptionLinkConverter converter = new SubscriptionLinkConverter();
		ArrayList<SubscriptionLink> links = new ArrayList<SubscriptionLink>();
		links.add(link4);
		links.add(link5);
		List<LessEqualAction> actions = converter.createActions(links, TYPE.ENERGY);
		
		assertEquals(3, actions.size());
		
		LessEqualAction le1 = actions.get(0);
		LessEqualAction le2 = actions.get(1);
		LessEqualAction le3 = actions.get(2);
		
		assertEquals( 0, le1.getUpperBound() );
		
		assertEquals( LessEqualAction.ACTION.ADD_LINK_FOR_NEXT_RANGE, le1.getAction());
		assertEquals( "Link [src=00:00:00:00:00:00:00:02 outPort=4, dst=ff:00:00:00:00:00:00:02, inPort=5, latency=1]",le1.getLinkToAdd().toString());
		assertEquals( 30, le2.getUpperBound() );
		assertEquals( LessEqualAction.ACTION.REMOVE_LINK_FOR_NEXT_RANGE, le2.getAction());
		assertEquals( 128, le3.getUpperBound() );
		assertEquals( LessEqualAction.ACTION.ADD_LINK_FOR_NEXT_RANGE, le3.getAction());	
		assertEquals( "Link [src=00:00:00:00:00:00:00:02 outPort=5, dst=ff:00:00:00:00:00:00:01, inPort=4, latency=1]",le3.getLinkToAdd().toString());
	}

}
