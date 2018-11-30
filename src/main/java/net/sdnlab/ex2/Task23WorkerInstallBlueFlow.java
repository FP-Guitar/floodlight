package net.sdnlab.ex2;

import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowDelete;


import net.floodlightcontroller.core.IOFSwitch;


/**
 * Remove red flow completely, install the blue flow
 * @author fabian
 *
 */
public class Task23WorkerInstallBlueFlow extends Task23WorkerState {

	@Override
	public void processState(Task23Worker context) {
		deleteRedFlow(context);
		addBlueFlow(context);
		
		// our next action should be the removal of controller routing
		changeState(context, new Task23WorkerStateRemoveControllerRouting());
		context.run();
	}

	private void addBlueFlow(Task23Worker context) {
		// once again, 'data', prior knowledge, hard coded
		updateSwitch("S1", 2, Task23Worker.BLUE_FLOW,  context.bluePriority, context);
		updateSwitch("S2", 2, Task23Worker.BLUE_FLOW,  context.bluePriority, context);
		updateSwitch("S4", 3, Task23Worker.BLUE_FLOW,  context.bluePriority, context);
		updateSwitch("S6", 3, Task23Worker.BLUE_FLOW,  context.bluePriority, context);
		updateSwitch("S8", 1, Task23Worker.BLUE_FLOW,  context.bluePriority, context);
	}

	
	private void deleteRedFlow(Task23Worker context) {
		// Go through all known switches and delete
		// flows witch belong to the red flow
		for( String switchName : context.switches.keySet() )
		{
			Task23Worker.logger.info("Deleting red flow on switch " + switchName);
			IOFSwitch sw = context.switches.get(switchName);
			OFFactory factoryToUse = sw.getOFFactory();
		
			OFFlowDelete flowDelete = factoryToUse.buildFlowDelete()				
					.setCookie(Task23Worker.RED_FLOW)
					.setCookieMask(Task23Worker.RED_FLOW)
					.build();
			
			// check if it was  successfully written to the switch
			boolean wasSuccess = sw.write(flowDelete);
			Task23Worker.logger.info("Success: " +  wasSuccess);
		}
	}
}
