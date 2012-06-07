package ch.hsr.hapdroid.network;

/**
 * Enumeration providing the capture source.
 * 
 * The CaptureSource enumeration describes the origin of every
 * captured packet.
 * 
 * @author "Dominik Spengler"
 *
 */
public enum CaptureSource {
	/**
	 * WLAN data source
	 */
	WLAN,
	/**
	 * Mobile data source
	 */
	MOBILE,
	/**
	 * pcap file data source
	 */
	PCAP,
	/**
	 * unknown data source
	 */
	UNKNOWN
}
