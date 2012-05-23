package ch.hsr.hapdroid.test.android;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HAPvizLibraryTest {

	@Before
	public void initialize(){
	}
	
	@Test
	public void testGetTransactionsByteArrayString() {
		assertNotNull(this.getClass().getResource("files/test"));
	}
	
	
	@After
	public void cleanup(){
		
	}
}
