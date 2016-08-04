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

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.mobilegt.collect.catchApp;
import com.mobilegt.collect.catchSockets;
import com.mobilegt.demo.R;
import com.mobilegt.demo.R.id;
import com.mobilegt.demo.R.layout;
import com.mobilegt.upload.fileUpload;
import com.mobilegt.utils.CmdUtils;
import com.mobilegt.utils.Constants;
import com.mobilegt.utils.FtpUtil;
import com.mobilegt.utils.fileManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

@SuppressLint("NewApi")
public class ToyVpnClient extends Activity implements View.OnClickListener {
	private TextView mServerAddress;
	private TextView mServerPort;
	private TextView mSharedSecret;
	private TextView m_TextView, timeview;
	RadioGroup m_RadioGroup;
	RadioButton m_Radio1, m_Radio2, m_Radio3;
	private String serverAddress = "54.255.191.152";
	private String port = "8000";
	private String secret = "test0";
	private Handler handler;
	private static long connectedTime;
	// private static long connectedtime;
	public static boolean computeduration = false;
	private MyReceiver receiver = null;
	Button connect, disconnect;
	Thread thread, thd,upthd;
	public static Boolean runflag = false;
	catchApp ctApp;
	catchSockets ctSocket;
	public static String filePath, outfilePath, appPath;
	String recordeFileName;
	
	CmdUtils cmd = new CmdUtils();// 实例化cmd对象,用来执行cmd命令
	// CmdPcap cmd1 = new CmdPcap(this);// 实例化pcap抓取对象
	FtpUtil ftp;

	SharedPreferences preferences;
	SharedPreferences.Editor editor;

	// private Intent intent;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.form);
		String deviceid = new CmdUtils().getDeviceId();
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		Calendar calendar = Calendar.getInstance();
		recordeFileName = deviceid + df.format(calendar.getTime());

		filePath = "/sdcard/android/data/" + this.getPackageName() + "/";
		outfilePath = "/sdcard/android/data/" + this.getPackageName()
				+ "/files/";
		appPath = getApplicationContext().getFilesDir().getAbsolutePath();

		ctApp = new catchApp(outfilePath, getPackageManager());
		ctSocket = new catchSockets(recordeFileName);

		// mServerAddress = (TextView) findViewById(R.id.address);
		// mServerPort = (TextView) findViewById(R.id.port);
		// mSharedSecret = (TextView) findViewById(R.id.secret);
		connect = (Button) findViewById(R.id.connect);
		disconnect = (Button) findViewById(R.id.disconnect);
		findViewById(R.id.connect).setOnClickListener(this);
		findViewById(R.id.disconnect).setOnClickListener(this);
		m_TextView = (TextView) findViewById(R.id.TextView01);
		m_RadioGroup = (RadioGroup) findViewById(R.id.RadioGroup01);
		m_Radio1 = (RadioButton) findViewById(R.id.RadioButton1);
		m_Radio2 = (RadioButton) findViewById(R.id.RadioButton2);
		m_Radio3 = (RadioButton) findViewById(R.id.RadioButton3);
		//m_Radio2.setEnabled(false);
		receiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.VpnClient");
		this.registerReceiver(receiver, filter);

		/* 设置事件监听 */
		m_RadioGroup
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						// TODO Auto-generated method stub

