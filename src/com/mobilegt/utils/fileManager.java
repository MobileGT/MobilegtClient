package com.mobilegt.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import android.content.Context;
import android.util.Log;

public class fileManager {
	CmdUtils cmd = new CmdUtils();
	Context context;
	public fileManager(Context context){
		this.context = context;
	}
	/** 验证文件是否存在, 如果不存在就拷贝 */
	public void varifyFile(Context context, String fileName, String appPath) {
		try {
			/* 查看文件是否存在, 如果不存在就会走异常中的代码 */
			context.openFileInput(fileName);
		} catch (FileNotFoundException notfoundE) {
			try {
				/* 拷贝文件到app安装目录的files目录下 */
				copyFromAssets(context, fileName, fileName);
				/* 修改文件权限脚本 */
				String script = "chmod 700 " + appPath + "/" + fileName;
				/* 执行脚本 */
				cmd.exe1(script);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/** 将文件从assets目录中拷贝到app安装目录的files目录下 */
	public void copyFromAssets(Context context, String source,
			String destination) throws IOException {
		/* 获取assets目录下文件的输入流 */
		InputStream is = context.getAssets().open(source);
		/* 获取文件大小 */
		int size = is.available();
		/* 创建文件的缓冲区 */
		byte[] buffer = new byte[size];
		/* 将文件读取到缓冲区中 */
		is.read(buffer);
		/* 关闭输入流 */
		is.close();
		/* 打开app安装目录文件的输出流 */
		FileOutputStream output = context.openFileOutput(destination,
				Context.MODE_PRIVATE);
		/* 将文件从缓冲区中写出到内存中 */
		output.write(buffer);
		/* 关闭输出流 */
		output.close();
	}
	
	public void copyBigDataToSDgt(String strOutFileName) throws IOException {
		InputStream myInput;
		OutputStream myOutput = new FileOutputStream(strOutFileName);
		myInput = context.getAssets().open("gt.log");
		byte[] buffer = new byte[1024];
		int length = myInput.read(buffer);
		while (length > 0) {
			myOutput.write(buffer, 0, length);
			length = myInput.read(buffer);
		}
		myOutput.flush();
		myInput.close();
		myOutput.close();
	}

	public void copyBigDataToSDgt2(String strOutFileName) throws IOException {
		InputStream myInput;
		OutputStream myOutput = new FileOutputStream(strOutFileName);
		myInput = context.getAssets().open("gt.conf");
		byte[] buffer = new byte[1024];
		int length = myInput.read(buffer);
		while (length > 0) {
			myOutput.write(buffer, 0, length);
			length = myInput.read(buffer);
		}

		myOutput.flush();

		myInput.close();
		myOutput.close();
	}
	
	public void creatFile(String filePath, String outfilePath) throws IOException {
		/*
		 * 若已经插入SD卡则创建文件夹，否则提示要插入SD卡
		 */
		File file = new File(filePath);
		if (!file.exists()) {
			file.mkdir();
			Log.i("createfile",file.toString() + "create files mobilegt");
		}

		file = new File(outfilePath);
		if (!file.exists()) {
			file.mkdir();
			Log.i("createfile",file.toString() +"create files mobilegt");
		}
		Log.i("createfile","create files mobilegt");
	}
}
