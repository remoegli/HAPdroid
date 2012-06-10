package ch.hsr.hapdroid.graph.node;

import java.net.InetAddress;

import ch.hsr.hapdroid.graph.Transaction;

/**
 * This class was needed because the equals and toString 
 * methods need to be handled in a special way for IP addresses.
 * 
 * @author "Dominik Spengler"
 *
 */
public class IPNode extends Node<InetAddress>{
	public IPNode(InetAddress ip, Transaction t) {
		super(ip,t);
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof IPNode){
			IPNode n = (IPNode) o;
			return n.getValue().equals(getValue());
		}
		
		return super.equals(o);
	}
	
	/**
	 * @see ch.hsr.hapdroid.graph.node.Node#toString()
	 */
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
