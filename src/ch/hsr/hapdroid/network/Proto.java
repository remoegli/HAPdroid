package ch.hsr.hapdroid.network;

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
