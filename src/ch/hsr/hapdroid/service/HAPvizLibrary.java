package ch.hsr.hapdroid.service;


public class HAPvizLibrary {
	static {
		System.loadLibrary("boost_regex");
		System.loadLibrary("boost_thread");
		System.loadLibrary("boost_iostreams");
		System.loadLibrary("hapviz");
		System.loadLibrary("hapvizwrapper");
	}
	
	static native boolean getTransactions(String in_filename, String localServName, String ipAddress, String netmask);
	static native boolean getTransactions(byte[] cflows, String localServName);
}
