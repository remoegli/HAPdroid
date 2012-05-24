package ch.hsr.hapdroid.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import android.util.Log;

public class FlowTable {
	private static final String LOG_TAG = "FlowTable";
	private List<Flow> mFlowList;
	
	public FlowTable() {
		mFlowList = new ArrayList<Flow>();
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
		Collections.sort(mFlowList);
		Iterator<Flow> it = mFlowList.iterator();
		Flow f;
		for (int i = 0; it.hasNext(); i++){
			f = it.next();
			System.arraycopy(f.toByteArray(), 0, result, Flow.SIZE_BYTE*i, Flow.SIZE_BYTE);
		}
		Log.d(LOG_TAG, mFlowList.toString());
		return result;
	}
	
	@Override
	public String toString() {
		return mFlowList.toString();
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
