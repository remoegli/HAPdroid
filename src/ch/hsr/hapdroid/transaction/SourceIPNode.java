package ch.hsr.hapdroid.transaction;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class SourceIPNode extends IPNode {

	public SourceIPNode(InetAddress ip, Transaction t) {
		super(ip, t);
		try {
			setValue(InetAddress.getLocalHost());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "localhost";
	}
}
