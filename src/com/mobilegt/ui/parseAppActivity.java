package com.mobilegt.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.mobilegt.demo.R;
import com.mobilegt.parse.AnalyzeData;
import com.mobilegt.utils.FileNameSelector;
import com.mobilegt.view.MyListView;
import com.mobilegt.view.MyListView.OnRefreshListener;
//import com.mobilegt.ui.MainActivity.catchSocket;
//import com.mobilegt.ui.R.id;
//import com.mobilegt.ui.R.layout;
import com.mobilegt.adapter.listAppAdapter;
//import com.mobilegt.upload.Compress;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class parseAppActivity extends Activity implements OnRefreshListener {

	private AnalyzeData ana = new AnalyzeData();
	private MyListView parseListView;
	private String gtFile = null;
	private String appFile = null;
	private String filepath;
	private Context context;
	private File[] tmplist;
	private HashMap<String, HashSet<String>> hm;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_parse_activity);

		parseListView = (MyListView) findViewById(R.id.parse_listview);
		filepath = "/sdcard/android/data/" + this.getPackageName() + "/files/";
		// appFile = bundle.getString("appfilename");
		context = this.getApplicationContext();
		File file = new File(filepath);
		tmplist = file.listFiles(new FileNameSelector("socket"));
		
		if (tmplist.length <= 0) {
			DisplayToast("There is no recorded socket information");
			// title.setText("采集的数据文件已删除，请采集新数据后上传");
		} else {

			parse(tmplist);
		}

		parseListView.setOnRefreshListener(this);
	}

	public void parse(File[] tmplist) {

		String filename = tmplist[tmplist.length-1].getName();
		String suffix = filename.substring(filename.lastIndexOf("."),
				filename.length());
		String name = filename.substring(0, filename.lastIndexOf("."));
		String path = tmplist[0].getParent() + "/";

		gtFile = path + name + ".socket";
		appFile = path + name + ".app";

		Log.i("gtfile", gtFile + "teacher");
		Log.i("appFile", appFile + "teacher");

		List<Map<String, Object>> list = getData();
		parseListView.setAdapter(new listAppAdapter(this, list));
		// Thread thread = new Thread(new parse()); // run parse
		// thread.start();
	}

	public List<Map<String, Object>> getData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		hm = ana.parseFiles(gtFile, appFile);
		for (String key : hm.keySet()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("title", key);
			String hosts = "";
			HashSet<String> ths = hm.get(key);
			HashSet<String> hs = new HashSet<>();
			for (String keys : ths) {
				String ho = keys.toString().split(" ")[1] + " " + keys.toString().split(" ")[5];
				hs.add(ho);
			}
			for (String keys : hs) {
				hosts += keys + " ";
				
			}
			map.put("info", hosts);
			list.add(map);
		}
		return list;
	}

	public void DisplayToast(String str) {
		Toast toast = Toast.makeText(this, str, Toast.LENGTH_LONG);
		// 设置toast显示的位置
		toast.setGravity(Gravity.TOP, 0, 220);
		// 显示该Toast
		toast.show();
	}

	@Override
	public void onRefresh() {

		onCreate(null);

		parseListView.onRefreshComplete();

	}
}
