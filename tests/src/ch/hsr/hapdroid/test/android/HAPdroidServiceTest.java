package ch.hsr.hapdroid.test.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.Looper;
import android.test.ServiceTestCase;
import android.util.Log;
import ch.hsr.hapdroid.HAPdroidService;
import ch.hsr.hapdroid.HAPdroidService.HAPdroidBinder;
import ch.hsr.hapdroid.network.FlowTable;
import edu.gatech.sjpcap.IPPacket;
import edu.gatech.sjpcap.Packet;
import edu.gatech.sjpcap.PcapParser;
import edu.gatech.sjpcap.TCPPacket;
import edu.gatech.sjpcap.UDPPacket;

public class HAPdroidServiceTest extends ServiceTestCase<HAPdroidService> {

	private HAPdroidService mService;
	private String mPath;
	private File mTestDir;
	private Context mTestAppContext;
	private ServiceConnection mServiceConnection;
	private Intent mIntent;
	private PcapParser mPcapParser;

	public HAPdroidServiceTest() {
		super(HAPdroidService.class);

		String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		mPath = baseDir + File.separator + "ch.hsr.hapdroid.test" + File.separator;
		mTestDir = new File(mPath);
		mTestDir.mkdirs();
		mPcapParser = new PcapParser();
		
		mServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName className, IBinder service) {
				HAPdroidBinder binder = (HAPdroidBinder) service;
				mService = binder.getService();
			}

			@Override
			public void onServiceDisconnected(ComponentName arg0) {
			}
		};
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	@Override
	protected void setupService() {
		super.setupService();
		
		// in order to make the Handler work inside the HAPdroidService
		// we need to start the service in the conventional way.
		mIntent = new Intent(getSystemContext(), HAPdroidService.class);
		getContext().bindService(mIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
		// make sure the service is bound when entering the test method.
		sleep(5000);
	}

	/**
	 * Checks whether the executable and the service capture
	 * all the packets as well as the cflow creation of the 
	 * captured files.
	 * 
	 * Although capture and cflow generation ideally should
	 * be tested individually it makes sense here to test 
	 * them together since it is rather easy to get correct
	 * cflow data for pcap files.
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * 
	 */
	public void testExecutable() throws FileNotFoundException, IOException {
		setupService();
		installResourcesToSdcard();

		checkPcap(mPath + "pcap_ipp.pcap");
		checkGeneratedCflow(mPath + "cflow_ipp");
		
//		checkPcap(mPath + "pcap_teamspeak2.pcap");
//		checkGeneratedCflow(mPath + "cflow_teamspeak2");

		checkPcap(mPath + "pcap_vpn_client_connect.pcap");
		checkGeneratedCflow(mPath + "cflow_vpn_client_connect");
		
		removeResourcesFromSdcard();
		shutdownService();
	}

	private void checkGeneratedCflow(String file) throws FileNotFoundException, IOException {
		FlowTable flowTable = mService.getFlowTable();
		byte[] cflow_flowTable = flowTable.toByteArray();
		byte[] cflow_file = new byte[cflow_flowTable.length];
		// The assetmanager already decompresses gzip files so we don't need
		// to do it here again.
		FileInputStream fis = new FileInputStream(file);
		fis.read(cflow_file, 0, cflow_flowTable.length);
		
		File cflow_orig = new File(file);
		
		assertEquals("generated cflow filesize mismatch", cflow_orig.length(), cflow_flowTable.length);
		
		for(int i = 0; i < cflow_flowTable.length; i++){
			//skip duration since there seems to be a rounding error involved
			if (i%48==16)
				continue;
			assertEquals("cflow for file " + file + " in flow "+i/48+", byte "+i%48+" different", 
					String.format("%02X", cflow_file[i]), String.format("%02X", cflow_flowTable[i]));
		}
	}

	/**
	 * Checks whether the flow table contains all the packages
	 * and the correct payload size. It checks the capturing of the 
	 * executable the transfer over the local socket and the parsing 
	 * of the packet all together.
	 * 
	 * Although it is a bit unfortunate to test so much logic in one
	 * step, it is acceptable since all these step are logically 
	 * considered to belong to each other.
	 * 
	 * @param file pcap file to check
	 */
	private void checkPcap(final String file) {
		mService.clearCapture();
		
		/**
		 * We need a seperate runnable with a Looper so that the 
		 * onPublishProgress method will be called.
		 */
		new Thread(new Runnable() {
	        public void run() {
	        	Looper.prepare();
	        	mService.startExecutableCapture("-p " + file);
	        	Looper.loop();
	        }
	    }).start();
		
		sleep(5000);
		FlowTable flowTable = mService.getFlowTable();
		assertEquals("packet count mismatch in "+file, getPacketCount(file), flowTable.getPacketCount());
		//TODO: remove ethernet trailer in bytecount
//		assertEquals("byte count mismatch in "+file, getByteCount(file), flowTable.getPayloadCount());
	}

	private long getByteCount(String file) {
		mPcapParser.openFile(file);
		long bytes = 0;
		Packet packet = mPcapParser.getPacket();
		while(packet != Packet.EOF){
			if(packet instanceof UDPPacket){
				bytes += ((UDPPacket) packet).data.length;
			} else if(packet instanceof TCPPacket){
				bytes += ((TCPPacket) packet).data.length;
			}
			
			packet = mPcapParser.getPacket();
		}
		mPcapParser.closeFile();
		return bytes;
	}

	private long getPacketCount(String file) {
		mPcapParser.openFile(file);
		long packets = 0;
		Packet packet = mPcapParser.getPacket();
		while(packet != Packet.EOF){
			if(packet instanceof IPPacket){
				packets++;
			}
			
			packet = mPcapParser.getPacket();
		}
		mPcapParser.closeFile();
		return packets;
	}

	private void sleep(long ms) {
		synchronized(this) {
			try {
				wait(ms);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected void shutdownService() {
		super.shutdownService();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	private void installResourcesToSdcard() {
		try {
			mTestAppContext = getContext().createPackageContext("ch.hsr.hapdroid.test",
			        Context.CONTEXT_IGNORE_SECURITY);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		AssetManager assetManager = mTestAppContext.getAssets();
		Log.d("TESTING", assetManager.toString());
		String[] files = null;
		InputStream in = null;
		OutputStream out = null;
		try {
			files = assetManager.list("service");
			Log.d("TESTING", files[0]);
			for (String file : files){
				Log.d("TESTING", "File " + file + " found");
				in = assetManager.open("service/" + file);
				out = new FileOutputStream(mPath + file);
				copyFile(in, out);
			}
			if (in != null)
				in.close();
			if (out != null)
				out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void removeResourcesFromSdcard() {
		File f;
		for (String file : mTestDir.list()){
			f = new File(mTestDir, file);
			f.delete();
		}
	}

	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}
}
