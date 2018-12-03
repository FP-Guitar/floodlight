package net.sdnlab.ex3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.topology.ITopologyService;

public class Task32 implements IFloodlightModule {
	private static Logger logger;
	private IOFSwitchService switchService;
	private IFloodlightProviderService floodlightProviderService;
	private ITopologyService topologyService;
	private Task32StaticFlows staticFlows;
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
		moduleDependencies.add(ITopologyService.class);
	 return moduleDependencies;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		this.logger = LoggerFactory.getLogger(Task32.class);
		this.switchService = context.getServiceImpl(IOFSwitchService.class);
		this.floodlightProviderService = context.getServiceImpl(IFloodlightProviderService.class);
		this.topologyService = context.getServiceImpl(ITopologyService.class);
	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		logger.info("Module Task32 loaded");
		this.staticFlows = new Task32StaticFlows(this.switchService);
	}

}
