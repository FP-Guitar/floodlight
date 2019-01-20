package net.sdnlab.ex4.task43;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFMatchBmap;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActions;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.oxm.OFOxms;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IPv4AddressWithMask;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TransportPort;

import net.floodlightcontroller.linkdiscovery.Link;
import net.sdnlab.ex4.task43.LessEqualAction.ACTION;
import net.sdnlab.ex4.task43.Subscription.OPERATOR;
import net.sdnlab.ex4.task43.Subscription.TYPE;

public class FlowCreator {
	private IOFFactoryProvider factoryProvider;
	SubscriptionLinkConverter linkConverter;
	public FlowCreator(IOFFactoryProvider factoryProvider) {
		super();
		this.factoryProvider = factoryProvider;
		this.linkConverter = new SubscriptionLinkConverter();
	}
	
	public List<FlowMod> createFlowMods( List<SubscriptionLink> links ) {
		ArrayList<FlowMod> flowMods = new ArrayList<FlowMod>();
		List<LessEqualAction> actions = linkConverter.createActions(links, TYPE.ENERGY);
		List<FlowMod> energyFlowMods = processActions( actions, TYPE.ENERGY );
		List<LessEqualAction> powerActions = linkConverter.createActions(links, TYPE.POWER);
		List<FlowMod> powerFlowMods = processActions( powerActions, TYPE.POWER );
		flowMods.addAll(energyFlowMods);
		flowMods.addAll(powerFlowMods);
		
		return flowMods;
	}
	private class LinkSubTuple {
		public LinkSubTuple(Link link, Subscription sub) {
			super();
			this.link = link;
			this.sub = sub;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((link == null) ? 0 : link.hashCode());
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
			LinkSubTuple other = (LinkSubTuple) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (link == null) {
				if (other.link != null)
					return false;
			} else if (!link.equals(other.link))
				return false;
			return true;
		}
		public Link link;
		public Subscription sub;
		private FlowCreator getOuterType() {
			return FlowCreator.this;
		}
	}
	
	private List<FlowMod> processActions(List<LessEqualAction> actions, TYPE energy) {
		ArrayList<FlowMod> flowMods = new ArrayList<FlowMod>();
		Set<LinkSubTuple> actualLinks = new HashSet<LinkSubTuple>();
		int upperBound = 0;

		DatapathId switchId = null;
		for( LessEqualAction action: actions ) {
			ACTION leACTION = action.getAction();
			Link link = action.getLinkToAdd();
			switchId = link.getSrc();
			int newUpperBound = action.getUpperBound();
			
			if( (newUpperBound != upperBound )) {
				FlowMod newMod = createFlowMod( newUpperBound, actualLinks,energy, link.getSrc() );
				flowMods.add(newMod);
				upperBound = newUpperBound;
			}
			if( leACTION == ACTION.ADD_LINK_FOR_NEXT_RANGE ) {
				actualLinks.add(new LinkSubTuple( link, action.getRelatedSubscription()));
			} else {			
				actualLinks.remove(new LinkSubTuple(link, action.getRelatedSubscription())); 		
			}
			
		}
		if( switchId != null ) {
			FlowMod finalMod = createFlowMod( 1<<16, actualLinks,energy, switchId );
			flowMods.add(finalMod);
		}
		
		return flowMods;
	}

	private FlowMod createFlowMod(int upperBound, Set<LinkSubTuple> actualLinks, TYPE filterType, DatapathId switchId ) {
		String lessThenMask = createLessThenMask( upperBound );
		String Match = "230." + TYPE.toIpCode( filterType ) + ".0.0" +lessThenMask;
		
		OFFactory factoryToUse = this.factoryProvider.getFactory( switchId);
		OFOxms oxms = factoryToUse.oxms();
		IPv4AddressWithMask matchIp = IPv4AddressWithMask.of(Match); 
		Match match = factoryToUse.buildMatch().setMasked(MatchField.IPV4_DST, matchIp)
			.setExact(MatchField.ETH_TYPE, EthType.IPv4)
			.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
			.build();
		
		OFActions actions = factoryToUse.actions();
		ArrayList<OFAction> actionListNormal = new ArrayList<OFAction>();
		ArrayList<OFAction> actionListEndpoints = new ArrayList<OFAction>();
		for( LinkSubTuple linkTuple : actualLinks ) {
			Link link = linkTuple.link;
			// add necessary rewrite for final destination
			if( link.getDst().equals( DatapathId.of("FF:FF:FF:FF:FF:FF:FF:FF") ) ) {
				actionListEndpoints.add(actions.setField(oxms.ipv4Dst(linkTuple.sub.getDestinationAddress())));
				actionListEndpoints.add(actions.setField(oxms.udpDst(TransportPort.of(linkTuple.sub.getPort()))));
				actionListEndpoints.add(
						actions.buildOutput()
						.setMaxLen(0xFFffFFff)
						.setPort(link.getSrcPort())
						.build() );
			} else {
				actionListNormal.add(
						actions.buildOutput()
						.setMaxLen(0xFFffFFff)
						.setPort(link.getSrcPort())
						.build() );
			}

		}
		actionListNormal.addAll(actionListEndpoints);
		FlowMod flow = new FlowMod(switchId,match, actionListNormal, factoryToUse);
		return flow;
	}

