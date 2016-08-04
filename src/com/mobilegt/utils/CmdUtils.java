package com.mobilegt.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.format.Time;
import android.util.Log;

/**
 * @author ZhenLiu
 * 
 */
public class CmdUtils {


	/** 执行 shell 脚本命令 */
	public List<String> exe(String[] cmd) {
		/* 获取执行工具 */
		Process process = null;
		/* 存放脚本执行结果 */
		List<String> list = new ArrayList<String>();
		try {
			/* 获取运行时环境 */
			Runtime runtime = Runtime.getRuntime();
			/* 执行脚本 */
			process = runtime.exec(cmd);
			/* 获取脚本结果的输入流 */
			InputStream is = process.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = null;
			/* 逐行读取脚本执行结果 */
			while ((line = br.readLine()) != null) {
				list.add(line);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

	public String execCmd(String cmd1) {
		String result = "";
		DataOutputStream dos = null;
		DataInputStream dis = null;

		try {
			Process p = Runtime.getRuntime().exec(cmd1);// 经过Root处理的android系统即有su命令
			dos = new DataOutputStream(p.getOutputStream());
			dis = new DataInputStream(p.getInputStream());
			Log.i("cmdTAG", cmd1 + "teacher");
			String line = null;
			while ((line = dis.readLine()) != null) {
				Log.i("result", line + "teacher");
				result += line + " ";
			}
			p.destroy();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (dos != null) {
				try {
					dos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (dis != null) {
				try {
					dis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	public String execCmdkill(String cmd1) {
		String result = "";
		DataOutputStream dos = null;
		DataInputStream dis = null;
		Log.i("TAG", cmd1 + "teacherrr");
		try {
			Process p = Runtime.getRuntime().exec(cmd1);// 经过Root处理的android系统即有su命令
			dos = new DataOutputStream(p.getOutputStream());
			dis = new DataInputStream(p.getInputStream());

			String line = null;
			while ((line = dis.readLine()) != null) {
				String[] pids = line.split("\\s+");
				Log.i("result", line + " teacherr " + pids.length + " "
						+ pids[0] + " " + pids[1] + " " + pids[2]);
				if (isNumeric(pids[1])) {
					result += pids[1] + " ";
				}
			}
			Log.i("killresult", result + "teacherr");
			p.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (dos != null) {
				try {
					dos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (dis != null) {
				try {
					dis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	public List<String> exe1(String cmd) {
		/* 获取执行工具 */
		Process process = null;
		/* 存放脚本执行结果 */
		List<String> list = new ArrayList<String>();
		try {
			/* 获取运行时环境 */
			Runtime runtime = Runtime.getRuntime();
			/* 执行脚本 */
			process = runtime.exec(cmd);
			/* 获取脚本结果的输入流 */
			InputStream is = process.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = null;
			/* 逐行读取脚本执行结果 */
			while ((line = br.readLine()) != null) {
				Log.i("result", line + "teacher");
				list.add(line);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

	public boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		return pattern.matcher(str).matches();
	}
	@SuppressLint("NewApi") public String getDeviceId() {
        String SerialNumber = android.os.Build.SERIAL;
//        Log.i("TAG",SerialNumber+"teacher");
        return SerialNumber;
    }
	
	public static boolean isWifi(Context mContext) {
		// 判断当前是否有wifi网络
		ConnectivityManager connectivityManager = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null
				&& activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			return true;
		}
		return false;
	}

}
