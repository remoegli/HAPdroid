package ch.hsr.hapdroid.network;

import java.nio.ByteBuffer;
import java.util.Date;

import android.util.Log;

public class Timeval implements Comparable<Timeval> {

	private static final String LOG_TAG = "Timeval";
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

	//TODO don't use long
	public byte[] getByteArrayMs() {
		long ms = Math.round((double)microseconds/1000);
		ms += seconds*1000;
		byte[] b = ByteBuffer.allocate(8).putLong(ms).array();
		Log.d(LOG_TAG, Long.toHexString(ms));
		
		return reverse(b);
	}

	private byte[] reverse(byte[] b) {
		int i = 0;
		int j = b.length - 1;
		byte tmp;
		while (j > i) {
			tmp = b[j];
			b[j] = b[i];
			b[i] = tmp;
			j--;
			i++;
		}
		return b;
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
