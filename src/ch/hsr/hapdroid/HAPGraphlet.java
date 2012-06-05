package ch.hsr.hapdroid;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Pseudograph;

import android.util.Log;

import ch.hsr.hapdroid.network.Proto;
import ch.hsr.hapdroid.transaction.Node;
import ch.hsr.hapdroid.transaction.NodeList;
import ch.hsr.hapdroid.transaction.Transaction;
import ch.hsr.hapdroid.transaction.UniqueNodeList;

public class HAPGraphlet extends Pseudograph<Node<?>, DefaultEdge>{
	private NodeList<InetAddress> mSrcIp;
	private NodeList<Proto> mProto;
	private NodeList<Integer> mSrcPort;
	private NodeList<Integer> mDstPort;
	private NodeList<InetAddress> mDstIp;
	private Set<Transaction> mTransactionList;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String LOG_tAG = "HAPGraphlet";

	public HAPGraphlet() {
		super(DefaultEdge.class);
		mTransactionList = new HashSet<Transaction>();
		
		mSrcIp = new UniqueNodeList<InetAddress>();
		mProto = new UniqueNodeList<Proto>();
		mSrcPort = new NodeList<Integer>();
		mDstPort = new NodeList<Integer>();
		mDstIp = new UniqueNodeList<InetAddress>();
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
		Log.d(LOG_tAG, "adding Transaction: "+trans);
		if (trans == null)
			return;
		
		if (getTransaction(trans) != null){
			Log.d(LOG_tAG, "Transaction already exist");
			return;
		}
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
		
		mTransactionList.add(trans);
	}

	private Transaction getTransaction(Transaction trans) {
		Transaction t;
		Iterator<Transaction> it = mTransactionList.iterator();
		while (it.hasNext()){
			t = it.next();
			if (t.equals(trans))
				return t;
		}
		return null;
	}

	public void clear() {
		mSrcIp.clear();
		mProto.clear();
		mSrcPort.clear();
		mDstPort.clear();
		mDstIp.clear();
	}

	public String showTransactions() {
		return mTransactionList.toString();
	}
}
