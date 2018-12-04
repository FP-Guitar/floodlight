package net.sdnlab.ex3;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.projectfloodlight.openflow.types.DatapathId;

import net.floodlightcontroller.linkdiscovery.Link;


/**
 * Task 32 assumes equal link cost for every link, so we just assign 1 to every link
 * alternative was to return null, but i personally don't like the idea of "null" values
 * i think of that as a violation of the type system (although very convinient)
 * @author fabian
 *
 */
public class Task32LinkCostCalculator implements ILinkCostCalculator {
	
	private Map<Link, Integer> lastLinkcosts = null;
	@Override
	public Map<Link, Integer> calculateLinkCost(Map<DatapathId, Set<Link>> links) {
		HashMap<Link, Integer> mapToPopulate = new HashMap<Link, Integer>();
		for( DatapathId node : links.keySet() ) {
			Set<Link> linksOfNode = links.get(node);
			for( Link link : linksOfNode) {
				mapToPopulate.put(link, 1);
			}
		}
		//return null;
		this.lastLinkcosts = mapToPopulate;
		return mapToPopulate;
	}
	@Override
	public Map<Link, Integer> getLastLinkCosts() {
		return this.lastLinkcosts;
	}
}
