package net.sdnlab.ex3;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.U64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.linkdiscovery.Link;
import net.floodlightcontroller.statistics.IStatisticsService;
import net.floodlightcontroller.statistics.SwitchPortBandwidth;

public class BandWithCostCalculator implements ILinkCostCalculator {
	private IStatisticsService statService;
	private Logger log;
	public BandWithCostCalculator(IStatisticsService statService) {
		this.statService = statService;
		this.log = LoggerFactory.getLogger(this.getClass());
	}
	private Map<Link, Integer> lastLinkCosts = null;
	@Override
	public Map<Link, Integer> calculateLinkCost(Map<DatapathId, Set<Link>> allLinks) {
		HashMap<Link, Integer> mapToPopulate = new HashMap<Link, Integer>();
		
		
		for( DatapathId node : allLinks.keySet() ) {
			for( Link link : allLinks.get(node) ) {
				if( node.equals(link.getSrc())) {
					SwitchPortBandwidth spbw = statService.getBandwidthConsumption(link.getSrc(), link.getSrcPort());
					if( spbw == null ) {
						log.error("No statistics available, abort");
						//actually this null is because dijkstra works with null
						// null is a evil thing, but i am clued to the given code
						return null;
					}
					// unfortunately, dijkstra where this interface comes from
					// works with Integer, so we have suffer some pain
					// to make sure we have reasonable values
					U64 weight = spbw.getBitsPerSecondTx() ;
					
					long value =  weight.getValue();
					int intValue = 0;
					if ( value > (long) Integer.MAX_VALUE ) {
						log.info("overflow, set link cost to max");
						value = Integer.MAX_VALUE;
					} else if( value < 0) {
						log.info("underflow, set link cost to 0");
						value = 0;
					}
					intValue  = (int) (value);
					mapToPopulate.put(link, intValue );
				}			
			}
		}
		lastLinkCosts = mapToPopulate;
		return mapToPopulate;
	}

	@Override
	public Map<Link, Integer> getLastLinkCosts() {
		return lastLinkCosts;
	}

}
