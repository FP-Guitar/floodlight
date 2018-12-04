package net.sdnlab.ex3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.projectfloodlight.openflow.protocol.OFType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.statistics.IStatisticsService;
import net.floodlightcontroller.topology.ITopologyService;

public class Task33 implements IFloodlightModule {
	private Logger log;
	private IOFSwitchService switchService;
	private IFloodlightProviderService floodlightProviderService;
	private ITopologyService topologyService;
	private Task3xStaticFlows staticFlows;
	private ReactiveRoutingModule reactiveRouting;
	private IStatisticsService statisticsService;
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
		moduleDependencies.add(IStatisticsService.class);
		return moduleDependencies;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		this.log = LoggerFactory.getLogger(Task33.class);
		log.info("Module Task33 Init");
		this.switchService = context.getServiceImpl(IOFSwitchService.class);
		this.floodlightProviderService = context.getServiceImpl(IFloodlightProviderService.class);
		this.topologyService = context.getServiceImpl(ITopologyService.class);
		this.statisticsService = context.getServiceImpl(IStatisticsService.class);
		
		this.reactiveRouting = new LoadBalancingRoutingModule(this.topologyService, this.switchService, new BandWithCostCalculator(this.statisticsService) );
	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		log.info("Module Task33 Startup");
		this.staticFlows = new Task3xStaticFlows(this.switchService);
		this.switchService.addOFSwitchListener(this.staticFlows);
		this.statisticsService.collectStatistics(true);
		this.floodlightProviderService.addOFMessageListener(OFType.PACKET_IN, this.reactiveRouting);
	}

}
