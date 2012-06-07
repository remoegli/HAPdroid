package ch.hsr.hapdroid.graph.node;

import java.net.InetAddress;

import ch.hsr.hapdroid.graph.Transaction;

/**
 * Since an Android device might have two active source IP
 * addresses this class makes sure that only one source is 
 * shown and it is always called "localhost"
 * 
 * @author "Dominik Spengler"
 *
 */
public class SourceIPNode extends IPNode {

	public SourceIPNode(InetAddress ip, Transaction t) {
		super(ip, t);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof SourceIPNode)
			return true;
		return super.equals(o);
	}

	@Override
	public String toString() {
		return "localhost";
	}
}
