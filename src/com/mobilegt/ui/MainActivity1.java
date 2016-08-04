package com.mobilegt.ui;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

import com.mobilegt.collect.Group;
import com.mobilegt.collect.catchApp;
import com.mobilegt.collect.catchSockets;
import com.mobilegt.demo.R;
import com.mobilegt.utils.CmdUtils;
import com.mobilegt.utils.CmdUtils;
import com.mobilegt.utils.fileManager;
import com.mobilegt.upload.fileUpload;
import com.mobilegt.upload.upload;

//import com.qiniu.demo.ui.BlockUploadActivity;
//import com.qiniu.demo.ui.MainActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity1 extends Activity {

	Button catchP, stopP, post, upload, visual;
	TextView tx, filetx, intro;
	
	Process p;
	
	// Boolean stopPcapflag = true;
	
	
	
	List<Group> list;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
//		
//		preferences = getSharedPreferences("gt", Context.MODE_WORLD_READABLE);
//		editor = preferences.edit();// 获得指定的sharedpreferences对象
		
		init();
	}

	// protected void onRestoreInstanceState
	public void init() {
		catchP = (Button) findViewById(R.id.catchP);
		stopP = (Button) findViewById(R.id.stopP);
		upload = (Button) findViewById(R.id.upload);
		intro = (TextView) findViewById(R.id.intro);
		visual = (Button) findViewById(R.id.visual);
		
		
		
		tx = (TextView) findViewById(R.id.filename);
		filetx = (TextView) findViewById(R.id.error);
		filetx.setText("");
		
		

		catchP.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub

				Intent intent = new Intent(MainActivity1.this,
						ToyVpnClient.class);
				// Intent intent = new Intent(MainActivity.this,radio.class);
				startActivity(intent);
				
				
			}
		});

		stopP.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});

		upload.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// String gtfilename = "/sdcard/appTeacher/";
				
							Intent intent = new Intent(MainActivity1.this,
									ResumableUploadWithoutKeyActivity.class);
							startActivity(intent);
			}
		});

		visual.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
					Intent intent = new Intent(MainActivity1.this,
							parseAppActivity.class);
					startActivity(intent);
				
			}
		});

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) { // 监听返回键
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			moveTaskToBack(false);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	

	

}
