package ch.hsr.hapdroid;

import android.net.LocalServerSocket;

/**
 * This class provides an interface to the HAPviz C++ library.
 * 
 * Currently only a high level interface is provided.
 * 
 * @author "Dominik Spengler"
 *
 */
public class HAPvizLibrary {
	static {
		System.loadLibrary("boost_regex");
		System.loadLibrary("boost_thread");
		System.loadLibrary("boost_iostreams");
		System.loadLibrary("hapviz");
		System.loadLibrary("hapvizwrapper");
	}
	
	/**
	 * Process transactions from the given file.
	 * 
	 * Passes all the parameters to the library. The library will:
	 * <ul>
	 * <li>Open the input file
	 * <li>Process the transactions according to ip, netmask
	 * <li>Provide the results over a local server socket to the server listening
	 * to the local server name
	 * 
	 * @param in_filename absolute path to input file
	 * @param localServName local server name used for inter process communication
	 * @param ipAddress the source ip address used for the transactions
	 * @param netmask the netmask used for the transactions
	 * 
	 * @return true if the transactions successfully have been processed,
	 * 		false otherwise
	 * @see LocalServerSocket
	 */
	static native boolean getTransactions(String in_filename, String localServName, String ipAddress, String netmask);
	
	/**
	 * Process transactions from the cflow data.
	 * 
	 * Passes all the parameters to the library. The library will:
	 * <ul>
	 * <li>Process the cflow data
	 * <li>Generate the transactions from the cflow data
	 * <li>Provide the results over a local server socket to the server listening
	 * to the local server name
	 * 
	 * @param cflows byte array containing the uncompressed cflow data
	 * @param localServName local server name used for inter process communication
	 * 
	 * @return true if the transactions successfully have been processed,
	 * 		false otherwise
	 * @see LocalServerSocket
	 */
	static native boolean getTransactions(byte[] cflows, String localServName);
}
