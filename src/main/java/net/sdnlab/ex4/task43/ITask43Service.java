package net.sdnlab.ex4.task43;

import net.floodlightcontroller.core.module.IFloodlightService;

public interface ITask43Service extends IFloodlightService {
	public String listSubscriptions();
	public String addSubscription(Subscription subscriptions);
	public String deleteSubscription(String name);
}
