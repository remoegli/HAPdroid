package ch.hsr.hapdroid.network;

import java.util.Vector;

public class TransactionTable {
	
	private Vector<String> mTransactionList;

	public TransactionTable() {
		mTransactionList = new Vector<String>();
	}

	public void add(String trans){
		mTransactionList.add(trans);
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		Object[] array = mTransactionList.toArray();
		for (Object o : array){
			result.append(", [" + o + "]");
		}
		return result.toString();
	}
}
