package ch.hsr.hapdroid;

public class NetworkCapture {
	
	static {
		System.loadLibrary("netcapture");
	}
	
	public static native void startCapture();

	public static native String getResultString();
	
	public static native void stopCapture();
}
