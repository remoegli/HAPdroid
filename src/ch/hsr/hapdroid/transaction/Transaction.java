package ch.hsr.hapdroid.transaction;

import java.net.InetAddress;
import java.util.Vector;


public class Transaction {
	
	private Vector<String> mTransactionList;
	private Node<InetAddress> mSourceIp;
	private Node<Integer> mProtocol;
	private Node<Integer> mSourcePort;
	private Node<Integer> mDstPort;
	private Node<InetAddress> mDstIp;

	public Transaction() {
		mTransactionList = new Vector<String>();
	}

	public static Transaction parse(String trans){
		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		Object[] array = mTransactionList.toArray();
		for (Object o : array){
			result.append(", [" + o + "]");
		}
		return result.toString();
	}

	public Node<InetAddress> getSourceIp() {
		return mSourceIp;
	}

	public Node<Integer> getProtocol() {
		return mProtocol;
	}

	public void setProtocol(Node<Integer> mProtocol) {
		this.mProtocol = mProtocol;
	}

	public Node<Integer> getSrcPort() {
		return mSourcePort;
	}

	public void setSourcePort(Node<Integer> mSourcePort) {
		this.mSourcePort = mSourcePort;
	}

	public Node<Integer> getDstPort() {
		return mDstPort;
	}

	public void setDstPort(Node<Integer> mDstPort) {
		this.mDstPort = mDstPort;
	}

	public Node<InetAddress> getDstIp() {
		return mDstIp;
	}

	public void setDstIp(Node<InetAddress> mDstIp) {
		this.mDstIp = mDstIp;
	}
}
