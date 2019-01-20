package net.sdnlab.ex3;

import org.projectfloodlight.openflow.types.DatapathId;

// We need to know on which switch is which, host
public class HostInfo {
	public HostInfo(DatapathId switchId, int outPutPort) {
		super();
		this.switchId = switchId;
		this.outPutPort = outPutPort;
	}
	private DatapathId switchId;
	private int outPutPort;
	
	@SuppressWarnings("unused")
	public DatapathId getSwitchId() {
		return switchId;
	}
	public int getOutPutPort() {
		return outPutPort;
	}
}