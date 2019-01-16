package net.sdnlab.ex3;
import java.util.HashMap;
import java.util.Map;

import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.MacAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sdnlab.ex3.ARPEntry;
public class ARPCache {
		private static Logger logger;
		private boolean printAlwaysFullCache = false;
		public ARPCache() {
			logger = LoggerFactory.getLogger(ARPCache.class.getName());
		}
		// Use Map instead of Arrays of entries for faster search
		private static Map<IPv4Address, MacAddress> arpEntries = new HashMap<IPv4Address,MacAddress>();
		/**
		 * Get an ArpEntry to a given IPAddress
		 * @param ipAddress
		 * @return ARPEntry
		 */
		public synchronized ARPEntry getEntryFromIP( IPv4Address ipAddress ) {
			MacAddress match = arpEntries.get(ipAddress);
			 
			if( match == null ) {
				// This is considered an programming error ==> unchecked expection
				throw new IllegalArgumentException("You should always check, if ARPEntry exists, before calling getEntryFromIP");
			}
			ARPEntry entry  = ARPEntry.of(ipAddress, arpEntries.get(ipAddress));
			return entry;
		}
		
		/**
		 * @param ipAddress
		 * @return True if the entry is in the cash, false if not
		 */
		public synchronized boolean contains( IPv4Address ipAddress ) {
			return arpEntries.containsKey(ipAddress);
		}
		/**
		 * Stores an ARPEntry with given information. A second store
		 * will overwrite another ARPEntry with the <b> same </b> ip.
		 * @param entry To store
		 */
		public synchronized void storeEntry( ARPEntry entry ) {
			logger.info("Storing " + entry);
			arpEntries.put(entry.getIpAddress(), entry.getMacAddress());
			printCache();
		}
		
		/**
		 * Remove a ARPEntry identified by a given IPAddress,
		 * Can not fail, if Entry does not exist, does nothing.
		 * @param ipAddress Of the ARPEntry to delete
		 */
		public synchronized void deleteEntryByIp( IPv4Address ipAddress ) {		
			if ( arpEntries.containsKey(ipAddress) ) {
				logger.info("Deleting " + arpEntries.get(ipAddress));
				arpEntries.remove(ipAddress);
				printCache();
			}
		}
		
		public synchronized void reset() {
			logger.info("Reset cache (clear)");
			arpEntries.clear();
			printCache();
		}
		
		void printCache( ) {
			if ( printAlwaysFullCache ) {
				logger.info("Printing all Entries:");
				for ( IPv4Address addr : arpEntries.keySet() ) {
					logger.info( ARPEntry.of( addr, arpEntries.get(addr) ).toString() );
				}
			}
		}
		
		
}
