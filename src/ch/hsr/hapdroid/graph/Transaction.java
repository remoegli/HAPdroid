package ch.hsr.hapdroid.graph;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.stericson.RootTools.RootTools;

import android.util.Log;
import ch.hsr.hapdroid.graph.node.IPNode;
import ch.hsr.hapdroid.graph.node.Node;
import ch.hsr.hapdroid.graph.node.ProtoNode;
import ch.hsr.hapdroid.graph.node.SourceIPNode;
import ch.hsr.hapdroid.network.CaptureSource;
import ch.hsr.hapdroid.network.Flow;
import ch.hsr.hapdroid.network.Proto;

/**
 * A class representing a unique path of the HAP graphlet.
 * 
 * A transaction is defined as a distinct path of the HAP graphlet.
 * It differs from a flow in the sense that it can contain summarized
 * nodes.
 * 
 * @author "Dominik Spengler"
 *
 */
public class Transaction {
	
	private static final String LOG_TAG = "Transaction";
	private static final String SPLIT_STRING = "\\s+";
	private long mBytes;
	private long mPackets;
	private int mDirection;
	
	private SourceIPNode mSrcIp;
	private ProtoNode mProtocol;
	private Node<Integer> mSourcePort;
	private Node<Integer> mDstPort;
	private IPNode mDstIp;
	private List<Flow> mFlows;

	public Transaction() {
		mFlows = new ArrayList<Flow>();
	}

	/**
	 * Parses a transaction from its string representation.
	 * 
	 * The string representation is derived from the FSG file format. 
	 * Bolow is an example of how a transaction might look like.
	 * 
	 * t
	 * SrcIp 10.10.10.10
	 * Proto 6
	 * sSrcPort 5
	 * DstPort 80
	 * DstIp 10.10.10.20
	 * 
	 * The order of lines is important. A unicase s indicates
	 * summarized nodes.
	 * 
	 * @param trans String array with the transaction data
	 * @return parsed Transaction
	 */
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

		//ignore invalid packages
		if (t.getDstPort().getValue() == 0)
			return null;
		
