package ch.hsr.hapdroid.network;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import android.util.Log;
import ch.hsr.hapdroid.transaction.Transaction;

public class FlowTable {
	private static final String LOG_TAG = "FlowTable";
	private List<Flow> mFlowList;
	private Timeval mStartTime;
	private Timeval mEndTime;
	private ArrayList<InetAddress> mLocalIpAddresses;
	
	public FlowTable() {
		mFlowList = new ArrayList<Flow>();
		mLocalIpAddresses = new ArrayList<InetAddress>();
		getLocalIpAddresses();
	}

	public void getLocalIpAddresses() {
	    try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress()) {
	                    mLocalIpAddresses.add(inetAddress);
	                }
	            }
	        }
	    } catch (SocketException e) {
	        e.printStackTrace();
	    }
	}
	
	public boolean add(Packet packet) {
		boolean toreturn = false;
		Flow f = getFlowFor(packet);

		if (f == null){
			f = createFlowFrom(packet);
			toreturn = true;
		} else {
			addPacketToFlow(packet, f);
			toreturn = false;
		}
		
		setStartTime(f);
		setEndTime(f);
		return toreturn;
	}

	private void setStartTime(Flow f) {
		if(mStartTime == null)
			mStartTime = f.getStartTime();
	}

	private void setEndTime(Flow f) {
		if (mEndTime == null)
			mEndTime = new Timeval(f.getStartTime());
		mEndTime.add(f.getDuration());
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

	private Flow createFlowFrom(Packet packet) {
		if (isLocalIp(packet.dst_addr)){
			packet.reverse();
		}
		Flow f = new Flow(packet);
		mFlowList.add(f);
		return f;
	}
	
	private boolean isLocalIp(InetAddress dst_addr) {
		if (dst_addr.isAnyLocalAddress())
			return true;
		
		for (InetAddress ip : mLocalIpAddresses){
			if (ip.equals(dst_addr))
				return true;
		}
		return false;
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
	
	public Timeval getEndTime() {
		return mEndTime;
	}
	
	public Timeval getStartTime() {
		return mStartTime;
	}
	
	public List<Flow> getFlowsForTransaction(Transaction t){
		List<Flow> flowlist = new ArrayList<Flow>();
		
		for (Flow f : mFlowList){
			if (f.belongsTo(t))
				flowlist.add(f);
		}
		
		return flowlist;
	}

	public boolean isEmpty() {
		return mFlowList.isEmpty();
	}
}
