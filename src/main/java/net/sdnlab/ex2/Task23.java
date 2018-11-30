package net.sdnlab.ex2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.threadpool.IThreadPoolService;

// we don't need the  IOFMessageListener Interface here,
// the packet routing is done by PacketRouter class.
public class Task23 implements IFloodlightModule {
	protected IOFSwitchService switchService;
	protected IFloodlightProviderService floodlightProvider;
	protected IThreadPoolService threadPoolService;
	
	// Task23Worker does the 'work'
	protected Task23Worker worker;
		
	private static Logger logger;

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		 Collection<Class<? extends IFloodlightService>> moduleDependencies = new
				ArrayList<Class<? extends IFloodlightService>>();
		 moduleDependencies.add(IFloodlightProviderService.class);
		 moduleDependencies.add(IOFSwitchService.class);
		 moduleDependencies.add(IThreadPoolService.class);
		return moduleDependencies;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		// get Implementations for used modules
		this.floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
		this.switchService = context.getServiceImpl(IOFSwitchService.class);
		this.threadPoolService = context.getServiceImpl(IThreadPoolService.class);
		// get logger
		logger = LoggerFactory.getLogger(Task23.class);
	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		logger.info("Startup...." );
		ScheduledExecutorService ses = threadPoolService.getScheduledExecutor();
		// The Workerthread does the work for this task
		// it waits until all switches are known, then pushes the initial flows
		// waits again some time and performs the two phased update
        worker = new Task23Worker( switchService, floodlightProvider, ses);
	}



}
