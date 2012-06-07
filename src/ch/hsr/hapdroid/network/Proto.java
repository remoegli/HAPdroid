package ch.hsr.hapdroid.network;

/**
 * Enumeration representing the supported network protocols.
 * 
 * This enumeration is used to convert the protocol numbers
 * as received from the network to its string representation.
 * 
 * @author "Dominik Spengler"
 *
 */
public enum Proto {
	TCP {
		@Override
		public String toString() {
			return "TCP";
		}
	},
	UDP {
		@Override
		public String toString() {
			return "UDP";
		}
	},
	ICMP {
		@Override
		public String toString() {
			return "ICMP";
		}
	}, UNKNOWN;
	
	/**
	 * Get the Proto enum from the network protocol number.
	 * 
	 * @param value network protocol number
	 * @return Proto enum
	 */
	public static Proto get(int value){
		switch (value){
		case 1:
			return ICMP;
		case 6:
			return TCP;
		case 17:
			return UDP;
		}
		return UNKNOWN;
	}
}
