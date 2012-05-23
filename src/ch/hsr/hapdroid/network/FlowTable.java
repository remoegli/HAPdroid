package ch.hsr.hapdroid.network;

import java.util.SortedSet;
import java.util.TreeSet;

import android.util.Log;

public class FlowTable {
	private static final String LOG_TAG = "FlowTable";
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
	
	public byte[] toByteArray(){
		byte[] result = new byte[mFlowList.size()*Flow.SIZE_BYTE];
		int i = 0;
		for (Flow f : mFlowList){
			System.arraycopy(f.toByteArray(), 0, result, Flow.SIZE_BYTE*i, Flow.SIZE_BYTE);
			++i;
		}
		Log.d(LOG_TAG, mFlowList.toString());
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (Flow f : mFlowList)
			result.append(f.toString());
		return result.toString();
	}

	public void clear() {
		mFlowList.clear();
	}

	public long getPacketCount() {
		long result = 0;
		for (Flow f : mFlowList){
			result += f.getPacketCount();
		}
		return result;
	}

	public long getByteCount() {
		long result = 0;
		for (Flow f : mFlowList){
			result += f.getByteCount();
		}
		return result;
	}
	
	public long getPayloadCount() {
		long result = 0;
		for (Flow f : mFlowList){
			result += f.getPayloadCount();
		}
		return result;
	}
}
