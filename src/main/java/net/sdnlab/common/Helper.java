package net.sdnlab.common;

import java.util.ArrayList;

import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActions;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;

import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IPv4AddressWithMask;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;

import net.floodlightcontroller.core.IOFSwitch;



/**
 * Class used for common Function based calls...
 * @author fabian
 *
 */
public class Helper{
/*	public static boolean addIPBaseRouteToSwitch(Switch,outputport,ip_to_match,priority,timeout) {
		// @ true on success false otherwise
		return false;
	}*/ 
	
	public static boolean updateSwitch (IOFSwitch switchToUpdate, IPv4Address ipToMatch,int outputPort) {
		return updateSwitch( switchToUpdate, ipToMatch, outputPort, U64.of(0xcafe), 32700, 0);
	}
	
	public static boolean updateSwitch (IOFSwitch switchToUpdate, IPv4Address ipToMatch,int outputPort,  int idleTimeout ) {
		return updateSwitch(switchToUpdate, ipToMatch, outputPort, U64.of(0xcafe), 32700, idleTimeout );
	}
	
	public static boolean updateSwitchWithPriority(IOFSwitch switchToUpdate, IPv4Address ipToMatch,int outputPort,  int priority) {
		return updateSwitch( switchToUpdate, ipToMatch, outputPort, U64.of(0xbadbeef), priority, 0);
	}
	
	public static boolean updateSwitchWithPriority(IOFSwitch switchToUpdate, IPv4AddressWithMask ipToMatch,int outputPort,  int priority) {
		return updateSwitchWithMask( switchToUpdate, ipToMatch, outputPort, U64.of(0xbadbeef), priority, 0);
	}
	
	public static boolean updateSwitch(IOFSwitch switchToUpdate, IPv4Address ipToMatch,int outputPort, U64 flowNumber, int flowPriority, int idleTimeout) {	
		// we want to use a factory, definitely matching to the switch
		// so instead of using generic factory with version, we 
		// always use the factory provided by the switch
		OFFactory factoryToUse = switchToUpdate.getOFFactory();
		
		// which packets to match
		Match match = factoryToUse.buildMatch()
				.setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setExact(MatchField.IPV4_DST,ipToMatch)
				.build();
		
		
		// create specific actions
		ArrayList<OFAction> actionList = new ArrayList<OFAction>();
		OFActions actions = factoryToUse.actions();
		actionList.add(
				actions.buildOutput()
				.setMaxLen(0xFFffFFff)
				.setPort(OFPort.of(outputPort))
				.build());
		
		// create the flow message
		OFFlowAdd flowAdd = factoryToUse.buildFlowAdd()
				.setCookie(flowNumber)
				.setPriority(flowPriority)
				.setIdleTimeout(idleTimeout)
				.setMatch(match)
				.setActions(actionList)
				.build();
		
		// check if it was  successfully written to the switch
		boolean wasSuccess = switchToUpdate.write(flowAdd);
		return wasSuccess;
	}
	
	public static boolean updateSwitchWithMask(IOFSwitch switchToUpdate, IPv4AddressWithMask ipToMatch,int outputPort, U64 flowNumber, int flowPriority, int idleTimeout) {	
		// we want to use a factory, definitely matching to the switch
		// so instead of using generic factory with version, we 
		// always use the factory provided by the switch
		OFFactory factoryToUse = switchToUpdate.getOFFactory();
		
		// which packets to match
		Match match = factoryToUse.buildMatch()
				.setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setMasked(MatchField.IPV4_DST, ipToMatch)
				.build();
		
		
		// create specific actions
		ArrayList<OFAction> actionList = new ArrayList<OFAction>();
		OFActions actions = factoryToUse.actions();
		actionList.add(
				actions.buildOutput()
				.setMaxLen(0xFFffFFff)
				.setPort(OFPort.of(outputPort))
				.build());
		
		// create the flow message
		OFFlowAdd flowAdd = factoryToUse.buildFlowAdd()
				.setCookie(flowNumber)
				.setPriority(flowPriority)
				.setIdleTimeout(idleTimeout)
				.setMatch(match)
				.setActions(actionList)
				.build();
		
		// check if it was  successfully written to the switch
		boolean wasSuccess = switchToUpdate.write(flowAdd);
		return wasSuccess;
	}
	
	
	
}