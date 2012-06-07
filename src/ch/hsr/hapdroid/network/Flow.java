package ch.hsr.hapdroid.network;

import java.net.InetAddress;
import java.net.UnknownHostException;

import android.util.Log;
import ch.hsr.hapdroid.graph.Transaction;
import ch.hsr.hapdroid.graph.node.Node;

/**
 * This class represents a flow with all necessary fields as
 * described in the cflow4 format.
 * 
 * @author Dominik Spengler
 *
 */
public class Flow implements Comparable<Flow>{
	public static final int TYPE_OUTGOING = 1;
	public static final int TYPE_INCOMING = 2;
	public static final int TYPE_UNIFLOW = 3;
	public static final int TYPE_BIFLOW = 4;
	public static final int TYPE_UNIBIFLOW = 8;
	public static final int TYPE_ALLFLOW = 7;
	public static final int TYPE_OKFLOW = 12;
	public static final int TYPE_SIMPLEFLOW = 15;
	public static final int TYPE_LATE = 16;
	public static final int TYPE_EARLY = 32;
	public static final int TYPE_LONGSTAND = 48;
	public static final int SIZE_BYTE = 48;

	private static final byte MAGIC_NUMBER = 1;
	private static final String LOG_TAG = "Flow";
	
	private InetAddress src_addr;
	private InetAddress dst_addr;
	private int src_port;
	private int dst_port;
	private int as_local;
	private int as_remote;
	private short proto;
	private short tos;
	
	private int pkgCount;
	private long flowSize;
	private long payloadSize;
	private Timeval starttime;
	private Timeval duration;
	private int direction;
	private CaptureSource source;

	public Flow(Packet p) {
		src_addr = p.src_addr;
		src_port = p.src_port;
		dst_addr = p.dst_addr;
		dst_port = p.dst_port;
		proto = p.proto;
		as_local = p.pid;
		source = p.source;
		
		tos = p.tos;
		starttime = p.timestamp;
		duration = new Timeval(0,0);
		flowSize = p.payload_size + p.header_size;
		payloadSize = p.payload_size;
		pkgCount = 1;
	}

	public Flow(byte[] flowdata) {
		try {
			src_addr = InetAddress.getByAddress(getNetworkByteOrder(flowdata, 0));
			dst_addr = InetAddress.getByAddress(getNetworkByteOrder(flowdata, 4));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		starttime = Timeval.getFromByteArray(flowdata, 8, 8);
		duration = Timeval.getFromByteArray(flowdata, 16, 4);
		src_port = get16bitInt(flowdata, 20);
		dst_port = get16bitInt(flowdata, 22);
		
		payloadSize = get48bitLong(flowdata, 24);
		flowSize = payloadSize;
		pkgCount = (int) get32bitLong(flowdata, 32);
		
		as_local = get16bitInt(flowdata, 36);
		as_remote = get16bitInt(flowdata, 38);
		
		proto = flowdata[40];
		direction = flowdata[41];
		tos = flowdata[42];
	}

	public void add(Packet p) {
		++pkgCount;
		flowSize += p.payload_size + p.header_size;
		payloadSize += p.payload_size;
		duration = Timeval.getDifference(p.timestamp, starttime);
		
		if (p.dst_addr.equals(src_addr))
			direction = TYPE_BIFLOW;
	}

	@Override
	public int compareTo(Flow another) {
		int result = src_addr.getHostAddress().compareTo(another.src_addr.getHostAddress());
		if (result == 0)
			result = dst_addr.getHostAddress().compareTo(another.dst_addr.getHostAddress());
		if (result == 0)
			result = (int) (another.flowSize - flowSize);
		
		return result;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof Flow)
			return compareTo((Flow) o) == 0;

		return super.equals(o);
	}

	public boolean describes(Packet p) {
		boolean isSrcEqual = false;
		boolean isDstEqual = false;
		boolean isProtoEqual = proto == p.proto;
		
		if (src_addr.equals(p.src_addr)){
			isSrcEqual = src_addr.equals(p.src_addr) && src_port == p.src_port;
			isDstEqual = dst_addr.equals(p.dst_addr) && dst_port == p.dst_port;
		} else {
			isSrcEqual = src_addr.equals(p.dst_addr) && src_port == p.dst_port;
			isDstEqual = dst_addr.equals(p.src_addr) && dst_port == p.src_port;
		}
		
		return isSrcEqual && isDstEqual && isProtoEqual;
	}
	
