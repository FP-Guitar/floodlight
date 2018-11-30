package net.sdnlab.ex2;

import java.util.concurrent.TimeUnit;

/**
 * The initial flow is the red flow. When we reached this state,
 * we know we have a complete view on the topology (all switches known).
 * After installing the blue flow, we wait some time until we switch to the blue
 * @author fabian
 *
 */
public class Task23WorkerStatePushInitialFlows extends Task23WorkerState {
	@Override
	public void processState(Task23Worker context) {
		Task23Worker.logger.info("Installing initial flows");
		installRedFlow(context);
		changeState(context, new Task23WorkerStartControllerRouting());
		// we wait 20 seconds, until we change to red flow
		context.ownTask.reschedule(20, TimeUnit.SECONDS);
	}
	
	private void installRedFlow(Task23Worker context) {
		// Hardcoded 'data' values for the red flow
		updateSwitch("S1", 3, Task23Worker.RED_FLOW, context.redPriority, context);
		updateSwitch("S3", 2, Task23Worker.RED_FLOW, context.redPriority, context);
		updateSwitch("S4", 4, Task23Worker.RED_FLOW, context.redPriority, context);
		updateSwitch("S7", 3, Task23Worker.RED_FLOW, context.redPriority, context);
		updateSwitch("S8", 1, Task23Worker.RED_FLOW, context.redPriority, context);
	}
}
