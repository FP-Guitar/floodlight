package net.sdnlab.ex4.task43;

import static net.sdnlab.common.Helper.updateSwitchWithPriority;

import java.util.logging.Logger;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IPv4AddressWithMask;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.sdnlab.ex3.Task3xStaticFlows;

public class Task43StaticFlows extends Task3xStaticFlows {

	public Task43StaticFlows(IOFSwitchService switchService) {
		super(switchService);
		logger.info("Loaded static flows");
	}

	@Override
	protected void fillStaticFlows() {
		logger.info("Loading static flows");
		staticFlows.put(DatapathId.of("00:00:00:00:00:00:00:01"), new SwitchUpdate() {
			@Override
			public boolean update(IOFSwitch switchToUpdate) {
				boolean success = true;
				success  = success && updateSwitchWithPriority( switchToUpdate, IPv4Address.of("10.1.0.1"), 2, 32767);
				
				success  = success && updateSwitchWithPriority( switchToUpdate, IPv4AddressWithMask.of("10.1.0.0/16"), 1, 32766);
				success  = success && updateSwitchWithPriority( switchToUpdate, IPv4Address.of("10.10.10.10"), 3, 32767);
				return success;
			}
		});
		
		staticFlows.put(DatapathId.of("00:00:00:00:00:00:00:02"), new SwitchUpdate() {
			@Override
			public boolean update(IOFSwitch switchToUpdate) {
				boolean success = true;
				success  = success && updateSwitchWithPriority( switchToUpdate, IPv4Address.of("10.1.0.1"), 1, 32767);
				success  = success && updateSwitchWithPriority( switchToUpdate, IPv4Address.of("10.1.1.1"), 3, 32767);
				success  = success && updateSwitchWithPriority( switchToUpdate, IPv4Address.of("10.1.1.3"), 4, 32767);
				success  = success && updateSwitchWithPriority( switchToUpdate, IPv4Address.of("10.1.1.4"), 5, 32767);
				success  = success && updateSwitchWithPriority( switchToUpdate, IPv4Address.of("10.1.1.2"), 2, 32767);			
				return success;
			}
		});
		
		staticFlows.put(DatapathId.of("00:00:00:00:00:00:00:03"), new SwitchUpdate() {
			@Override
			public boolean update(IOFSwitch switchToUpdate) {
				boolean success = true;
				success  = success && updateSwitchWithPriority( switchToUpdate, IPv4AddressWithMask.of("10.0.0.0/8"), 1, 32766);
				success  = success && updateSwitchWithPriority( switchToUpdate, IPv4Address.of("10.1.1.2"), 2, 32767);			
				return success;
			}
		});
	}
}