		return t;
	}
	
	private static void setDstIpData(String dstip, Transaction t) {
		String[] tokens = dstip.split(SPLIT_STRING);
		
		try {
			IPNode n = new IPNode(Inet4Address.getByName(tokens[1]), t);
			setSummarized(tokens[0], n);
			t.setDstIp(n);
		} catch (UnknownHostException e) {
			//if the node is summarized node, a UnknownHostException will be thrown
			//we use the forth octed as container for the host count value
			try {
				IPNode n = new IPNode(Inet4Address.getByName("255.255.255." +tokens[1]), t);
				setSummarized(tokens[0], n);
				t.setDstIp(n);
			} catch (UnknownHostException e1) {
				// Should not happen
				e1.printStackTrace();
			}
			RootTools.log(LOG_TAG, "Summarized DstIp node: " + t.getDstIp().getValue());
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
		t.setProto(new ProtoNode(p, t));
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

	/**
	 * @see java.lang.Object#toString()
	 */
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

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof Transaction){
			Transaction other = (Transaction) o;
			boolean equalsSummarized = 
					other.getSrcIp().isSummarized() == getSrcIp().isSummarized() &&
					other.getProto().isSummarized() == getProto().isSummarized() &&
					other.getSrcPort().isSummarized() == getSrcPort().isSummarized() &&
					other.getDstPort().isSummarized() == getDstPort().isSummarized() &&
					other.getDstIp().isSummarized() == getDstIp().isSummarized();
			
			if (!equalsSummarized)
				return false;
			
			return 	other.getSrcIp().equals(getSrcIp()) &&
					other.getProto().equals(getProto()) &&
					other.getSrcPort().equals(getSrcPort()) &&
					other.getDstPort().equals(getDstPort()) &&
					other.getDstIp().equals(getDstIp());
		} else
			return super.equals(o);
	}
	
	/**
	 * Getter for {@link SourceIPNode}
	 * 
	 * @return {@link SourceIPNode} of the transaction
	 */
	public SourceIPNode getSrcIp() {
		return mSrcIp;
	}

	/**
	 * Getter for {@link ProtoNode}
	 * 
	 * @return {@link ProtoNode} of the transaction
	 */
	public ProtoNode getProto() {
		return mProtocol;
	}

	/**
	 * Setter for {@link ProtoNode}
	 * 
	 * @param protocol
	 */
	public void setProto(ProtoNode protocol) {
		this.mProtocol = protocol;
	}

	/**
	 * Getter for source port
	 * 
	 * @return {@link Node} with Integer value
	 */
	public Node<Integer> getSrcPort() {
		return mSourcePort;
	}

	/**
	 * Setter for the source port
	 * 
	 * @param sourcePort
	 */
	public void setSrcPort(Node<Integer> sourcePort) {
		this.mSourcePort = sourcePort;
	}

	/**
	 * Getter for the destination port
	 * 
	 * @return {@link Node} with Integer value
	 */
	public Node<Integer> getDstPort() {
		return mDstPort;
	}
	
	/**
	 * Setter for the destination port
	 * 
	 * @param dstPort
	 */
	public void setDstPort(Node<Integer> dstPort) {
		this.mDstPort = dstPort;
	}

	/**
	 * Getter for the destination IP
	 * 
	 * @return {@link IPNode}
	 */
	public IPNode getDstIp() {
		return mDstIp;
	}

	/**
	 * Setter for the destination IP
	 * 
	 * @param dstIp
	 */
	public void setDstIp(IPNode dstIp) {
		this.mDstIp = dstIp;
	}

	/**
	 * Get total byte count of the transaction.
	 * 
	 * @return total byte count
	 */
	public long getBytes() {
		return mBytes;
	}

	/**
	 * Get total packet count of the transaction.
	 * 
	 * @return total packet count
	 */
	public long getPackets() {
		return mPackets;
	}

	/**
	 * Get the direction of the transaction.
	 * 
	 * @return direction
	 * @see Flow#TYPE_BIFLOW
	 * @see Flow#TYPE_INCOMING
	 * @see Flow#TYPE_OUTGOING
	 * @see Flow#TYPE_OKFLOW
	 * @see Flow#TYPE_UNIBIFLOW
	 * @see Flow#TYPE_UNIFLOW
	 */
	public int getDirection() {
		return mDirection;
	}

	/**
	 * Set the list of flows belonging to the transaction.
	 * 
	 * Please note that no checks for the existing values
	 * of the transaction are performed. Hence it is possible
	 * to add a list of flows which is not described by the 
	 * transaction.
	 * 
	 * @param flowlist list of flows belonging to the transaction
	 */
	public void setFlows(List<Flow> flowlist) {
		long packets = 0;
		long bytes = 0;
		
		// save since flowlist should not be empty
		mDirection = flowlist.get(0).getDirection();
		for (Flow f : flowlist){
			if (mDirection < Flow.TYPE_BIFLOW && f.getDirection() == Flow.TYPE_BIFLOW)
				mDirection = Flow.TYPE_UNIBIFLOW | mDirection;
			else
				mDirection = f.getDirection();
			packets += f.getPacketCount();
			bytes += f.getByteCount();
		}
		
		mPackets = packets;
		mBytes = bytes;
		mFlows = flowlist;
	}
	
	/**
	 * Getter for attached flow list.
	 * 
	 * @return list of flows set by {@link #setFlows(List)}
	 */
	public List<Flow> getFlows() {
		return mFlows;
	}

	/**
	 * Get the capture source of the attached flows.
	 * 
	 * Please not that this method merely returns the capture 
	 * source of the first attached flow.
	 * 
	 * @return {@link CaptureSource} of the first attached flow
	 */
	public CaptureSource getCaptureSource() {
		return mFlows.get(0).getCaptureSource();
	}
}
