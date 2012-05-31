package ch.hsr.hapdroid.transaction;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import ch.hsr.hapdroid.network.Flow;

import android.util.Log;


public class Transaction {
	
	private static final String LOG_TAG = "Transaction";
	private static final String SPLIT_STRING = "\\s+";
	private long mBytes;
	private long mPackets;
	private int mDirection;
	
	private Node<InetAddress> mSrcIp;
	private Node<Integer> mProtocol;
	private Node<Integer> mSourcePort;
	private Node<Integer> mDstPort;
	private Node<InetAddress> mDstIp;
	private List<Flow> mFlows;

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
		
		setSrcIpData(trans[1], t);
		setProtoData(trans[2], t);
		setSrcPortData(trans[3], t);
		setDstPortData(trans[4], t);
		setDstIpData(trans[5], t);

		//ignore local internal captured packages
		if (t.getProto().getValue().intValue() == 0)
			return null;
		
		return t;
	}
	
	private static void setDstIpData(String dstip, Transaction t) {
		String[] tokens = dstip.split(SPLIT_STRING);
		
		try {
			Node<InetAddress> n = new Node<InetAddress>(Inet4Address.getByName(tokens[1]), t);
			t.setDstIp(n);
			setSummarized(tokens[0], n);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	private static void setDstPortData(String dstport, Transaction t) {
		String[] tokens = dstport.split(SPLIT_STRING);
		
		Node<Integer> n = new Node<Integer>(Integer.valueOf(tokens[1]), t);
		t.setDstPort(n);
		setSummarized(tokens[0], n);
	}
	
	private static void setSrcPortData(String srcPort, Transaction t) {
		String[] tokens = srcPort.split(SPLIT_STRING);
		
		Node<Integer> n = new Node<Integer>(Integer.valueOf(tokens[1]), t);
		t.setSrcPort(n);
		setSummarized(tokens[0], n);
	}
	
	private static void setProtoData(String proto, Transaction t) {
		String[] tokens = proto.split(SPLIT_STRING);
		
		t.setProto(new Node<Integer>(Integer.valueOf(tokens[1]), t));
	}
	
	private static void setSrcIpData(String srcip, Transaction t) {
		String[] tokens = srcip.split(SPLIT_STRING);
		
		try {
			Node<InetAddress> n = new Node<InetAddress>(Inet4Address.getByName(tokens[1]), t);
			t.setSrcIp(n);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	private static void setSummarized(String string, Node<?> n) {
		if (string.length()>0 && string.charAt(0)=='s')
			n.setSummarized(true);
	}

	private void setSrcIp(Node<InetAddress> node) {
		this.mSrcIp = node;
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

	public void setFlows(List<Flow> flowlist) {
		long packets = 0;
		long bytes = 0;
		
		for (Flow f : flowlist){
			packets += f.getPacketCount();
			bytes += f.getPayloadCount();
		}
		
		mPackets = packets;
		mBytes = bytes;
		
		mFlows = flowlist;
	}
	
	public List<Flow> getFlows() {
		return mFlows;
	}
}
