package ch.hsr.hapdroid;


public class HAPvizLibrary {
	static {
		System.loadLibrary("boost_regex");
		System.loadLibrary("boost_thread");
		System.loadLibrary("boost_iostreams");
		System.loadLibrary("hapviz");
		System.loadLibrary("hapvizwrapper");
	}
	
	static native boolean getTransactions(String in_filename, String localServSocket, String ipAddress, String netmask);
}