	public byte[] toByteArray(){
		byte[] result = new byte[Flow.SIZE_BYTE];
		
		setHostByteOrder(result, 0, src_addr.getAddress());
		setHostByteOrder(result, 4, dst_addr.getAddress());
		System.arraycopy(starttime.getByteArrayMs(), 0, result, 8, 8);
		System.arraycopy(duration.getByteArrayMs(), 0, result, 16, 4);
		
		set16bitInt(result, 20, src_port);
		set16bitInt(result, 22, dst_port);
		
		set48bitLong(result, 24, flowSize);
		Log.d(LOG_TAG, Long.toHexString(flowSize));
		set32bitLong(result, 32, pkgCount);
		Log.d(LOG_TAG, Long.toHexString(flowSize));
		
		set16bitInt(result, 36, as_local);
		set16bitInt(result, 38, as_remote);
		
		result[40] = (byte) proto;
		result[41] = (byte) direction;
		result[42] = (byte) tos;
		result[43] = MAGIC_NUMBER;
		
		return result;
	}

	private void set32bitLong(byte[] result, int start_index, long value) {
		writeOut(result, start_index, value, 4);
	}

	private long get32bitLong(byte[] source, int start_index){
		long result = 0;
		for(int i = 0; i < 4; ++i){
			result += ((long) source[start_index+i] & 0xff) << (8 * i);
		}
		return result;
	}
	/**
	 * Writes the bit representation of the long value into the given array.
	 * In order not to include the sign bit only 44 bits will get written.
	 * 
	 * @param result
	 * @param start_index
	 * @param value
	 */
	private void set48bitLong(byte[] result, int start_index, long value) {
		writeOut(result, start_index, value, 7);
	}
	
	private long get48bitLong(byte[] source, int start_index){
		long result = 0;
		for(int i = 0; i < 8; ++i){
			result += ((long) source[start_index+i] & 0xff) << (8 * i);
		}
		return result;
	}

	private void writeOut(byte[] result, int start_index, long value, int count) {
		long tmp = value;
		for(int i = 0; i < count; ++i){
			result[start_index+i] = (byte) tmp;
			tmp >>= 8;
		}
	}

	private void set16bitInt(byte[] result, int start_index, int value) {
		result[start_index] = (byte) (value);
		result[start_index+1] = (byte) (value >> 8);
	}
	
	private int get16bitInt(byte[] source, int start_index){
		int value = ((int) source[start_index] & 0xff);
		value += ((int) source[start_index+1] & 0xff) << 8;
		return value;
	}

	private void setHostByteOrder(byte[] result, int start_index, byte[] address) {
		if (address.length != 4)
			return;
		
		int end_index = start_index + 3;
		for (int i = 0; i <= 3; ++i){
			result[end_index - i] = address[i];
		}
	}
	
	private byte[] getNetworkByteOrder(byte[] source, int start_index){
		byte[] result = new byte[4];
		
		int end_index = start_index + 3;
		for (int i = 0; i <= 3; ++i) {
			result[i] = source[end_index - i];
		}
		
		return result;
	}

	@Override
	public String toString() {
		return src_addr.toString() +":"+src_port + " TO "+
			dst_addr.toString() +":"+dst_port + " PROTO "+proto+
			" SIZE "+flowSize+" COUNT "+pkgCount+
			" DIRECTION "+direction+" STARTTIME "+starttime+"\n";
	}

	public int getPacketCount() {
		return pkgCount;
	}
	
	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public long getByteCount() {
		return flowSize;
	}
	
	public long getPayloadCount(){
		return payloadSize;
	}

	public boolean belongsTo(Transaction t) {
		boolean srcIp = true;
		boolean booleanProto = true;
		boolean srcPort = true;
		Node<Integer> srcPortNode = t.getSrcPort();
		boolean dstPort = true;
		Node<Integer> dstPortNode = t.getDstPort();
		boolean dstIp = true;
		Node<InetAddress> dstIpNode = t.getDstIp();

		if (t.getProto().getValue() != Proto.get(proto))
			booleanProto = false;
		
		if (!srcPortNode.isSummarized() && srcPortNode.getValue().intValue() != src_port)
			srcPort = false;
		
		if (!dstPortNode.isSummarized() && dstPortNode.getValue().intValue() != dst_port)
			dstPort = false;
		
		if (!dstIpNode.isSummarized() && !dstIpNode.getValue().equals(dst_addr))
			dstIp = false;
		
		return srcIp && booleanProto && srcPort && dstPort && dstIp;
	}

	public Timeval getStartTime() {
		return starttime;
	}
	
	public Timeval getDuration() {
		return duration;
	}

	public CaptureSource getCaptureSource() {
		return source;
	}

	public void reverse() {
		InetAddress tmpIp = src_addr;
		src_addr = dst_addr;
		dst_addr = tmpIp;
		
		int tmpPort = src_port;
		src_port = dst_port;
		dst_port = tmpPort;
	}
}
