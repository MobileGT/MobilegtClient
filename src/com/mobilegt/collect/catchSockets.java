package com.mobilegt.collect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.mobilegt.utils.CmdUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class catchSockets {
	SharedPreferences preferences;
	SharedPreferences.Editor editor;
	String filename;
	CmdUtils cmd = new CmdUtils();
	public catchSockets(String filename) {
		this.filename = filename;
	}
	
	public void catchPackage(String appPath, String filePath) {
		// Time time=new Time();
		// time.setToNow();
		// String
		// fileName=time.year+""+time.month+""+time.monthDay+""+time.hour+""+time.minute+""+time.second+".socket";
		try{
			
		String fileName = filename + ".socket";
//		preferences = context.getSharedPreferences("gt",
//				context.MODE_WORLD_READABLE);
//		editor = preferences.edit();
//		editor.putString("catchedFileName", fileName);
//		editor.commit();
		Log.i("gtfilename",fileName + "mygt");
		String[] cd = { "sh", "-c",
				appPath + "/gt -s 4000 > " + filePath + fileName };
		Log.i("TAG", cd[2] + "teacherr");
		List<String> results = cmd.exe(cd);
		Log.i("gtresults", results.toString() + "teacherr");		
	}catch(Exception e){
		Log.e("error", Log.getStackTraceString(e)+"teacherr");
	}
	}
	
}
