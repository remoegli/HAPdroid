package ch.hsr.hapdroid.network;

import java.net.InetAddress;
import java.net.UnknownHostException;

import android.util.Log;

public class Packet {
	public static final short IPPROTO_ICMP = 1;
	public static final short IPPROTO_TCP = 6;
	public static final short IPPROTO_UDP = 17;
	
	private static final String LOG_TAG = "Packet";
	
	public InetAddress src_addr;
	public int src_port;
	public InetAddress dst_addr;
	public int dst_port;
	public byte proto;
	public byte tos;
	public int payload_size;
	public Timeval timestamp;
	
	@Override
	public String toString() {
		return timestamp.toString() + 
				src_addr.toString() +":"+src_port + " TO "+
				dst_addr.toString() +":"+dst_port + " PROTO "+proto+" SIZE "+payload_size+"\n";
	}
	
	public static final Packet parsePacket(String packet){
		String[] tokens = packet.split(":|-->|,");
		Packet p = new Packet();
		
		try {
			p.src_addr = InetAddress.getByName(tokens[2]);
			p.dst_addr = InetAddress.getByName(tokens[4]);
		} catch (UnknownHostException e) {
			Log.e(LOG_TAG, "could not parse InetAddress");
			e.printStackTrace();
			return null;
		}
		
		p.proto = Byte.parseByte(tokens[1], 10);
		p.tos = Byte.parseByte(tokens[6], 10);
		p.src_port = Integer.parseInt(tokens[3], 10);
		p.dst_port = Integer.parseInt(tokens[5], 10);
		p.payload_size = Integer.parseInt(tokens[7], 10);
		
		long seconds = Long.parseLong(tokens[8], 10);
		long microseconds = Long.parseLong(tokens[9], 10);
		p.timestamp = new Timeval(seconds, microseconds);
		
		return p;
	}
}
