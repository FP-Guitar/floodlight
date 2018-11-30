package net.sdnlab.ex2;


import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.projectfloodlight.openflow.types.DatapathId;
/**
 * Initial State just waits until link/topology discovery is complete,
 * then schedules the next state
 * @author fabian
 *
 */
public class Task23WorkerStateInit extends Task23WorkerState {
	@Override
	public void processState(Task23Worker context) {
		// We have to wait, until link discovery service completely found all  switches and link
		Set<DatapathId> switchIDs = context.switchService.getAllSwitchDpids();
		if( switchIDs.size() == 8 ) {
			// get the needed switches;
			// use prior knowledge, this is considered as 'data' => no generating function
			context.switches.put("S1", context.switchService.getSwitch(DatapathId.of("00:00:00:00:00:00:00:01")) );
			context.switches.put("S2", context.switchService.getSwitch(DatapathId.of("00:00:00:00:00:00:00:02")) );
			context.switches.put("S3", context.switchService.getSwitch(DatapathId.of("00:00:00:00:00:00:00:03")) );
			context.switches.put("S4", context.switchService.getSwitch(DatapathId.of("00:00:00:00:00:00:00:04")) );
			context.switches.put("S5", context.switchService.getSwitch(DatapathId.of("00:00:00:00:00:00:00:05")) );
			context.switches.put("S6", context.switchService.getSwitch(DatapathId.of("00:00:00:00:00:00:00:06")) );
			context.switches.put("S7", context.switchService.getSwitch(DatapathId.of("00:00:00:00:00:00:00:07")) );
			context.switches.put("S8", context.switchService.getSwitch(DatapathId.of("00:00:00:00:00:00:00:08")) );
			
			changeState(context, new Task23WorkerStatePushInitialFlows());
			// Why is it save to run?
			// We are running this in a SingletonTask, which means, there is only
			// one instance of the thread running at once, so there is no concurrent situation here
			// would be cleaner to use still two functions, but this is no code which ever will be reused.
			context.run();
		} else {
			// have not found all, we wait another waitTime * unit
			int waitTime = 1;
			TimeUnit unit = TimeUnit.SECONDS;
			Task23Worker.logger.info("Do not know all switches yet... try again in {} {}", waitTime, unit.toString());
			context.ownTask.reschedule(waitTime, unit);
		}		
	}

}
