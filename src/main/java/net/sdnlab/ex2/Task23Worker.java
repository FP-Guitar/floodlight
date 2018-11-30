package net.sdnlab.ex2;

import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;


import org.projectfloodlight.openflow.types.U64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.util.SingletonTask;

/**
 * A Task23Worker is  responsible for the
 * correct execution of the tasks given in the
 * Task23 Problem Description. It is an implementation
 * of the state pattern. This pattern was chosen to reflect
 * the different phases, which can be identified:
 *
 * StateInit:
 * 	We have to wait until the controller knows all switches
 *    
 * ::-->StatePushInitialFlows:
 *  Here we install the blue flow, then wait for some time until
 * 
 * ::-->StartControllerRouting:
 *  We install the routing over the controller
 * 
 * ::-->InstallRedFLow:
 *  Install RedFlow, delete BlueFlow
 *  
 * ::-->RemoveControllerRouting
 *   Give traffice back to the network
 * 
 * ::--> END  
 * @author fabian
 *
 */
public class Task23Worker implements Runnable {
	// State which represents different phases of the task
	Task23WorkerState state;
	// Services needed for function
	protected IOFSwitchService switchService;
	protected IFloodlightProviderService floodlightProvider;
	
	// We allow states to reschedule execution of the next task
	// ==> we need a reference to the SingletonTask responsible for us
	// Some states may not reschedule execution and have
	// different semantics for state change (e.g. WaitForBlueTimeout will
	// change state, when all timeouts are received....)
	// A SingletonTask is a task which makes sure, that even on rescheduling
	// using the ExecutorService, only one instance is running at the same time.
	protected SingletonTask ownTask;
	
	// we use a simple name --> switch map for easier access
	// we will store all switches of the topology here
	protected HashMap<String, IOFSwitch> switches = new HashMap<String, IOFSwitch>();
	
	// These cookies identify red and blue flow,
	// this makes deleting the flows very easy
	static final U64 BLUE_FLOW = U64.of(0x01);
	static final U64 RED_FLOW = U64.of(0x02);
	// Wanted to play around with priorities
	protected int bluePriority= 1000;
	protected int redPriority = 900;

	// we use this one for routing over the controller
	protected PacketRouter packetRouter;
	// identify the packet in also by a nice cookie
	static final U64 CONTROLLER_ROUTING = U64.of(0x04);
	
	protected static Logger logger;
	
	public Task23Worker(IOFSwitchService switchService, IFloodlightProviderService floodlightProvider, ScheduledExecutorService executorService) {
		this.switchService = switchService;
		this.floodlightProvider = floodlightProvider;
		
		// well we want to have some logging...
		logger= LoggerFactory.getLogger(Task23.class);
		
		// initate the state machine
		this.state = new Task23WorkerStateInit();
		this.ownTask = new SingletonTask(executorService, this);
		run();
	}

	@Override
	public void run() {
		logger.info("Processing state: {}", state.getClass().getSimpleName());
		state.processState(this);

	}

	protected void changeState(Task23WorkerState state) {
		this.state = state;
	}
	
	

}
