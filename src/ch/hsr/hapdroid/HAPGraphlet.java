package ch.hsr.hapdroid;

import java.net.InetAddress;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Pseudograph;

import ch.hsr.hapdroid.network.Proto;
import ch.hsr.hapdroid.transaction.DuplicateNodeList;
import ch.hsr.hapdroid.transaction.Node;
import ch.hsr.hapdroid.transaction.NodeList;
import ch.hsr.hapdroid.transaction.Transaction;

public class HAPGraphlet extends Pseudograph<Node<?>, DefaultEdge>{
	private NodeList<InetAddress> mSrcIp;
	private NodeList<Proto> mProto;
	private NodeList<Integer> mSrcPort;
	private NodeList<Integer> mDstPort;
	private NodeList<InetAddress> mDstIp;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public HAPGraphlet() {
		super(DefaultEdge.class);
		
		
		mSrcIp = new NodeList<InetAddress>();
		mProto = new NodeList<Proto>();
		mSrcPort = new DuplicateNodeList<Integer>();
		mDstPort = new DuplicateNodeList<Integer>();
		mDstIp = new NodeList<InetAddress>();
	}
	
	public NodeList<InetAddress> getSrcIpList() {
		return mSrcIp;
	}

	public NodeList<Proto> getProtoList() {
		return mProto;
	}

	public NodeList<Integer> getSrcPortList() {
		return mSrcPort;
	}

	public NodeList<Integer> getDstPortList() {
		return mDstPort;
	}

	public NodeList<InetAddress> getDstIpList() {
		return mDstIp;
	}

	public void add(Transaction trans){
		if (trans == null)
			return;
		
		Node<InetAddress> srcIp = mSrcIp.add(trans.getSrcIp());
		addVertex(srcIp);
		
		Node<Proto> proto = mProto.add(trans.getProto());
		addVertex(proto);
		addEdge(srcIp, proto);
		
		Node<Integer> srcPort = mSrcPort.add(trans.getSrcPort());
		addVertex(srcPort);
		addEdge(proto, srcPort);
		
		Node<Integer> dstPort = mDstPort.add(trans.getDstPort());
		addVertex(dstPort);
		addEdge(srcPort, dstPort);
		
		Node<InetAddress> dstIp = mDstIp.add(trans.getDstIp());
		addVertex(dstIp);
		addEdge(dstPort, dstIp);
	}

	public void clear() {
		mSrcIp.clear();
		mProto.clear();
		mSrcPort.clear();
		mDstPort.clear();
		mDstIp.clear();
	}
}
