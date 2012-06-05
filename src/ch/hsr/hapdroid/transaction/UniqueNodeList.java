package ch.hsr.hapdroid.transaction;


public class UniqueNodeList<T> extends NodeList<T> {
	
	@Override
	protected boolean isSummarized(Node<T> node) {
		return true;
	}
}
