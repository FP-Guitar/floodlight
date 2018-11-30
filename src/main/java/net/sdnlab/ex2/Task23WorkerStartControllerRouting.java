package net.sdnlab.ex2;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;

import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActions;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.OFPort;


import net.floodlightcontroller.core.IOFSwitch;

/**
 * This state initiates the 'Routing over the Controller' for packets
 * from h1 two h2. Unfortunately we cannot alter the ildeTime(soft timeout) value
 * of a flow using the OFFlowMod message, to increase probability that there are
 * no packets still in the net, we wait some time until we go to the next phase, which
 * installs the red flow and removes the blue flow.
 * @author fabian
 *
 */
public class Task23WorkerStartControllerRouting extends Task23WorkerState {

	@Override
	public void processState(Task23Worker context) {
		addRoutingOverController( context );
					
		// give packets in the network some time to reach their destination
		// unlikely that there are some, because we have low traffic and fast net
		// in reality, i think buffers will be still filled to some extend.
		// i really would like to know a better approach to handle this...
		changeState(context, new Task23WorkerInstallBlueFlow());	
		context.ownTask.reschedule(500, TimeUnit.MILLISECONDS);
	}
	
	private void addRoutingOverController(Task23Worker context) {
		Task23Worker.logger.info("Create packet router for controller routing");
		
		// Create the PacketRouter, which actually does the ControllerSide Router Logic
		context.packetRouter = new PacketRouter(context.floodlightProvider, context.switches.get("S1"),context.switches.get("S8"));
		context.packetRouter.startRouting();
		
		// Create correspondingFLowTable on S1
		installPacketInToControllerOnS1(context);	
	}

	
	private void installPacketInToControllerOnS1(Task23Worker context ) {
		Task23Worker.logger.info("Installing PACKET_IN to CONTROLLER on S1");
		// this one should go with highest priority
		// as it should be selected instead of the blue flow
		int flowPriority = 32767;
		IOFSwitch switchToUpdate = context.switches.get("S1");
		
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
				.setPort(OFPort.CONTROLLER)
				.build());
		
		// create the flow message
		OFFlowAdd flowAdd = factoryToUse.buildFlowAdd()
				.setCookie(Task23Worker.CONTROLLER_ROUTING)
				.setPriority(flowPriority)
				.setMatch(match)
				.setActions(actionList)
				.build();
		
		// check if it was  successfully written to the switch
		boolean wasSuccess = switchToUpdate.write(flowAdd);
		Task23Worker.logger.info("Success: " +  wasSuccess);
	}

}
