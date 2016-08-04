package com.mobilegt.collect;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.mobilegt.utils.CmdUtils;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

public class catchApp {
	String outfilepath;
	PackageManager pm;
	public catchApp(String outfilepath, PackageManager pm){
		this.outfilepath = outfilepath;
		this.pm = pm;
	}
	public void recordApps(String name){
		List<Group> list = getAppsInfo(false);
		StringBuffer str = new StringBuffer();
		for (int i = 0; i < list.size(); i++) {
			Group g = list.get(i);
			str.append(g.getUserId() + "\t" + g.getAppName() + "\t"
					+ g.getVersion() + "\t" + g.getAppRoot() + "\n");
		}
		write(str.toString(),name);
	}
	/**
	 * 获取本机应用列表信息(应用名，版本，安装文件路径)
	 * */
	public List<Group> getAppsInfo(boolean getSysPackages) {
		List<Group> groups = new ArrayList();
		List<PackageInfo> packs = new ArrayList();
		packs = pm.getInstalledPackages(0); // 获取应用信息列表
		if (packs == null) {
			return null;
		}
		for (int i = 0; i < packs.size(); i++) {
			PackageInfo pack = packs.get(i);
			String appName = pack.applicationInfo
					.loadLabel(pm).toString();
			String userId = pack.applicationInfo.uid + "";
			String version = pack.versionName;
			String appRoot = pack.packageName;

			if ((!getSysPackages)
					&& (appName.equals("") || appName == null
							|| version == null || version.equals("")
							|| appRoot == null || appRoot.equals("")
					// || userId == null || userId.equals("")
					)) {
				continue;
			}
			Group group = new Group(appName, version, appRoot, userId);
			groups.add(group);
		}
		return groups;
	}
	
	private void write(String str,String name) {
		try {
			
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {// 如果手机插入了SD卡，而且应用程序具有访问SD的权限（在AndroidManifest.xml配置）
				File sdCardDir = Environment.getExternalStorageDirectory();// 获取SD卡目录
				File targetFile = new File(outfilepath + name + ".app");

				Log.i("sdCardDir.getCanonicalPath()",
						"teacher" + outfilepath);
				RandomAccessFile raf = new RandomAccessFile(targetFile, "rw");// 以指定文件创建RandomAccessFile对象
				raf.seek(0);// 将文件记录指针移动到开始
				raf.write(str.getBytes());// 写入文件内容
				raf.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
