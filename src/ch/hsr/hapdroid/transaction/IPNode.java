package ch.hsr.hapdroid.transaction;

import java.net.InetAddress;

public class IPNode extends Node<InetAddress>{
	public IPNode(InetAddress ip, Transaction t) {
		super(ip,t);
	}
	
	@Override
	public String toString() {
		InetAddress ip = getValue();
		if (isSummarized()){
			byte[] address = ip.getAddress();
			return Byte.toString(address[3]);
		}
		return super.toString().replace('/', ' ');
	}
}
