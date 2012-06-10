package ch.hsr.hapdroid.network;

import java.nio.ByteBuffer;
import java.util.Date;

import android.util.Log;

/**
 * Java representation of the unix time.
 * 
 * This class contains the number of seconds and microseconds 
 * that have elapsed since midnight Coordinated Universal Time 
 * (UTC), January 1, 1970.
 * 
 * @author "Dominik Spengler"
 *
 */
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

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getDate().toLocaleString();
	}

	/**
	 * Get the date representation of the timeval.
	 * 
	 * @return {@link Date} representation of the timeval
	 */
	public Date getDate() {
		return new Date(seconds * 1000 + microseconds / 1000);
	}
	
	/**
	 * 
	 * @return the number of seconds that have elapsed since midnight 
	 * 		Coordinated Universal Time (UTC), January 1, 1970.
	 */
	public long getSeconds() {
		return seconds;
	}

	/**
	 * 
	 * @return the number of microseconds that have elapsed in addition
	 * 		to the seconds elapsed since midnight Coordinated Universal 
	 * 		Time (UTC), January 1, 1970.
	 */
	public long getMicroseconds() {
		return microseconds;
	}

	/**
	 * Convert the timeval to a byte array.
	 * 
	 * @return byte array as used for the cflow format
	 */
	public byte[] getByteArrayMs() {
		//TODO don't use long
		long ms = microseconds/1000;
		ms += seconds*1000;
		byte[] b = ByteBuffer.allocate(8).putLong(ms).array();
		
		return reverse(b);
	}

	/**
	 * Creates a new Timeval instance out of the values in the byte array.
	 * 
	 * @param flowdata byte array containing the timeval data
	 * @param start_index of the byte array where the timeval data starts
	 * @param length length of the data stored insid the byte array
	 * @return Timeval created from the data in the array
	 */
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

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Timeval another) {
		long diff = getSeconds() - another.getSeconds();
		if (diff == 0)
			diff = getMicroseconds() - another.getMicroseconds();
		return (int) diff;
	}

	/**
	 * Returns a new instance of timeval with subtrahend substracted
	 * from minuend.
	 * 
	 * @param minuend
	 * @param subtrahend
	 * @return a new Timeval instance with the difference of minuend 
	 * 		and subtrahend
	 */
	public static Timeval getDifference(Timeval minuend, Timeval subtrahend) {
		return new Timeval(minuend.getSeconds() - subtrahend.getSeconds(),
				minuend.getMicroseconds() - subtrahend.getMicroseconds());
	}

	/**
	 * Add other to this timeval.
	 * 
	 * @param other
	 */
	public void add(Timeval other) {
		seconds += other.getSeconds(); 
		microseconds += other.getMicroseconds();
	}
}
