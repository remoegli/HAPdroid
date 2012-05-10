package ch.hsr.hapdroid.transaction;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.util.Log;


public class Transaction {
	
	private static final String LOG_TAG = "Transaction";
	private long mBytes;
	private long mPackets;
	private int mDirection;
	
	private Node<InetAddress> mSrcIp;
	private Node<Integer> mProtocol;
	private Node<Integer> mSourcePort;
	private Node<Integer> mDstPort;
	private Node<InetAddress> mDstIp;

	public Transaction() {
	}

	public static Transaction parse(String[] trans){
		Transaction t = new Transaction();
		
		if (trans == null || trans.length < 6)
			return null;
		for(String s : trans){
			if (s == null){
				Log.e(LOG_TAG, "incomplete transaction recieved");
				return null;
			}
		}
		
		setTransactionData(trans[0],t);
		setSrcIpData(trans[1], t);
		setProtoData(trans[2], t);
		setSrcPortData(trans[3], t);
		setDstPortData(trans[4], t);
		setDstIpData(trans[5], t);

		return t;
	}
	
	private static void setDstIpData(String dstip, Transaction t) {
		String[] tokens = dstip.split(" ");
		
		try {
			t.setDstIp(new Node<InetAddress>(Inet4Address.getByName(tokens[1])));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	private static void setDstPortData(String dstport, Transaction t) {
		String[] tokens = dstport.split(" ");
		
		t.setDstPort(new Node<Integer>(Integer.valueOf(tokens[1])));
	}
	
	private static void setSrcPortData(String srcPort, Transaction t) {
		String[] tokens = srcPort.split(" ");
		
		t.setSrcPort(new Node<Integer>(Integer.valueOf(tokens[1])));
	}
	
	private static void setProtoData(String proto, Transaction t) {
		String[] tokens = proto.split(" ");
		
		t.setProto(new Node<Integer>(Integer.valueOf(tokens[1])));
	}
	
	private static void setSrcIpData(String srcip, Transaction t) {
		String[] tokens = srcip.split(" ");
		
		try {
			t.setSrcIp(new Node<InetAddress>(Inet4Address.getByName(tokens[1])));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	private void setSrcIp(Node<InetAddress> node) {
		this.mSrcIp = node;
	}

	private static void setTransactionData(String trans, Transaction t) {
		String[] tokens = trans.split(" ");
		
		t.setBytes(Long.parseLong(tokens[1]));
		t.setPackets(Long.parseLong(tokens[2]));
		t.setDirection(Integer.parseInt(tokens[3]));
	}

	@Override
	public String toString() {
		String result = "[ " + mSrcIp.toString() + ", " +
				mProtocol.toString() + ", " +
				mSourcePort.toString() + ", " +
				mDstPort.toString() + ", " +
				mDstIp.toString() + "]";
		return result.toString();
	}

	public Node<InetAddress> getSrcIp() {
		return mSrcIp;
	}

	public Node<Integer> getProto() {
		return mProtocol;
	}

	public void setProto(Node<Integer> mProtocol) {
		this.mProtocol = mProtocol;
	}

	public Node<Integer> getSrcPort() {
		return mSourcePort;
	}

	public void setSrcPort(Node<Integer> mSourcePort) {
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

	public long getBytes() {
		return mBytes;
	}

	public void setBytes(long mBytes) {
		this.mBytes = mBytes;
	}

	public long getPackets() {
		return mPackets;
	}

	public void setPackets(long mPackets) {
		this.mPackets = mPackets;
	}

	public int getDirection() {
		return mDirection;
	}

	public void setDirection(int mDirection) {
		this.mDirection = mDirection;
	}
}