						if (checkedId == m_Radio1.getId()) {
							serverAddress = "54.255.191.152";
//							DisplayToast(m_Radio1.getText() + " is selected");
						} else if (checkedId == m_Radio2.getId()) {
							serverAddress = "115.28.91.223";
//							DisplayToast(m_Radio2.getText() + " is selected");
						} else if (checkedId == m_Radio3.getId()) {
							serverAddress = "10.81.10.250";
//							DisplayToast(m_Radio3.getText() + " is selected");
						}
					}
				});
		Constants.VPN_SERVER = serverAddress;
		timeview = (TextView) findViewById(R.id.timeview);
		handler = new Handler() {
			public void handleMessage(Message msg) {
				timeview.setText((String) msg.obj);
			}
		};
		new Thread(new monitor()).start();

	}

	@Override
	public void onClick(View v) {
		Intent intent;

		switch (v.getId()) {

		case R.id.connect:
			// ToyVpnService.stopVpnFlag = false; // set the flat of stopping
			// vpn
			Log.i("connect", "click connect mobilegt");
			if (!runflag) {
				ctApp.recordApps("uid");
				thread = new Thread(new catchSocket()); // run gt
				thread.start();

				runflag = true;
				thd = new Thread(new gtmonitor()); // monitor the time passed,
													// stop gt every 5 hours
													// and
													// restart
				thd.start();

			}

			intent = VpnService.prepare(this);
			Log.i("info:", "serviceprepare teacher");
			if (intent != null) {
				startActivityForResult(intent, 0);
			} else {
				onActivityResult(0, RESULT_OK, null);
			}

			intent = new Intent();// 创建Intent对象
			intent.setAction("android.net.vpn");
			intent.putExtra("flag", false);
			sendBroadcast(intent);// 发送广播

			// connect.setEnabled(false);
			// disconnect.setEnabled(true);
			break;
		case R.id.disconnect:
			runflag = false;
			// catchP.setText("Socekt数据采集");

			stopGt();

			intent = new Intent();// 创建Intent对象
			intent.setAction("android.net.vpn");
			intent.putExtra("flag", true);
			sendBroadcast(intent);// 发送广播
			// unregisterReceiver(receiver);
			// connect.setEnabled(true);
			// disconnect.setEnabled(false);
			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int request, int result, Intent data) {
		if (result == RESULT_OK) {
			String prefix = getPackageName();
			Log.i("prefix", prefix + " mobilegt");
			Intent intent = new Intent(this, ToyVpnService.class);
			Constants.VPN_SERVER=serverAddress;
			Constants.VPN_PORT=port;
			Constants.SHARED_SECRET=secret;
//					.putExtra(prefix + ".ADDRESS", serverAddress)
//					.putExtra(prefix + ".PORT", port)
//					.putExtra(prefix + ".SECRET", secret);
//			Log.i("ipinformation", "teachermyvpn");
			startService(intent);

//			Intent intent = new Intent(ToyVpnService.ACTION_FOREGROUND);  
//			 intent.setClass(this,  
//			 ForegroundService.class);  
//			 startService(intent);  
			// Intent intent1 = new Intent(this, MainActivity.class);
			// startActivity(intent1);

			// 注册广播接收器

		}
	}

	/* 显示Toast */
	public void DisplayToast(String str) {
		Toast toast = Toast.makeText(this, str, Toast.LENGTH_LONG);
		// 设置toast显示的位置
		toast.setGravity(Gravity.TOP, 0, 220);
		// 显示该Toast
		toast.show();
	}

	public class monitor implements Runnable {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			// Log.i("TAG", "monitor start");
			
			try {
				while (true) {
					SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
					// String str=sdf.format(new Date());
					if (computeduration) {
						long duration = System.currentTimeMillis()
								- connectedTime;
						// handler.sendMessage(handler.obtainMessage(100,
						// duration+""));
						String str = convertDuration2String(duration);
						handler.sendMessage(handler.obtainMessage(100, str));
					} else {
						String str = "0 day 00:00:00";
						// String str = new Date(1024).toString();
						handler.sendMessage(handler.obtainMessage(100, str));

					}
//					}
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private String convertDuration2String(long durationMillis) {
		long day = durationMillis / (24 * 60 * 60 * 1000);
		long hour = (durationMillis - day * (24 * 60 * 60 * 1000))
				/ (60 * 60 * 1000);
		long minute = (durationMillis - day * (24 * 60 * 60 * 1000) - hour * 60 * 60 * 1000)
				/ (60 * 1000);
		long second = (durationMillis - day * (24 * 60 * 60 * 1000) - hour * 60
				* 60 * 1000 - minute * 60 * 1000) / 1000;
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(day + " day " + hour + ":" + minute + ":" + second);
		return sbuf.toString();
	}

	public IBinder onBind(Intent intent) {// 重写onBind方法
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 获取广播数据
	 */
	public class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			long cTime = bundle.getLong("connectedTime");
			connectedTime = cTime;
			boolean computestate = bundle.getBoolean("computeduration");
			// DisplayToast("recive broacast" + computestate);
			computeduration = computestate;
		}
	}

	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);

	}

	public class catchSocket implements Runnable {
		public void run() {
			ctSocket.catchPackage(appPath, outfilePath);
		}
	}

	public void stopGt() {

		try {
			if (thread != null) {
				thread.interrupt();// stop gt thread
			}
			if (thd != null) {
				thd.interrupt();// stop monitor thread
			}
			ToyVpnService.stopVpnFlag = true; // stop vpn
			ToyVpnClient.computeduration = false;

			// long starttime = System.currentTimeMillis();
			Log.i("TAG", "stop GT teacherrr");
			String pid = cmd.execCmdkill("ps | grep gt"); // kill gt process
			// long endtime = System.currentTimeMillis();
			// Log.i("time:", (endtime - starttime) + "teacher");
			String[] idArray = pid.split(" ");
			// Log.i("idArray",idArray[0] + "teacher");
			for (int i = 0; i < idArray.length; i++) {
				cmd.execCmd("kill " + idArray[i]);
			}

			// tx.setText("File：" + filePath
			// + preferences.getString("catchedFileName", null));
			if (CmdUtils.isWifi(this)) {
				// fileUpload up = new fileUpload(this, outfilePath);
				// up.prepareUpload();
				// tx.setText("File：" + "upload success");
				ftp = new FtpUtil(this,msgHandler);
//				ftp.prepareUpload(outfilePath);
				new Thread(new uploadRun()).start();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void TmpStopGt() {
		Log.i("TAG", "stop GT");
		try {
			thread.interrupt();// stop gt
			thd.interrupt(); // stop monitor
			// long starttime = System.currentTimeMillis();
			String pid = cmd.execCmdkill("ps | grep gt");
			// long endtime = System.currentTimeMillis();
			// Log.i("time:", (endtime - starttime) + "teacher");
			String[] idArray = pid.split(" ");
			// Log.i("idArray",idArray[0] + "teacher");
			for (int i = 0; i < idArray.length; i++) {
				cmd.execCmd("kill " + idArray[i]);
			}

			// tx.setText("File：" + filePath
			// + preferences.getString("catchedFileName", null));

			// if (CmdUtils.isWifi(this)) {
			// fileUpload up = new fileUpload(this, outfilePath);
			// up.prepareUpload();
			// // tx.setText("File：" + "upload success");
			// }

			restartGt(); // restart gt

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void restartGt() { // restartgt
		// ctApp.recordApps();

		thread = new Thread(new catchSocket()); // run gt
		thread.start();

		runflag = false;
		thd = new Thread(new monitor()); // set stopflag
		thd.start();

		// catchP.setText("采集中...");
		// catchP.setEnabled(false);
		// stopP.setEnabled(true);
	}

	final private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				TmpStopGt();
				break;

			default:
				break;
			}
		}
	};

	public class gtmonitor implements Runnable {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			// Log.i("TAG", "monitor start");
			// String name = preferences.getString("catchedFileName", null);
			// name = name.substring(0, name.lastIndexOf("."));
			Log.i("recordefile", recordeFileName + "mygt");
//			ctApp.recordApps(recordeFileName);
			
			ctApp.recordApps("uid");
			long start = System.currentTimeMillis();
			long end = System.currentTimeMillis();
			while (runflag) {
				end = System.currentTimeMillis();
				if ((end - start) < 18000000) {
					// if ((end - start) < 60000) {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					runflag = false;
					mHandler.sendEmptyMessage(0);
				}
			}
		}
	}
	public final Handler msgHandler = new Handler(){  
        public void handleMessage(Message msg) {  
                switch (msg.arg1) {  
                case 1:  
                        Toast.makeText(getApplicationContext(), "Prepare upload", Toast.LENGTH_SHORT).show();  
                        break; 
                case 2:
                	 Toast.makeText(getApplicationContext(), "Uploading", Toast.LENGTH_SHORT).show();  
                     break; 
                case 3:
               	 Toast.makeText(getApplicationContext(), "Upload scuccess", Toast.LENGTH_SHORT).show();  
                    break; 
                default:  
                        break;  
                }  
        }  
};  
	public class uploadRun implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			// String file = "//sdcard//gt"; // 要上传的文件
			// FtpUtil.Sftp_server(outfilePath); // 上传文件

			try {
				ftp.prepareUpload();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // 上传文件
		}
	};
}
