package com.mobilegt.parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import android.util.Log;

public class AnalyzeData {
	public static HashMap<String, HashSet<String>> appInfo = new HashMap<>();

	public static String[] convertStrToArray(String str) {
		String[] strArray = null;
		strArray = str.split(", "); // 拆分字符为"," ,然后把结果交给数组strArray
		return strArray;
	}

	public static String[] convertStrToArray2(String str) {
		String[] strArray = null;
		strArray = str.split(" "); // 拆分字符为"," ,然后把结果交给数组strArray
		return strArray;
	}

	public String[] FTofGT(String tempString) {
		// File file = new File(fileName);
		// BufferedReader reader = null;
		// HashMap<String, Integer> map = new HashMap<String, Integer>();
		// HashSet<String> hs = new HashSet<String>();
		// HashMap<String, Integer> SrcPortDistOfAppDstIPPort = new
		// HashMap<String, Integer>();
		String[] six = new String[7];
		// System.out.println("before:" + six[0]);
		try {
			// System.out.println("以行为单位读取文件内容，一次读一整行：");
			// reader = new BufferedReader(new FileReader(file));
			// tempString = null;
			String[] tmp = null;
			String[] tmp2 = null;
			String[] tmp3 = null;
			tmp = convertStrToArray(tempString);
			tmp2 = convertStrToArray2(tmp[0]);
			// if(!tmp2[3].equals("0.0.0.0") && !tmp[1].equals("0.0.0.0"))
			{
				six[0] = (tmp2[3]) + ""; //srcip
				six[1] = (tmp[1]) + "";  //dstip
				six[2] = tmp[2]; //srcport
				six[3] = tmp[3];  //dstport

				tmp3 = convertStrToArray2(tmp[6]);
				// app = tmp3[1] + " "+ tmp3[2];
				six[4] = tmp[5]; //proto
				six[5] = tmp[4]; //appname
				six[6] = tmp3[0]; //created or destroy
				// System.out.println(fivetupel + " " + app);
				// for(int i = 0;i < six.length;i++)
				// {
				// System.out.print(six[i] + " ");
				// }
				// System.out.println();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		// reader.close();

		return six;
	}

	public HashMap<String, HashSet<String>> getAppName(String fileName) {
		File file = new File(fileName);
		HashMap<String, HashSet<String>> hm = new HashMap<>();
		BufferedReader reader = null;
		try {
			System.out.println("以行为单位读取文件内容，一次读一整行：");
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			int line = 1;
			String[] tmp = null;
			// 一次读入一行，直到读入null为文件结束

			while ((tempString = reader.readLine()) != null) {
				tmp = tempString.split("\\s");
				HashSet<String> hs = new HashSet<>();
				if (hm.containsKey(tmp[0])) {
					hs = hm.get(tmp[0]);
				}
				hs.add(tmp[1]);
				hm.put(tmp[0], hs);
				line++;
			}
			for (String key : hm.keySet()) {
				System.out.println(key + " " + hm.get(key));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hm;
	}

	public HashMap<String, HashSet<String>> sixGroup(String fileName) {
		File file = new File(fileName);
		BufferedReader reader = null;
//		String[][] sixGr = null;
		HashMap<String, HashSet<String>> hm = new HashMap<>();
		 SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
	        Date date = new Date();
	        String formatDate = sdf.format(date);
		try {
			System.out.println("以行为单位读取文件内容，一次读一整行：");
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			int line = 1;
			String[] tmp = null;
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
				if (!tempString.contains(formatDate))
					continue;
				
				HashSet<String> hs = null;
				tmp = FTofGT(tempString);
				if (tmp[4].equals("TCP")) {
					tmp[4] = 6 + "";
				} else if (tmp[4].equals("UDP")) {
					tmp[4] = 17 + "";
				}
				String tuple = tmp[0] + " " + tmp[1] + " " + tmp[2] + " "
						+ tmp[3] + " " + tmp[4] + " " + tmp[6];
				String app = tmp[5];

				if (hm.containsKey(app)) {
					hs = hm.get(app);
				} else {
					hs = new HashSet<String>();
				}
				hs.add(tuple);
				hm.put(app, hs);
				line++;
			}
			reader.close();
			for (String key : hm.keySet()) {
				System.out.println(key + "--" + hm.get(key));
//				Log.i("keyset",key+"teacher");
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		return hm;
	}

	public HashMap<String, HashSet<String>> parseFiles(String gtFile,
			String appFile) {
		HashMap<String, HashSet<String>> hmGt = sixGroup(gtFile);
//		HashMap<String, HashSet<String>> hmApp = getAppName(appFile);
		HashMap<String, HashSet<String>> res = new HashMap<>();
		Log.i("hmgt",hmGt.size() +"teacher");
//		Log.i("hmApp",hmApp.size() +"teacher");
//		for (String key : hmGt.keySet()) {
////			HashSet<String> hs = hmApp.get(key);
//			String appName = key;
////			int count = 0;
////			if (hs != null) {
////				for (String app : hs) {
////					count++;
////					appName = app;
////				}
////			} else {
////				System.out.println(key + "null");
////			}
//			
////			if (count > 1)
//			if(key.contains("NOT"));
//				appName = "?";
//				
//			res.put(appName, hmGt.get(key));
//		}
		return hmGt;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// AnalyzeData ana=new AnalyzeData();
		// HashMap<String,HashSet<String>> hm =
		// ana.parseFiles("D:\\20160430232153.socket",
		// "D:\\20160430232153.app");
		// for(String key:hm.keySet()){
		// System.out.println(key+" "+hm.get(key).size());
		// }
	}

}
