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

	public Timeval(Timeval other) {
		this(other.getSeconds(), other.getMicroseconds());
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
		long ms = microseconds/1000;
		ms += seconds*1000;
		byte[] b = ByteBuffer.allocate(8).putLong(ms).array();
		
		return reverse(b);
	}

	public static Timeval getFromByteArray(byte[] flowdata, int start_index, int length) {
		byte[] array = new byte[length];
		System.arraycopy(flowdata, start_index, array, 0, length);
		
		long ms = getByteArrayLong(array);
		Log.d(LOG_TAG, "parsed timeval ms: "+ Long.toString(ms));
		long seconds = ms/1000;
		long microseconds = (ms*1000) - (seconds*1000000);
		return new Timeval(seconds, microseconds);
	}

	private static long getByteArrayLong(byte[] array) {
		long value = 0;
		for (int i = 0; i < array.length; i++){
		   value += ((long) array[i] & 0xff) << (8 * i);
		}
		return value;
	}

	private static byte[] reverse(byte[] b) {
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

	public void add(Timeval other) {
		seconds += other.getSeconds(); 
		microseconds += other.getMicroseconds();
	}
}
