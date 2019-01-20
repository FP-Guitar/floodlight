package net.sdnlab.ex4.task43;

import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.types.DatapathId;
/**
 *  Use a wrapperinterface arround the switchService
 *  mainly used for unit testing purpose
 */
public interface IOFFactoryProvider {
	/**
	 * 
	 * @param switchId The SwitchID for which teh factoy should be obtained
	 * @return The factory to used
	 * @throws IllegalArgumentException If the datapathId does not belong to a valid switch
	 */
	public OFFactory getFactory( DatapathId switchId ) throws IllegalArgumentException;
}
