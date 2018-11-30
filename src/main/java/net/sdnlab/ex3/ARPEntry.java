package net.sdnlab.ex3;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.IPv4Address;
public class ARPEntry {
	private IPv4Address ipAddress;
	private MacAddress macAddress;
	
	/**
	 * use static factory function idiom, as it is used in most other classes
	 * here
	 */
	public static ARPEntry of(IPv4Address ipAddress, MacAddress macAddress) {
		ARPEntry entry = new ARPEntry();
		entry.setIpAddress(ipAddress);
		entry.setMacAddress(macAddress);
		return entry;
	}


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
	@Override 
	public boolean equals(Object a ) {
		if (!(a instanceof ARPEntry)) {
			return false;
		}
		ARPEntry RHS = (ARPEntry) a;
		if( RHS.getIpAddress().equals( this.getIpAddress() )
		    && RHS.getMacAddress().equals(this.getMacAddress() )) {
			return true;
		} else {
			return false;
		}
	}
	
	private ARPEntry () {
	}
		
}
