/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mobilegt.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import com.mobilegt.demo.R;
import com.mobilegt.demo.R.string;
import com.mobilegt.utils.Constants;
import com.mobilegt.vpn.ToyVpnAESOperator;

@SuppressLint("NewApi")
public class ToyVpnService extends VpnService implements Handler.Callback,
		Runnable {
	private static final String TAG = "ToyVpnService";

	private String mServerAddress;
	private String mServerPort;
	private byte[] mSharedSecret;
	private PendingIntent mConfigureIntent;

	private static Handler mHandler;
	private static Thread mThread;

	private ParcelFileDescriptor mInterface;
	private String mParameters;
	public static boolean stopVpnFlag = false,flag=false;

	private double inBytes;
	private double outBytes;
	private double inPackets;
	private double outPackets;
	private Intent intentt;
	private int stid,fg;
	int STOP_SERVICE = 0;
	MyReceiver receiver;

	@Override
	public void onCreate() {
//		int NOTIFICATION_ID = 1;
//		// Create an Intent that will open the main Activity
//		// if the notification is clicked.
//
//		Log.i("oncreate","inoncreate mobilegt");
//		
//		 super.onCreate();
////	        @SuppressWarnings(deprecation)
//	        Notification notification=new Notification(R.drawable.ic_launcher,
//	                "Notification comes",System.currentTimeMillis());
//	        Intent notificationIntent=new Intent(this,MainActivity.class);
//	        PendingIntent pendingIntent=PendingIntent.getActivity(this, 0,notificationIntent,
//	                0);
//	        notification.setLatestEventInfo(this, "this is title", "this is content",
//	                pendingIntent);
//	        startForeground(1, notification);
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// The handler is only used to show messages.
		IntentFilter filter = new IntentFilter();// 创建IntentFilter对象
		// 注册一个广播，用于接收Activity传送过来的命令，控制Service的行为，如：发送数据，停止服务等
		filter.addAction("android.net.vpn");
		// 注册Broadcast Receiver
		registerReceiver(receiver, filter);
		intentt = intent;
		stid=startId;
		fg=flags;
		
Log.i("onstart","in on startcomand mobilegt");
		if (mHandler == null) {
			mHandler = new Handler(this);
		}

		// Stop the previous session by interrupting the thread.
		if (mThread != null) {
			mThread.interrupt();
		}

		// Extract information from the intent.
		String prefix = getPackageName();
//		mServerAddress = intent.getStringExtra(prefix + ".ADDRESS");
//		mServerPort = intent.getStringExtra(prefix + ".PORT");
//		mSharedSecret = intent.getStringExtra(prefix + ".SECRET").getBytes();
		mServerAddress = Constants.VPN_SERVER;
		mServerPort = Constants.VPN_PORT;
		mSharedSecret = Constants.SHARED_SECRET.getBytes();
		// Start a new session by creating a new thread.
		mThread = new Thread(this, "ToyVpnThread");
		mThread.start();

		return  START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
//		mHandler.sendEmptyMessage(R.string.disconnect);
		if (mThread != null) {
			mThread.interrupt();
		}
		stopForeground(true);
	}

	@Override
	public boolean handleMessage(Message message) {
		if (message != null) {
			Toast.makeText(this, message.what, Toast.LENGTH_SHORT).show();
		}
		return true;
	}

	@Override
	public synchronized void run() {
		try {
			Log.i(TAG, "Starting mobilegt");

			// If anything needs to be obtained using the network, get it now.
			// This greatly reduces the complexity of seamless handover, which
			// tries to recreate the tunnel without shutting down everything.
			// In this demo, all we need to know is the server address.
			InetSocketAddress server = new InetSocketAddress(mServerAddress,
					Integer.parseInt(mServerPort));
//			Log.i("address", mServerAddress + "mobilegt");
			// We try to create the tunnel for several times. The better way
			// is to work with ConnectivityManager, such as trying only when
			// the network is avaiable. Here we just use a counter to keep
			// things simple.
			for (int attempt = 0; attempt < 1000; ++attempt) {
				if (!stopVpnFlag) {
					flag=false;
					mHandler.sendEmptyMessage(R.string.connecting);
					// Reset the counter if we were connected.
					if (run(server)) {
						attempt = 0;
					}
					
					// Sleep for a while. This also checks if we got
					// interrupted.
					Thread.sleep(3000);
					Log.i("attempt:",attempt+"mobilegt");
				}
			}
			Log.i(TAG, "Giving up");
		} catch (Exception e) {
			Log.e(TAG, "Got " + e.toString() + "mobilegt");
		} finally {
			try {
				mInterface.close();
			} catch (Exception e) {
				// ignore
			}
			mInterface = null;
			mParameters = null;

			Intent intent = new Intent();
			intent.putExtra("computeduration", false);
			// intent.putExtra("connectedTime", System.currentTimeMillis());
			intent.setAction("android.net.VpnClient");
			sendBroadcast(intent);
			flag=true;
//			mHandler.sendEmptyMessage(R.string.disconnecteddd);
			// unregisterReceiver(receiver);
			Log.i(TAG, "Exiting");
		}
	}

	private boolean run(InetSocketAddress server) throws Exception {
		DatagramChannel tunnel = null;
		boolean connected = false;
		try {
			// Create a DatagramChannel as the VPN tunnel.
			tunnel = DatagramChannel.open();

			// Protect the tunnel before connecting to avoid loopback.
			if (!protect(tunnel.socket())) {
				throw new IllegalStateException("Cannot protect the tunnel");
			}
			Log.i("address",server.getAddress().getHostAddress() +":"+server.getPort()+" mobilegt");
			// Connect to the server.
			try{
				if(tunnel.isConnected()) {
					tunnel.disconnect();
					Log.i(TAG,"tunnel is connected. mobilegt");
				}
			} catch(Exception ex){
				Log.i(TAG,ex.getMessage()+" mobilegt");
			}
			
			tunnel.connect(server);
			Log.i("connectStat",tunnel.isBlocking() + " " + tunnel.isConnected() + " " + tunnel.isOpen() + " " + tunnel.isRegistered() + " mobilegt");
			Log.i("tunnelAddress",tunnel.socket().getLocalSocketAddress().toString() + " " +tunnel.socket().getLocalAddress().getHostAddress() +
					" " + tunnel.socket().getLocalPort() + " mobilegt");
			// For simplicity, we use the same thread for both reading and
			// writing. Here we put the tunnel into non-blocking mode.
			tunnel.configureBlocking(false);

			// Authenticate and configure the virtual network interface.
			handshake(tunnel);
			Log.i("attempt:","connecting mobilegt");
			// Now we are connected. Set the flag and show the message.
			connected = true;
			
//			mHandler.sendEmptyMessage(R.string.connected);
			// ToyVpnClient.connected = true;
			Intent intent = new Intent();
			intent.putExtra("connectedTime", System.currentTimeMillis());
			intent.putExtra("computeduration", true);
			intent.setAction("android.net.VpnClient");
			sendBroadcast(intent);

			// Packets to be sent are queued in this input stream.
			FileInputStream in = new FileInputStream(
					mInterface.getFileDescriptor());

			// Packets received need to be written to this output stream.
			FileOutputStream out = new FileOutputStream(
					mInterface.getFileDescriptor());

			// Allocate the buffer for a single packet.
			ByteBuffer packet = ByteBuffer.allocate(2048);
			ByteBuffer encryptedPacket = ByteBuffer.allocate(2048);
			// ByteBuffer decryptedPacket = ByteBuffer.allocate(2048);
			// We use a timer to determine the status of the tunnel. It
			// works on both sides. A positive value means sending, and
			// any other means receiving. We start with receiving.
			int timer = 0;
			Log.i("timer", "initial timer teacher");
			ToyVpnAESOperator aesOperator = ToyVpnAESOperator.getInstance();

			// We keep forwarding packets till something goes wrong.
			while (true) {
				// Assume that we did not make any progress in this iteration.
				boolean idle = true;

				// Read the outgoing packet from the input stream.
				int length = in.read(packet.array());
				if (length > 0) {
					// Write the outgoing packet to the tunnel.
					packet.limit(length);
					// Log.i("inread",packet.array()+"vpn");

					// 加密
					long lStart = System.currentTimeMillis();
					byte[] plainBytes = new byte[length + 4];
					packet.get(plainBytes, 4, length);
					byte[] lb = ToyVpnAESOperator.intToByteArray(length);
					for (int i = 0; i < 4; i++)
						plainBytes[i] = lb[i];
					byte[] encryptedBytes = aesOperator.encrypt(plainBytes);// 加密
					long lUseTime = System.currentTimeMillis() - lStart;
					encryptedPacket.put((byte) 1);
					encryptedPacket.put(encryptedBytes);
					encryptedPacket.flip();
					encryptedPacket.limit(encryptedBytes.length + 1);
//					Log.i("pktinfo",
//							"加密耗时："
//									+ lUseTime
//									+ "毫秒 vpn length(原始报文长-加密前报文长-加密报文长-加密报文缓冲池大小):"
//									+ length + "--" + plainBytes.length + "--"
//									+ encryptedBytes.length + "--"
//									+ encryptedPacket.array().length);
					tunnel.write(encryptedPacket);// 发送加密报文
					// tunnel.write(packet);
					// Log.i("writesend plain",
					// ToyVpnAESOperator.bytes2hex(plainBytes) + " vpn");
					// Log.i("writesend encrypted",
					// ToyVpnAESOperator.bytes2hex(encryptedPacket.array()) +
					// " vpn");
					packet.clear();
					encryptedPacket.clear();

					// There might be more outgoing packets.
					idle = false;

					// If we were receiving, switch to sending.
					if (timer < 1) {
						timer = 1;
					}
				}

				// Read the incoming packet from the tunnel.
				// Log.i("outreadbefore",packet.array()+"vpn");
				length = tunnel.read(packet);
				// Log.i("outreadafter",packet.array()+"vpn");
				if (length > 0) {
					// Ignore control messages, which start with zero.
					if (packet.get(0) != 0) {
						// Write the incoming packet to the output stream.
						// Log.i("outread",packet.array()+"vpn");
						// 解密
						long lStart = System.currentTimeMillis();
						byte[] encryptedBytes = new byte[length - 1];
						for (int i = 0; i < encryptedBytes.length; i++)
							encryptedBytes[i] = packet.get(i + 1);
						byte[] decryptedBytes = aesOperator
								.decrypt(encryptedBytes);// 解密
						byte[] lb = new byte[4];
						for (int i = 0; i < 4; i++)
							lb[i] = decryptedBytes[i];
						int datalength = ToyVpnAESOperator
								.byteArrayToInt(lb, 0);
						// byte[] decryptedPackets=new
						// byte[decryptedBytes.length-4];
						// for(int i=0;i<decryptedPackets.length;i++)
						// decryptedPackets[i]=decryptedBytes[i+4];
						long lUseTime = System.currentTimeMillis() - lStart;
//						Log.i("outwrite",
//								"解密耗时："
//										+ lUseTime
//										+ " 毫秒 vpn length(接收报文长度-解密前长度-长度解密后长度-数据报文长度):"
//										+ length + "--" + encryptedBytes.length
//										+ "--" + decryptedBytes.length + "--"
//										+ datalength);
						// Log.i("outwrite decrypted: ","数据长度: "+datalength+" "+ToyVpnAESOperator.bytes2hex(packet.array())+" vpn");
						// Log.i("outwrite decrypted: ","数据长度: "+datalength+" "+ToyVpnAESOperator.bytes2hex(decryptedBytes)+" vpn");

						out.write(decryptedBytes, 4, datalength);// 发送明文报文
					}
					packet.clear();

					// There might be more incoming packets.
					idle = false;

					// If we were sending, switch to receiving.
					if (timer > 0) {
						timer = 0;
					}
				}

				// If we are idle or waiting for the network, sleep for a
				// fraction of time to avoid busy looping.
				if (idle) {
					Thread.sleep(100);
					// Log.i("timer",timer+"teacher");
					// Increase the timer. This is inaccurate but good enough,
					// since everything is operated in non-blocking mode.
					timer += (timer > 0) ? 100 : -100;

					// We are receiving for a long time but not sending.
					if (timer < -15000) {
						// Send empty control messages.
						packet.put((byte) 0).limit(1);
						for (int i = 0; i < 3; ++i) {
							packet.position(0);
							tunnel.write(packet);
						}
						packet.clear();

						// Switch to sending.
						timer = 1;
					}

					// We are sending for a long time but not receiving.
					if (timer > 20000) {
//						Intent mService = new Intent(getApplicationContext(), ToyVpnService.class);
//						startService(mService); 
//						Intent intentt = new Intent(Intent.ACTION_MAIN);
//						intentt.addCategory(Intent.CATEGORY_LAUNCHER);
//						intentt.setClass(this, MainActivity.class);
//						intentt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//						startActivity(intentt); 
						throw new IllegalStateException("Timed out");
					
					}

					if (stopVpnFlag) {
						throw new InterruptedException();
					}
				}
			}
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			Log.e(TAG, "Got " + e.toString()+"in run mobilegt");
			
		} finally {
			try {
				tunnel.close();
			} catch (Exception e) {
				// ignore
				Log.e(TAG, "Got " + e.toString()+"in finally mobilegt");
			}
			
		}
		return connected;
	}

	private void handshake(DatagramChannel tunnel) throws Exception {
		// To build a secured tunnel, we should perform mutual authentication
		// and exchange session keys for encryption. To keep things simple in
		// this demo, we just send the shared secret in plaintext and wait
		// for the server to send the parameters.

		// Allocate the buffer for handshaking.
		ByteBuffer packet = ByteBuffer.allocate(2048);

		// Control messages always start with zero.
		// packet.put((byte) 0).put(mSharedSecret).flip();
		packet.put((byte) 0).put(mSharedSecret).put((byte) ':')
				.put(getDeviceId().getBytes()).flip();
//		Log.i("pkt",packet+"mobilegt");
		// Send the secret several times in case of packet loss.
		for (int i = 0; i < 3; ++i) {
			packet.position(0);
			tunnel.write(packet);
		}
		packet.clear();

		// Wait for the parameters within a limited time.
		for (int i = 0; i < 50; ++i) {
			Thread.sleep(100);

			// Normally we should not receive random packets.
			int length = tunnel.read(packet);
			if (length > 0 && packet.get(0) == 0) {
				configure(new String(packet.array(), 1, length - 1).trim());
				return;
			}
//			Log.i("handshake","watfor"+i+"mobilegt");
		}
		throw new IllegalStateException("Timed out in handshake ");

	}

	private void configure(String parameters) throws Exception {
		// If the old interface has exactly the same parameters, use it!
		if (mInterface != null && parameters.equals(mParameters)) {
			Log.i(TAG, "Using the previous interface");
			return;
		}

		// Configure a builder while parsing the parameters.
		Builder builder = new Builder();
		for (String parameter : parameters.split(" ")) {
			String[] fields = parameter.split(",");
			try {
				switch (fields[0].charAt(0)) {
				case 'm':
					builder.setMtu(Short.parseShort(fields[1]));
					break;
				case 'a':
					builder.addAddress(fields[1], Integer.parseInt(fields[2]));
					break;
				case 'r':
					builder.addRoute(fields[1], Integer.parseInt(fields[2]));
					break;
				case 'd':
					builder.addDnsServer(fields[1]);
					break;
				case 's':
					builder.addSearchDomain(fields[1]);
					break;
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("Bad parameter: "
						+ parameter);
			}
		}

		// Close the old interface since the parameters have been changed.
		try {
			mInterface.close();
		} catch (Exception e) {
			// ignore
		}

		// Create a new interface using the builder and save the parameters.
		mInterface = builder.setSession(mServerAddress)
				.setConfigureIntent(mConfigureIntent).establish();
		mParameters = parameters;
		Log.i(TAG, "New interface: " + parameters);
	}

	private String getDeviceId() {
		String SerialNumber = android.os.Build.SERIAL;
//		Log.i("TAG", SerialNumber + "teacher");
		return SerialNumber;
	}

	public static class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			Bundle bundle = intent.getExtras();
			stopVpnFlag = bundle.getBoolean("flag");
			if (stopVpnFlag) {// 如果发来的消息是停止服务
			// mHandler.sendEmptyMessage(R.string.disconnectedd);

			}
		}
	}

	public void DisplayToast(String str) {
		Toast toast = Toast.makeText(this, str, Toast.LENGTH_LONG);

		// 显示该Toast
		toast.show();
	}
}
