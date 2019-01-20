package net.sdnlab.ex4.task43;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.floodlightcontroller.linkdiscovery.Link;
import net.sdnlab.ex4.task43.LessEqualAction.ACTION;
import net.sdnlab.ex4.task43.Subscription.OPERATOR;
import net.sdnlab.ex4.task43.Subscription.TYPE;

public class SubscriptionLinkConverter {

	public SubscriptionLinkConverter() {
		// TODO Auto-generated constructor stub
	}
	
	public List<LessEqualAction> createActions(List<SubscriptionLink> links, TYPE subscriptionType ) {
		ArrayList<LessEqualAction> actions = new ArrayList<LessEqualAction>();
		for( SubscriptionLink link : links) {
			actions.addAll(createActions(link, subscriptionType));
		}
		Collections.sort(actions);
		return actions;
	}
	
	
	public List<LessEqualAction> createActions(SubscriptionLink link, TYPE subscriptionType ) {
		ArrayList<LessEqualAction> actions = new ArrayList<LessEqualAction>();
		
		List<Subscription> powerSubscriptions = link.getSubscriptions(subscriptionType);
		
		for( Subscription subscription : powerSubscriptions ) {
			if (subscription.getOperator() == OPERATOR.GREATER_THAN ) {
				 LessEqualAction action = createGreaterThan( subscription, link );
				 actions.add(action);
			}
			if (subscription.getOperator() == OPERATOR.LESS_EQUAL ) {
				 List<LessEqualAction> leActions = createLessEqual( subscription, link );
				 actions.addAll(leActions);
			}
		}	
	
		Collections.sort(actions);
		return actions;
	}


	private List<LessEqualAction> createLessEqual(Subscription subscription, SubscriptionLink subLink) {
		int upperBound = subscription.getReferenceValue();
		int nextUpperPowerOfTwo = 1 << ((int)Math.ceil( (Math.log(upperBound) / Math.log(2) )) );
		Link link= new Link(subLink.getSrc(), subLink.getSrcPort(), subLink.getDst(), subLink.getDstPort(), subLink.getLatency());
		LessEqualAction actionHigher = new LessEqualAction(ACTION.REMOVE_LINK_FOR_NEXT_RANGE, nextUpperPowerOfTwo, link, subscription);
		LessEqualAction actionAtZero = new LessEqualAction(ACTION.ADD_LINK_FOR_NEXT_RANGE, 0, link, subscription);
		List<LessEqualAction> leActions = new ArrayList<LessEqualAction> ();
		leActions.add(actionAtZero);
		leActions.add(actionHigher);
		return leActions;
	}


	private LessEqualAction createGreaterThan(Subscription subscription, SubscriptionLink subLink) {
		int lowerBound = subscription.getReferenceValue();
		int nextLowerPowerOfTwo = 1 << ((int)Math.floor( (Math.log(lowerBound) / Math.log(2) )) );
		Link link= new Link(subLink.getSrc(), subLink.getSrcPort(), subLink.getDst(), subLink.getDstPort(), subLink.getLatency());
		LessEqualAction actionLower = new LessEqualAction(ACTION.ADD_LINK_FOR_NEXT_RANGE, nextLowerPowerOfTwo, link, subscription);
		return actionLower;
	}
}
