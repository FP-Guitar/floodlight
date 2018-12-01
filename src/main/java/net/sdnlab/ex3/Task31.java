package net.sdnlab.ex3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.IPv4Address;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.PortChangeType;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.threadpool.IThreadPoolService;

import static net.sdnlab.common.Helper.updateSwitch;

public class Task31 implements IFloodlightModule, IOFSwitchListener {
	private static Logger logger;
	private IOFSwitchService switchService;
	private IFloodlightProviderService floodlightProviderService;
	private ARPHandler arpHandler;
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
		logger = LoggerFactory.getLogger(Task31.class);
		logger.info("Loaded Module");
		this.switchService = context.getServiceImpl(IOFSwitchService.class);
		this.floodlightProviderService = context.getServiceImpl(IFloodlightProviderService.class);
		this.arpHandler = new ARPHandler( this.switchService );
	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		this.switchService.addOFSwitchListener(this);
		this.floodlightProviderService.addOFMessageListener(OFType.PACKET_IN, this.arpHandler);
	}

	@Override
	public void switchAdded(DatapathId switchId) {

		
	}

	@Override
	public void switchRemoved(DatapathId switchId) {
		logger.info( "Switch removed" + switchId);
		
	}

	@Override
	public void switchActivated(DatapathId switchId) {
		logger.info( "Switch activated installing config..." + switchId);
		IOFSwitch switchToUpdate  = this.switchService.getSwitch(switchId);
		boolean success= true;
		if( switchId.equals(DatapathId.of("00:00:00:00:00:00:00:01")) ) {
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.1.1"), 1);
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.1.2"), 2);
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.1.3"), 3);
			
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.2.1"), 4);
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.2.1"), 4);
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.2.1"), 4);
						
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.4.1"), 4);
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.4.1"), 4);
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.4.1"), 4);
		} else if ( switchId.equals(DatapathId.of("00:00:00:00:00:00:00:02") ) ) {
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.1.1"), 4);
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.1.2"), 4);
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.1.3"), 4);
			
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.2.1"), 1);
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.2.1"), 2);
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.2.1"), 3);
					
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.4.1"), 4);
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.4.1"), 4);
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.4.1"), 4);
		} else if ( switchId.equals(DatapathId.of("00:00:00:00:00:00:00:03") ) ){
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.1.1"), 1);
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.1.2"), 1);
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.1.3"), 1);
			
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.2.1"), 1);
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.2.1"), 1);
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.2.1"), 1);
					
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.4.1"), 2);
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.4.1"), 2);
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.4.1"), 2);
		} else if ( switchId.equals(DatapathId.of("00:00:00:00:00:00:00:04") ) ){
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.1.1"), 4);
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.1.2"), 4);
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.1.3"), 4);
			
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.2.1"), 4);
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.2.1"), 4);
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.2.1"), 4);
					
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.4.1"), 1);
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.4.1"), 2);
			success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.10.4.1"), 3);
		}
		logger.info("Installing config was success: " + success);
	}

	@Override
	public void switchPortChanged(DatapathId switchId, OFPortDesc port, PortChangeType type) {
		logger.info( "Switchport activated" + switchId);
		
	}

	@Override
	public void switchChanged(DatapathId switchId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void switchDeactivated(DatapathId switchId) {
		// TODO Auto-generated method stub
		
	}

}
