package ch.hsr.hapdroid.network;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import android.util.Log;
import ch.hsr.hapdroid.transaction.Transaction;

/**
 * Flow table consisting of a number of flows.
 * 
 * 
 * 
 * @author "Dominik Spengler"
 *
 */
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

	private void getLocalIpAddresses() {
		mLocalIpAddresses.clear();
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
	
	/**
	 * Sets the source IP of the flow table.
	 * 
	 * @param ip as a dotted formated string.
	 */
	public void setSourceIp(String ip) {
		try {
			mLocalIpAddresses.add(InetAddress.getByName(ip));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Imports cflow data into the flow table.
	 * 
	 * This method imports a byte array containing a list of flows
	 * in uncompressed cflow4 format into the flow table.
	 * 
	 * @param cflow uncompressed cflow4 data
	 * @return true if the byte array has been successfully imported,
	 * 		false otherwise
	 */
	public boolean importByteArray(byte[] cflow){
		if (cflow.length % Flow.SIZE_BYTE != 0)
			return false;
		
		byte[] flowdata = new byte[Flow.SIZE_BYTE];
		for(int i = 0; i < cflow.length; i += Flow.SIZE_BYTE){
			System.arraycopy(cflow, i, flowdata, 0, Flow.SIZE_BYTE);
			addFlow(new Flow(flowdata));
		}
		
		return true;
	}

	private void addFlow(Flow flow) {
		mFlowList.add(flow);
		setStartTime(flow);
		setEndTime(flow);
	}

	/**
	 * Add one packet to the flow table.
	 * 
	 * Adds the given packet to the flow table and updates start
	 * and end time accordingly.
	 * 
	 * @param {@link Packet}}
	 */
	public boolean add(Packet packet) {
		if (mLocalIpAddresses.isEmpty())
			getLocalIpAddresses();
		
		boolean toreturn = false;
		Flow f = getFlowFor(packet);

		if (f == null){
			f = createFlowFrom(packet);
			if (f == null){
				return false;
			}
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
		Flow f = new Flow(packet);
		if (isLocalIp(packet.src_addr)){
			f.setDirection(Flow.TYPE_OUTGOING);
		} else if (isLocalIp(packet.dst_addr)){
			f.setDirection(Flow.TYPE_INCOMING);
			f.reverse();
		} else{
			//ignore packet
			return null;
		}
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

	/**
	 * Converts the flow to an uncompressed cflow4 byte array.
	 * 
	 * @return uncompressed cflow4 byte array
	 */
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

	/**
	 * Clears the flow table and resets the source ip list.
	 */
	public void clear() {
		mFlowList.clear();
		mLocalIpAddresses.clear();
	}

	/**
	 * Getter for packet count of all flows contained in the flow table.
	 * 
	 * @return packet count of all flows inside the flow table
	 */
	public long getPacketCount() {
		long result = 0;
		for (Flow f : mFlowList){
			result += f.getPacketCount();
		}
		return result;
	}

	/**
	 * Getter for byte count of all flows contained in the flow table.
	 * 
	 * @return byte count of all flows inside the flow table
	 */
	public long getByteCount() {
		long result = 0;
		for (Flow f : mFlowList){
			result += f.getByteCount();
		}
		return result;
	}
	
	/**
	 * Getter for payload count of all flows contained in the flow table.
	 * 
	 * @return payload count of all flows inside the flow table
	 */
	public long getPayloadCount() {
		long result = 0;
		for (Flow f : mFlowList){
			result += f.getPayloadCount();
		}
		return result;
	}
	
	/**
	 * Get the end time of the last flow inside the table.
	 * 
	 * @return end time of the last flow
	 */
	public Timeval getEndTime() {
		return mEndTime;
	}
	
	/**
	 * Get the start time of the first flow inside the table.
	 * 
	 * @return start time of the first flow
	 */
	public Timeval getStartTime() {
		return mStartTime;
	}
	
	/**
	 * Get all flows that match the given transaction.
	 * 
	 * @param {@link Transaction} to get the flows for
	 * @return {@link List} of flows that match the transaction
	 */
	public List<Flow> getFlowsForTransaction(Transaction t){
		List<Flow> flowlist = new ArrayList<Flow>();
		
		for (Flow f : mFlowList){
			if (f.belongsTo(t))
				flowlist.add(f);
		}
		
		return flowlist;
	}

	/**
	 * Check whether the flow table is empty.
	 * 
	 * @return true if the flow table is empty,
	 * 		false otherwise
	 */
	public boolean isEmpty() {
		return mFlowList.isEmpty();
	}
}
