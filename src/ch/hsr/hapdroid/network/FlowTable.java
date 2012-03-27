package ch.hsr.hapdroid.network;

import java.util.SortedSet;
import java.util.TreeSet;

public class FlowTable {
	private SortedSet<Flow> mFlowList;
	
	public FlowTable() {
		mFlowList = new TreeSet<Flow>();
	}

	public boolean add(Packet packet) {
		Flow f = getFlowFor(packet);
		if (f == null){
			createFlowFrom(packet);
			return true;
		} else {
			addPacketToFlow(packet, f);
			return false;
		}
	}

	private Flow getFlowFor(Packet packet) {
		// TODO Auto-generated method stub
		return null;
	}

	private void addPacketToFlow(Packet packet, Flow flow) {
		// TODO Auto-generated method stub
		
	}

	private void createFlowFrom(Packet packet) {
		// TODO Auto-generated method stub
		
	}

}
