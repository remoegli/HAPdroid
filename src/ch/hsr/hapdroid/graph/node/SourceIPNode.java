package ch.hsr.hapdroid.graph.node;

import java.net.InetAddress;
import java.net.UnknownHostException;

import ch.hsr.hapdroid.graph.Transaction;


public class SourceIPNode extends IPNode {

	public SourceIPNode(InetAddress ip, Transaction t) {
		super(ip, t);
//		try {
//			setValue(InetAddress.getLocalHost());
//		} catch (UnknownHostException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
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
