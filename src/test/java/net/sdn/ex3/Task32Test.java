package net.sdn.ex3;

import java.util.HashMap;
import java.util.HashSet;

import java.util.Set;

import org.junit.Test;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;

import net.floodlightcontroller.linkdiscovery.Link;
import net.floodlightcontroller.routing.BroadcastTree;
import static net.sdnlab.common.dijkstra.Dijkstra.compute;
public class Task32Test {
	
	@Test
	public void playAroundWithDijkstraTest() {
		HashMap<DatapathId, Set<Link>> topology = new HashMap<DatapathId, Set<Link>>();
		DatapathId root = DatapathId.of("0");
		DatapathId S1 = DatapathId.of("1");
		DatapathId S2 = DatapathId.of("2");
		DatapathId S3 = DatapathId.of("3");
		DatapathId S4 = DatapathId.of("4");
		DatapathId S5 = DatapathId.of("5");
		
		
		
		
		Set<Link> linksroot = new HashSet<Link>();
		Set<Link> linksS1 = new HashSet<Link>();
		Set<Link> linksS2 = new HashSet<Link>();
		Set<Link> linksS3 = new HashSet<Link>();
		Set<Link> linksS4 = new HashSet<Link>();
		Set<Link> linksS5 = new HashSet<Link>();
		
		
		Link rootS1 = new Link(root,OFPort.of(1),  S1, OFPort.of(1), U64.of(1) );
		linksroot.add( rootS1 );
		
		Link S1S4 = new Link(S1, OFPort.of(2),  S4, OFPort.of(1), U64.of(1) );
		Link S1S2 = new Link(S1, OFPort.of(3),  S2, OFPort.of(1), U64.of(1) );
		linksS1.add( S1S4 );
		linksS1.add( S1S2 );
		
		Link S2S3 = new Link(S2, OFPort.of(2),  S3, OFPort.of(1), U64.of(1) );
		linksS2.add( S2S3 );
		
		Link S4S5 = new Link(S4, OFPort.of(2),  S5, OFPort.of(1), U64.of(1) );
		linksS4.add( S4S5 );
		
		Link S5S3 = new Link(S5, OFPort.of(2),  S3, OFPort.of(2), U64.of(1) );
		linksS5.add( S5S3  );
		Link S3S5 = new Link(S3, OFPort.of(2),  S5, OFPort.of(2), U64.of(1) );
		linksS3.add(  S3S5 );
		
		HashMap<Link, Integer> linkCost = new HashMap<Link, Integer>();
		linkCost.put(rootS1, 1);
		linkCost.put( S1S4 , 1 );
		linkCost.put( S1S2 , 10 );
		
		linkCost.put( S2S3 , 1 );
		
		linkCost.put( S4S5 , 10 );
		
		linkCost.put( S5S3 , 1 );
		
		linkCost.put( S3S5 , 1 );
		
		
		topology.put(root, linksroot);
		topology.put(S1, linksS1);
		topology.put(S2, linksS2);
		topology.put(S3, linksS3);
		topology.put(S4, linksS4);
		topology.put(S5, linksS5);
		
		BroadcastTree tree = compute(topology, root, linkCost, false);	
		
		Link nextLink = null;
		DatapathId nextNode = S5;
		while ( nextNode != root ) {
			System.out.println(nextNode);
			nextLink = tree.getTreeLink(nextNode);
			System.out.println(nextLink);
			nextNode = nextLink.getSrc();
		}
		System.out.println(nextNode);
	}


}

