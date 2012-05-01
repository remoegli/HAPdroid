package ch.hsr.hapdroid.network;

import java.math.BigInteger;
import java.util.Date;

public class Timeval implements Comparable<Timeval> {

	private long seconds;
	private long microseconds;

	public Timeval(long seconds, long microseconds) {
		this.seconds = seconds;
		this.microseconds = microseconds;
	}

	@Override
	public String toString() {
		return getDate().toLocaleString();
	}

	public Date getDate() {
		return new Date(seconds * 1000 + microseconds / 1000);
	}
	
	public long getSeconds() {
		return seconds;
	}

	public long getMicroseconds() {
		return microseconds;
	}

	public byte[] getByteArrayMs() {
		BigInteger ms = BigInteger.valueOf(microseconds/1000);
		ms.add(BigInteger.valueOf(seconds*1000));
		return ms.toByteArray();
	}

	@Override
	public int compareTo(Timeval another) {
		long diff = getSeconds() - another.getSeconds();
		if (diff == 0)
			diff = getMicroseconds() - another.getMicroseconds();
		return (int) diff;
	}

	public static Timeval getDifference(Timeval minuend, Timeval subtrahend) {
		return new Timeval(minuend.getSeconds() - subtrahend.getSeconds(),
				minuend.getMicroseconds() - subtrahend.getMicroseconds());
	}
}
