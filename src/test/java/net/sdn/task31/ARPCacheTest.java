package net.sdn.task31;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.MacAddress;

import net.sdnlab.task31.ARPCache;
import net.sdnlab.task31.ARPEntry;
public class ARPCacheTest {
	@Test
	public void testBasics() {
		ARPCache cache = new ARPCache();
		// Check if i can store entries
		cache.storeEntry(ARPEntry.of(IPv4Address.of("127.0.0.1"), MacAddress.of("11:22:33:44:55:66")));
		assertTrue( cache.contains( IPv4Address.of("127.0.0.1")) );
		// check if we don't have "wrong" entries"
		assertFalse( cache.contains(IPv4Address.of("127.0.0.2")) );
		
		// store another entry
		ARPEntry entry = ARPEntry.of(IPv4Address.of("127.0.0.2"), MacAddress.of("00:22:33:44:55:66"));
		cache.storeEntry(entry);
		assertTrue( cache.contains(IPv4Address.of("127.0.0.2")) );
		// check if it is really the same
		ARPEntry entToCompare = cache.getEntryFromIP(IPv4Address.of("127.0.0.2"));
		assertEquals(entry, entToCompare );
		
		// now, maybe from time to time we want to delete entries
		cache.deleteEntryByIp( IPv4Address.of("127.0.0.2"));
		assertFalse( cache.contains(IPv4Address.of("127.0.0.2")) );
	}
}
