package ch.hsr.hapdroid.transaction;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.List;

import android.util.Log;
import ch.hsr.hapdroid.network.CaptureSource;
import ch.hsr.hapdroid.network.Flow;
import ch.hsr.hapdroid.network.Proto;


public class Transaction {
	
	private static final String LOG_TAG = "Transaction";
	private static final String SPLIT_STRING = "\\s+";
	private long mBytes;
	private long mPackets;
	private int mDirection;
	
	private SourceIPNode mSrcIp;
	private Node<Proto> mProtocol;
	private Node<Integer> mSourcePort;
	private Node<Integer> mDstPort;
	private IPNode mDstIp;
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

		//ignore internal local server socket packages
		if (t.getDstPort().getValue() == 0)
			return null;
		
		return t;
	}
	
	private static void setDstIpData(String dstip, Transaction t) {
		String[] tokens = dstip.split(SPLIT_STRING);
		
		try {
			IPNode n = new IPNode(Inet4Address.getByName(tokens[1]), t);
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
		Proto p = Proto.get(Integer.valueOf(tokens[1]));
		t.setProto(new Node<Proto>(p, t));
	}
	
	private static void setSrcIpData(String srcip, Transaction t) {
		String[] tokens = srcip.split(SPLIT_STRING);
		
		try {
			SourceIPNode n = new SourceIPNode(Inet4Address.getByName(tokens[1]), t);
			t.setSrcIp(n);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	private static void setSummarized(String string, Node<?> n) {
		if (string.length()>0 && string.charAt(0)=='s')
			n.setSummarized(true);
	}

	private void setSrcIp(SourceIPNode node) {
		this.mSrcIp = node;
	}

	@Override
	public String toString() {
		String result = "[ " + mSrcIp.toString() + ", " +
				mProtocol.toString() + ", " +
				mSourcePort.toString() + ", " +
				mDstPort.toString() + ", " +
				mDstIp.toString() + "," +
				Long.toString(mBytes) + "," +
				Long.toString(mPackets) + "]";
		return result.toString();
	}

	public SourceIPNode getSrcIp() {
		return mSrcIp;
	}

	public Node<Proto> getProto() {
		return mProtocol;
	}

	public void setProto(Node<Proto> mProtocol) {
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

	public IPNode getDstIp() {
		return mDstIp;
	}

	public void setDstIp(IPNode mDstIp) {
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
		mDirection = flowlist.get(0).getDirection();
		mFlows = flowlist;
	}
	
	public List<Flow> getFlows() {
		return mFlows;
	}

	public CaptureSource getCaptureSource() {
		if (!mFlows.isEmpty())
			return mFlows.get(0).getCaptureSource();
		
		return CaptureSource.UNKNOWN;
	}
}
