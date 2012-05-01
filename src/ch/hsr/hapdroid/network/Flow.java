package ch.hsr.hapdroid.network;

import java.net.InetAddress;

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
	private static final byte MAGIC_NUMBER = 1;
	public static final int SIZE_BYTE = 48;
	
	private InetAddress src_addr;
	private InetAddress dst_addr;
	private int src_port;
	private int dst_port;
	private int as_local;
	private int as_remote;
	private byte proto;
	private byte tos;
	private Timeval starttime;
	
	private Timeval duration;
	private long flowSize;
	private byte direction;
	private int pkgCount;

	public Flow(Packet p) {
		src_addr = p.src_addr;
		src_port = p.src_port;
		dst_addr = p.dst_addr;
		dst_port = p.dst_port;
		proto = p.proto;
		
		tos = p.tos;
		starttime = p.timestamp;
		flowSize = p.payload_size;
		pkgCount = 1;
	}

	public void add(Packet p) {
		++pkgCount;
		flowSize += p.payload_size;
		duration = Timeval.getDifference(p.timestamp, starttime);
	}

	@Override
	public int compareTo(Flow another) {
		int result = src_addr.getHostAddress().compareTo(another.src_addr.getHostAddress());
		if (result == 0)
			result = dst_addr.getHostAddress().compareTo(another.dst_addr.getHostAddress());
		if (result == 0)
			result = starttime.compareTo(another.starttime);
		
		return result;
	}

	public boolean describes(Packet p) {
		boolean isSrcEqual = src_addr.equals(p.src_addr) && src_port == p.src_port;
		boolean isDstEqual = dst_addr.equals(p.dst_addr) && dst_port == p.dst_port;
		boolean isProtoEqual = proto == p.proto;
		
		return isSrcEqual && isDstEqual && isProtoEqual;
	}
	
	public byte[] toByteArray(){
		byte[] result = new byte[48];
		
		setHostByteOrder(result, 0, src_addr.getAddress());
		setHostByteOrder(result, 4, dst_addr.getAddress());
		System.arraycopy(starttime.getByteArrayMs(), 0, result, 8, 8);
		System.arraycopy(duration.getByteArrayMs(), 0, result, 16, 4);
		
		set16bitInt(result, 20, src_port);
		set16bitInt(result, 22, dst_port);
		
		set48bitLong(result, 24, flowSize);
		set32bitLong(result, 32, pkgCount);
		
		set16bitInt(result, 36, as_local);
		set16bitInt(result, 38, as_remote);
		
		result[40] = proto;
		result[41] = direction;
		result[42] = tos;
		result[43] = MAGIC_NUMBER;
		
		return result;
	}

	private void set32bitLong(byte[] result, int start_index, long value) {
		writeOut(result, start_index, value, 4);
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

	private void writeOut(byte[] result, int start_index, long value, int count) {
		long tmp = value;
		for(int i = 0; i < count; ++i){
			result[start_index+i] = (byte) tmp;
			tmp <<= 8;
		}
	}

	private void set16bitInt(byte[] result, int start_index, int value) {
		result[start_index] = (byte) (value);
		result[start_index+1] = (byte) (value >> 8);
	}

	private void setHostByteOrder(byte[] result, int start_index, byte[] address) {
		int end_index = start_index + 3;
		for (int i = start_index; i <= end_index; ++i){
			result[end_index - i] = address[i];
		}
	}

	@Override
	public String toString() {
		return src_addr.toString() +":"+src_port + " TO "+
			dst_addr.toString() +":"+dst_port + " PROTO "+proto+
			" SIZE "+flowSize+" COUNT "+pkgCount+
			" DIRECTION "+direction+" DURATION "+duration+"\n";
	}
}
