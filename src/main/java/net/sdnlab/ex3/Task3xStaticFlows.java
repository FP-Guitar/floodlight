package net.sdnlab.ex3;

import static net.sdnlab.common.Helper.updateSwitch;

import java.util.HashMap;
import java.util.Map;

import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.PortChangeType;
import net.floodlightcontroller.core.internal.IOFSwitchService;
/**
 * Push static flows
 * @author fabian
 *
 */
public class Task3xStaticFlows implements IOFSwitchListener {
	// We need to update several switches,
	// use a poor mans variant of a command pattern
	protected abstract class SwitchUpdate {
		// We need to pass a reference, because 
		// we have no reference to the switch, when we update it
		public abstract boolean update(IOFSwitch switchToUpdate);
	}
	
	// store updates to perform
	protected Map<DatapathId,SwitchUpdate> staticFlows;
	
	// We need the switch service
	IOFSwitchService switchService;
	
	protected Logger logger;
	
	public Task3xStaticFlows(IOFSwitchService switchService ) {
		this.logger = LoggerFactory.getLogger(this.getClass());
		staticFlows = new HashMap<DatapathId, SwitchUpdate>();
		
		this.switchService = switchService;
		// now fill it up:
		fillStaticFlows();
	}
	
	protected void fillStaticFlows() {
		// Switch 1.1 with the connected hosts
				staticFlows.put(DatapathId.of("00:00:00:00:00:00:01:01"), new SwitchUpdate() {
					@Override
					public
					boolean update(IOFSwitch switchToUpdate) {
						boolean success = true;
						success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.0.1.1"), 3);
						success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.0.1.2"), 4);
						return success;
					}
				});
				
				// switch 1.2
				staticFlows.put(DatapathId.of("00:00:00:00:00:00:01:02"), new SwitchUpdate() {
					@Override
					public
					boolean update(IOFSwitch switchToUpdate) {
						boolean success = true;
						success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.0.1.3"), 3);
						success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.0.1.4"), 4);
						return success;
					}
				});
				
				// switch 2.1
				staticFlows.put(DatapathId.of("00:00:00:00:00:00:02:01"), new SwitchUpdate() {
					@Override
					public
					boolean update(IOFSwitch switchToUpdate) {
						boolean success = true;
						success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.0.2.1"), 3);
						success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.0.2.2"), 4);
						return success;
					}
				});
				
				// switch 2.2
				staticFlows.put(DatapathId.of("00:00:00:00:00:00:02:02"), new SwitchUpdate() {
					@Override
					public
					boolean update(IOFSwitch switchToUpdate) {
						boolean success = true;
						success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.0.2.3"), 3);
						success  = success && updateSwitch( switchToUpdate, IPv4Address.of("10.0.2.4"), 4);
						return success;
					}
				});
	}
	
	@Override
	public void switchAdded(DatapathId switchId) {

	}

	@Override
	public void switchRemoved(DatapathId switchId) {
		// we don't care
	}

	@Override
	public void switchActivated(DatapathId switchId) {
		// If we have a static flow for this one, push it now
		if( staticFlows.containsKey(switchId) ) {
			logger.info("Pushing static flow to: " + switchId );
			IOFSwitch switchToUse = switchService.getSwitch(switchId);
			boolean success = staticFlows.get(switchId).update(switchToUse);
			logger.info("Success: " + success);		
		} else {
			logger.info("No Flow for {}" , switchId );
		}

	}

	@Override
	public void switchPortChanged(DatapathId switchId, OFPortDesc port, PortChangeType type) {
		// TODO Auto-generated method stub

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
