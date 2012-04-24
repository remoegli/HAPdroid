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
		for (Flow f : mFlowList){
			if (f.describes(packet))
				return f;
		}
		return null;
	}

	private void addPacketToFlow(Packet packet, Flow flow) {
		flow.add(packet);
	}

	private void createFlowFrom(Packet packet) {
		mFlowList.add(new Flow(packet));
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (Flow f : mFlowList)
			result.append(f.toString());
		return result.toString();
	}

}