	private List<FlowMod> createGreaterThen(List<SubscriptionLink> links, Subscription sub) {
		ArrayList<FlowMod> mods = new ArrayList<FlowMod>();
		FlowMod removeLowerValues = createLessThan( links, sub);
		mods.add(removeLowerValues);
		// remove all actions, as we want to ddrop all of them
		removeLowerValues.getActionList().clear();
		String greaterThanMask = "/16";
		String Match = "230." + TYPE.toIpCode( sub.getFilterType() ) + ".0.0" +greaterThanMask;
		DatapathId switchId = links.get(0).getSrc();
		OFFactory factoryToUse = this.factoryProvider.getFactory( switchId);
		OFOxms oxms = factoryToUse.oxms();
		IPv4AddressWithMask matchIp = IPv4AddressWithMask.of(Match); 
		Match match = factoryToUse.buildMatch().setMasked(MatchField.IPV4_DST, matchIp)
			.setExact(MatchField.ETH_TYPE, EthType.IPv4)
			.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
			.build();
		
		OFActions actions = factoryToUse.actions();
		ArrayList<OFAction> actionList = new ArrayList<OFAction>();
		for( SubscriptionLink link : links ) {
			// add necessary rewrite for final destination
			if( link.getDst().equals( DatapathId.of("FF:FF:FF:FF:FF:FF:FF:FF")) ) {
				actionList.add(actions.setField(oxms.ipv4Dst(sub.getDestinationAddress())));
				actionList.add(actions.setField(oxms.udpDst(TransportPort.of(sub.getPort()))));
			}
			actionList.add(
					actions.buildOutput()
					.setMaxLen(0xFFffFFff)
					.setPort(link.getSrcPort())
					.build() );
		}
		FlowMod flow = new FlowMod(switchId,match, actionList, factoryToUse);
		mods.add(flow);
		return mods;
	}

	private FlowMod createLessThan( List<SubscriptionLink> links, Subscription sub ) {
		String lessThenMask = createLessThenMask( sub.getReferenceValue() );
		String Match = "230." + TYPE.toIpCode( sub.getFilterType() ) + ".0.0" +lessThenMask;
		DatapathId switchId = links.get(0).getSrc();
		OFFactory factoryToUse = this.factoryProvider.getFactory( switchId);
		OFOxms oxms = factoryToUse.oxms();
		IPv4AddressWithMask matchIp = IPv4AddressWithMask.of(Match); 
		Match match = factoryToUse.buildMatch().setMasked(MatchField.IPV4_DST, matchIp)
			.setExact(MatchField.ETH_TYPE, EthType.IPv4)
			.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
			.build();
		
		OFActions actions = factoryToUse.actions();
		ArrayList<OFAction> actionList = new ArrayList<OFAction>();
		for( SubscriptionLink link : links ) {
			// add necessary rewrite for final destination
			if( link.getDst().equals( DatapathId.of("FF:FF:FF:FF:FF:FF:FF:FF") ) ) {
				actionList.add(actions.setField(oxms.ipv4Dst(sub.getDestinationAddress())));
				actionList.add(actions.setField(oxms.udpDst(TransportPort.of(sub.getPort()))));
			}
			actionList.add(
					actions.buildOutput()
					.setMaxLen(0xFFffFFff)
					.setPort(link.getSrcPort())
					.build() );
		}
		FlowMod flow = new FlowMod(switchId,match, actionList, factoryToUse);
		return flow;
	}
	
	private String createLessThenMask(int value ) {
		// why has java no integer math library?!!!! no ld? this is painfull
		// we need always one bit more to have exactily  < power of two
		int numberOfMaskedBits = 16 - (int)(Math.log(value) / Math.log(2));
		numberOfMaskedBits = numberOfMaskedBits + 16; // we always need the first 16 bits
		return "/" + String.valueOf(numberOfMaskedBits);
	}

	
	

}
