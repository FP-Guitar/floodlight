package net.sdnlab.ex4.task43;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.OFPort;

import net.sdnlab.ex4.task43.Subscription.OPERATOR;
import net.sdnlab.ex4.task43.Subscription.TYPE;

public class FlowCreatorTest {
	private Subscription createDummySubscription(  Subscription.TYPE subType, Subscription.OPERATOR operator,int referenceValue ) {
		int port = 50001;
		IPv4Address ipAddress = IPv4Address.of( "127.0.0.1");
		
		return new Subscription("DummyName", ipAddress, port, subType, operator,referenceValue );
	}
	
	static public class  FactoryProvider implements IOFFactoryProvider {
		@Override
		public OFFactory getFactory(DatapathId switchId) throws IllegalArgumentException {
			OFFactory factory = OFFactories.getFactory(OFVersion.OF_13);
			return factory;
		}
		
	}
	@Test
	public void test42T1() {
		// Mimic situation of 4.2. for T1

		DatapathId srcId = DatapathId.of("00:00:00:00:00:00:00:00");
		SubscriptionLink srcLink = new SubscriptionLink(srcId, OFPort.of(1), DatapathId.of("00:00:00:00:00:00:00:02"), OFPort.of(1), null);

		srcLink.addSubscription(createDummySubscription(TYPE.ENERGY, OPERATOR.GREATER_THAN, 500 ));
		srcLink.addSubscription(createDummySubscription(TYPE.ENERGY, OPERATOR.GREATER_THAN, 100 ));
		List<SubscriptionLink> links = new ArrayList<SubscriptionLink>();
	
		links.add(srcLink);

		
		FlowCreator flowCreator = new FlowCreator( new FactoryProvider() );
		List<FlowMod> flowMods = flowCreator.createFlowMods(links);
		assertEquals(  3,flowMods.size());
		assertEquals( "OFMatchV3Ver13(eth_type=0x800, ip_proto=0x11, ipv4_dst=230.0.0.0/255.255.255.192)", flowMods.get(0).getMatch().toString());
		assertEquals("[]", flowMods.get(0).getActionList().toString());
		assertEquals( "OFMatchV3Ver13(eth_type=0x800, ip_proto=0x11, ipv4_dst=230.0.0.0/255.255.255.0)", flowMods.get(1).getMatch().toString());
		assertEquals("[OFActionOutputVer13(port=1, maxLen=-1)]", flowMods.get(1).getActionList().toString());
		assertEquals( "OFMatchV3Ver13(eth_type=0x800, ip_proto=0x11, ipv4_dst=230.0.0.0/255.255.0.0)", flowMods.get(2).getMatch().toString());
		assertEquals("[OFActionOutputVer13(port=1, maxLen=-1)]", flowMods.get(2).getActionList().toString());

	}
	
	@Test
	public void test42T0less32() {
		// Mimic situation of 4.2. for T1
		DatapathId srcId = DatapathId.of("00:00:00:00:00:00:00:00");
		SubscriptionLink srcLink = new SubscriptionLink(srcId, OFPort.of(1), DatapathId.of("00:00:00:00:00:00:00:02"), OFPort.of(1), null);

		srcLink.addSubscription(createDummySubscription(TYPE.POWER, OPERATOR.LESS_EQUAL, 30 ));
		srcLink.addSubscription(createDummySubscription(TYPE.POWER, OPERATOR.GREATER_THAN, 136 ));
		
		List<SubscriptionLink> links = new ArrayList<SubscriptionLink>();
	
		links.add(srcLink);
		links.add(srcLink);

		
		FlowCreator flowCreator = new FlowCreator( new FactoryProvider() );
		List<FlowMod> flowMods = flowCreator.createFlowMods(links);
		assertEquals(  3,flowMods.size());	
		assertEquals( "OFMatchV3Ver13(eth_type=0x800, ip_proto=0x11, ipv4_dst=230.1.0.0/255.255.255.224)", flowMods.get(0).getMatch().toString());
		assertEquals("[OFActionOutputVer13(port=1, maxLen=-1)]", flowMods.get(0).getActionList().toString());
		assertEquals( "OFMatchV3Ver13(eth_type=0x800, ip_proto=0x11, ipv4_dst=230.1.0.0/255.255.255.128)", flowMods.get(1).getMatch().toString());
		assertEquals("[]", flowMods.get(1).getActionList().toString());
		assertEquals( "OFMatchV3Ver13(eth_type=0x800, ip_proto=0x11, ipv4_dst=230.1.0.0/255.255.0.0)", flowMods.get(2).getMatch().toString());
		assertEquals("[OFActionOutputVer13(port=1, maxLen=-1)]", flowMods.get(2).getActionList().toString());
	}
	

	@Test
	public void test42T1OnS2() {
		// Mimic situation of 4.2. for T1

		DatapathId srcId = DatapathId.of("00:00:00:00:00:00:00:02");
		List<SubscriptionLink> links = new ArrayList<SubscriptionLink>();
		
		SubscriptionLink srcLink = new SubscriptionLink(srcId, OFPort.of(3), DatapathId.of("FF:FF:FF:FF:FF:FF:FF:FF"), OFPort.of(1), null);
		srcLink.addSubscription(createDummySubscription(TYPE.POWER, OPERATOR.GREATER_THAN, 256 ));
		links.add(srcLink);
		srcLink = new SubscriptionLink(srcId, OFPort.of(2), DatapathId.of("00:00:00:00:00:00:00:03"), OFPort.of(2), null);
		srcLink.addSubscription(createDummySubscription(TYPE.POWER, OPERATOR.GREATER_THAN, 64 ));
		
		links.add(srcLink);
		
	
		
		FlowCreator flowCreator = new FlowCreator( new FactoryProvider() );
		List<FlowMod> flowMods = flowCreator.createFlowMods(links);
		assertEquals(  3,flowMods.size());	
		assertEquals( "OFMatchV3Ver13(eth_type=0x800, ip_proto=0x11, ipv4_dst=230.1.0.0/255.255.255.192)", flowMods.get(0).getMatch().toString());
		assertEquals("[]", flowMods.get(0).getActionList().toString());
		assertEquals( "OFMatchV3Ver13(eth_type=0x800, ip_proto=0x11, ipv4_dst=230.1.0.0/255.255.255.0)", flowMods.get(1).getMatch().toString());
		assertEquals("[OFActionOutputVer13(port=2, maxLen=-1)]", flowMods.get(1).getActionList().toString());
		assertEquals( "OFMatchV3Ver13(eth_type=0x800, ip_proto=0x11, ipv4_dst=230.1.0.0/255.255.0.0)", flowMods.get(2).getMatch().toString());
		assertEquals("[OFActionOutputVer13(port=2, maxLen=-1), OFActionSetFieldVer13(field=OFOxmIpv4DstVer13(value=127.0.0.1)), OFActionSetFieldVer13(field=OFOxmUdpDstVer13(value=50001)), OFActionOutputVer13(port=3, maxLen=-1)]", flowMods.get(2).getActionList().toString());
		
		

	}

}
