package ch.hsr.hapdroid.network;

import java.net.InetAddress;
import java.util.Date;

public class Flow implements Comparable<Flow>{
	public static final int DIRECTION_OUTGOING = 0;
	public static final int DIRECTION_INCOMING = 1;
	public static final int DIRECTION_TRANSIT = 2;
	
	private InetAddress src_addr;
	private int src_port;
	private InetAddress dst_addr;
	private int dst_port;
	private short proto;
	private short tos;
	private Date starttime;
	
	private long duration;
	private int flowSize;
	private int direction;
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
		duration = p.timestamp.getTime() - starttime.getTime();
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

	@Override
	public String toString() {
		return src_addr.toString() +":"+src_port + " TO "+
			dst_addr.toString() +":"+dst_port + " PROTO "+proto+
			" SIZE "+flowSize+" COUNT "+pkgCount+
			" DIRECTION "+direction+" DURATION "+duration+"\n";
	}
}
