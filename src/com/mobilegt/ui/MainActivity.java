package com.mobilegt.ui;



import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.mobilegt.about.AboutActivity;
import com.mobilegt.collect.catchApp;
import com.mobilegt.collect.catchSockets;
import com.mobilegt.demo.R;
import com.mobilegt.utils.CmdUtils;
import com.mobilegt.utils.fileManager;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;
import android.widget.TextView;

@SuppressWarnings("deprecation")
public class MainActivity extends TabActivity {
    /** Called when the activity is first created. */
	private TabHost tabHost;
	fileManager fManager = new fileManager(this);
	String filePath, outfilePath,appPath;
//	catchApp ctApp;
//	catchSockets ctSocket;
//	String recordeFileName;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        
        filePath = "/sdcard/android/data/" + this.getPackageName() + "/";
		outfilePath = "/sdcard/android/data/" + this.getPackageName()
				+ "/files/";
		appPath = getApplicationContext().getFilesDir().getAbsolutePath();	
//		String deviceid = new CmdUtils().getDeviceId();
//		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
//		Calendar calendar = Calendar.getInstance();
//		recordeFileName = deviceid+df.format(calendar.getTime());
//		
//		ctApp = new catchApp(outfilePath, getPackageManager());
//		ctSocket = new catchSockets(recordeFileName);
		
        try {
			fManager.creatFile(filePath, outfilePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e("error", e.toString());
		}
		fManager.varifyFile(getApplicationContext(), "gt", appPath);
		try {
			fManager.copyBigDataToSDgt2(filePath + "gt.conf");
			fManager.copyBigDataToSDgt(filePath + "gt.log");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			
			Log.e("error", e.toString());
		}
		
        tabHost=this.getTabHost();
        TabHost.TabSpec spec;
        Intent intent;

        intent=new Intent().setClass(this, ToyVpnClient.class);
        spec=tabHost.newTabSpec("Connect").setIndicator("Connect").setContent(intent);
        tabHost.addTab(spec);
        
        intent=new Intent().setClass(this,parseAppActivity.class);
        spec=tabHost.newTabSpec("ShowData").setIndicator("ShowData").setContent(intent);
        tabHost.addTab(spec);
        
        intent=new Intent().setClass(this, ResumableUploadWithoutKeyActivity.class);
        spec=tabHost.newTabSpec("Upload").setIndicator("Upload").setContent(intent);
        tabHost.addTab(spec);
        
     
        intent=new Intent().setClass(this, AboutActivity.class);
        spec=tabHost.newTabSpec("About us").setIndicator("About us").setContent(intent);
        tabHost.addTab(spec);
        tabHost.setCurrentTab(0);
        
        RadioGroup radioGroup=(RadioGroup) this.findViewById(R.id.main_tab_group);
        radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				switch (checkedId) {
				case R.id.main_tab_addExam:
					tabHost.setCurrentTabByTag("Connect");
					break;
				case R.id.main_tab_myExam:
					tabHost.setCurrentTabByTag("ShowData");
					break;
				case R.id.main_tab_message:
					tabHost.setCurrentTabByTag("Upload");
					break;
				case R.id.main_tab_settings:
					tabHost.setCurrentTabByTag("About us");
					break;
				default:
					//tabHost.setCurrentTabByTag("");
					break;
				}
			}
		});
    }
    
   
}