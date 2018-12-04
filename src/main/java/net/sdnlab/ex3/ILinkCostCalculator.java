package net.sdnlab.ex3;

import java.util.Map;
import java.util.Set;

import org.projectfloodlight.openflow.types.DatapathId;

import net.floodlightcontroller.linkdiscovery.Link;

public interface ILinkCostCalculator {

	/**
	 * 
	 * @param allLinks where we are interested in the weights
	 * @returs a map with a weight for each link
	 */
	Map<Link, Integer> calculateLinkCost(Map<DatapathId, Set<Link>> allLinks);
	
}
