package ch.hsr.hapdroid.transaction;

import java.net.InetAddress;

public class SourceIPNode extends IPNode {

	public SourceIPNode(InetAddress ip, Transaction t) {
		super(ip, t);
	}
	
	@Override
	public String toString() {
		return "localhost";
	}
}
