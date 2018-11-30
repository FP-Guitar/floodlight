package net.sdnlab.ex2;

import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowDelete;


import net.floodlightcontroller.core.IOFSwitch;
/**
 * Last step, now remove the 'Routing over Controller'
 * and give traffic back to the network
 * @author fabian
 *
 */
public class Task23WorkerStateRemoveControllerRouting extends Task23WorkerState {

	@Override
	public void processState(Task23Worker context) {
		context.packetRouter.stopRouting();
		removePacketInToControllerOnS1(context);
		// we are done here... so we do not trigger any further state processing or change.
		// cleaner: add a finally "done" state...but too lazy
	}
	
	private void removePacketInToControllerOnS1(Task23Worker context) {
		Task23Worker.logger.info("Removing PACKET_IN to CONTROLLER in S1");
		IOFSwitch sw = context.switches.get("S1");
		OFFactory factoryToUse = sw.getOFFactory();
		
		OFFlowDelete flowDelete = factoryToUse.buildFlowDelete()
				.setCookieMask(Task23Worker.CONTROLLER_ROUTING)
				.setCookie(Task23Worker.CONTROLLER_ROUTING)
				.build();
		
		// check if it was  successfully written to the switch
		boolean wasSuccess = sw.write(flowDelete);
		Task23Worker.logger.info("Success: " +  wasSuccess);
	}
}
