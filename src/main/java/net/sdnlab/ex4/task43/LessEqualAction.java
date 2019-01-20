package net.sdnlab.ex4.task43;

import net.floodlightcontroller.linkdiscovery.Link;

public class LessEqualAction implements Comparable<LessEqualAction> {
	public enum ACTION {
		ADD_LINK_FOR_NEXT_RANGE,
		REMOVE_LINK_FOR_NEXT_RANGE,
	}
	
	private ACTION action = ACTION.ADD_LINK_FOR_NEXT_RANGE;
	private int upperBound = 0;
	private Link linkToAdd;
	private Subscription relatedSubscription;
	public ACTION getAction() {
		return action;
	}
	public int getUpperBound() {
		return upperBound;
	}
	public Link getLinkToAdd() {
		return linkToAdd;
	}
	public Subscription getRelatedSubscription() {
		return relatedSubscription;
	}
	public LessEqualAction(ACTION action, int upperBound, Link linkToAdd, Subscription relatedSubscription) {
		super();
		this.action = action;
		this.upperBound = upperBound;
		this.linkToAdd = linkToAdd;
		this.relatedSubscription = relatedSubscription;
	}
	@Override
	public int compareTo(LessEqualAction o) {
		return this.upperBound - o.getUpperBound();
	}
	
	
}
