package net.sdnlab.ex2;

import java.util.ArrayList;

import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActions;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;

import net.floodlightcontroller.core.IOFSwitch;

public abstract class Task23WorkerState {
	abstract public void processState(Task23Worker context);
	
	public void changeState(Task23Worker context, Task23WorkerState state) {
		context.changeState(state);
	}
	
	/**
	 * We have very similar flows (red and blue, so we use a convenient function)
	 * Could easily be augmented to be more generic. 
	 * @param switchname The name of the switch
	 * @param outputPort Where to put the packet
	 * @param flowNumber To which flow does this update refer
	 * @param flowpriority which priority to use
	 * @param context context (state pattern
	 */
	protected void updateSwitch(String switchname, int outputPort, U64 flowNumber, int flowPriority ,Task23Worker context) {
		Task23Worker.logger.info("Installing " +  flowToName(flowNumber)+  " on " +  switchname);
		
		IOFSwitch switchToUpdate = context.switches.get(switchname);
		
		// we want to use a factory, definitely matching to the switch
		// so instead of using generic factory with version, we 
		// always use the factory provided by the switch
		OFFactory factoryToUse = switchToUpdate.getOFFactory();
		
		// which packets to match
		Match match = factoryToUse.buildMatch()
				.setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setExact(MatchField.IPV4_DST,IPv4Address.of("10.0.0.2"))
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
				.setMatch(match)
				.setActions(actionList)
				.build();
		
		// check if it was  successfully written to the switch
		boolean wasSuccess = switchToUpdate.write(flowAdd);
		Task23Worker.logger.info("Success: " +  wasSuccess);
	}
	
	private String flowToName(U64 number ) {
		if( number.equals( Task23Worker.BLUE_FLOW ) )  return "Blue Flow";
		if( number.equals( Task23Worker.RED_FLOW ) ) return "Red Flow";		
		return "unknown";
		
	}
}
