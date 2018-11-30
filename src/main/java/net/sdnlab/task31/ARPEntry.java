package net.sdnlab.task31;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.IPv4Address;
public class ARPEntry {
	private IPv4Address ipAddress;
	private MacAddress macAddress;
	
	public IPv4Address getIpAddress() {
		return ipAddress;
	}
	
	public void setIpAddress(IPv4Address ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	public MacAddress getMacAddress() {
		return macAddress;
	}
	
	public void setMacAddress(MacAddress macAddress) {
		this.macAddress = macAddress;
	}
}
