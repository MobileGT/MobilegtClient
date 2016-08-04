package com.mobilegt.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.mobilegt.demo.R;
import com.mobilegt.demo.R.id;
import com.mobilegt.demo.R.layout;
import com.mobilegt.adapter.UploadAdapter;
import com.mobilegt.model.UploadModel;
import com.mobilegt.myinterface.MyZip;
import com.mobilegt.upload.AES;
import com.mobilegt.upload.Compress;
import com.mobilegt.upload.QiNiuConfig;
import com.mobilegt.upload.Tools;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.KeyGenerator;
import com.qiniu.android.storage.UpCancellationSignal;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.qiniu.android.storage.persistent.FileRecorder;
import com.qiniu.android.utils.AsyncRun;
import com.qiniu.android.utils.UrlSafeBase64;


import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;
import org.json.JSONObject;


import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class ResumableUploadWithoutKeyActivity extends Activity implements
		MyZip {

	private UploadManager uploadManager;
	private long uploadLastOffset[] = new long[100];
	private long uploadFileLength[] = new long[100];
	private long uploadLastTimePoint[] = new long[100];
	private List<UploadModel> uploadModelList = new ArrayList<UploadModel>();

	private boolean cancelUpload;
	private String gtfilePath;
	private String zipName;
	private String zipFilePath; // 压缩之后的路径
	private ListView uploadListView;
	private ResumableUploadWithoutKeyActivity context;

	private UploadAdapter mAdapter;

	public ResumableUploadWithoutKeyActivity() {
		this.context = this;
		this.cancelUpload = false;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.resumable_upload_without_key_activity);
		getActionBar().setTitle("Upload files");
		uploadListView = (ListView) findViewById(R.id.upload_listview);
		Bundle bundle = this.getIntent().getExtras();
		// 接收gtfilepath值
		gtfilePath = bundle.getString("outfilepath");

		File file = new File(gtfilePath);
		File[] tmplist = file.listFiles();
		if (tmplist.length <= 0) {
			// title.setText("采集的数据文件已删除，请采集新数据后上传");
		} else {
			for (int i = 0; i < tmplist.length; i++) {
				if (tmplist[i].isFile()) {
					// Log.i("file",tmplist[i] + "teacher");
					// filelength += tmplist[i].length();
					// if(tmplist[i].length() <= 342){
					// //
					// title.setText("您采集的时间太短，请间隔较长时间才停止采集，或者您的手机不能采集数据，请发送您的手机型号和操作系统版本给我们，便于改进程序，谢谢您的合作");
					// // try {
					// //// dFile(tmplist[i].toString());
					// // } catch (IOException e) {
					// // // TODO Auto-generated catch block
					// // e.printStackTrace();
					// // }
					// }else{
					// Uri uri = Uri.parse("file://"+tmplist[i]);

					zipName = tmplist[i].getName().substring(0,
							tmplist[i].getName().lastIndexOf("."));					
					zipFilePath = gtfilePath.substring(0,
							gtfilePath.lastIndexOf("/"));
					zipFilePath = zipFilePath.substring(0,
							zipFilePath.lastIndexOf("/") + 1)
							+ "zip/";
//					String zipfile = zipFilePath+zipName+".zip";
//					if(!new File(zipfile).exists()){
					new Compress(context, gtfilePath, zipFilePath + zipName
							+ ".zip", zipName, this).execute();
//					}else{
//						zipComplete(zipfile);
//						
//					}
				}
			}

		}

		mAdapter = new UploadAdapter(context, uploadModelList);
		uploadListView.setAdapter(mAdapter);

	}

	/*
	 * 压缩完成后的回调函数
	 * zipPath  压缩后文件的存放路径
	 * AESPath  加密后文件的存放路径
	 */
	@Override
	public void zipComplete(String zipPath) {
		// TODO Auto-generated method stub

		String AESPath=zipPath+"(AES)";

		boolean isHas = false;// 压缩后的zip是否已经在list中存在了
		for (int i = 0; i < uploadModelList.size(); i++) {
			if (uploadModelList.get(i).getUploadPath()
					.equals(AESPath)) {
				isHas = true;
			}
		}
		if (!isHas) {
//			Toast.makeText(context, zipPath + "compressed", Toast.LENGTH_SHORT)
//					.show();
			
			
//			if(!file.exists()){
			AES.main(zipPath,AESPath);
//			}
			File file = new File(AESPath);
			UploadModel uploadModel = new UploadModel();
			uploadModel.setUploadPath(file.getPath());
			uploadModel.setUploadPercentageTextView("0 %");
			uploadModel.setUploadSpeedTextView("Prepare for upload");
			uploadModel.setUploadProgressBar(0);
			uploadModel
					.setUploadFileLengthTextView(Tools.formatSize(file.length()));
			uploadModelList.add(uploadModel);
			mAdapter.notifyDataSetChanged();
		}

	}

	// 上传文件
	public void uploadFile(View view) {
		if (uploadModelList.size() == 0) {
			return;
		}
		// 是否取消上传
		this.cancelUpload = false;
		getUploadManager();
		for (int i = 0; i < uploadModelList.size(); i++) {
			UploadModel uploadModel = uploadModelList.get(i);
			File file = new File(uploadModel.getUploadPath());
			if (file.exists()) {
				load(uploadModelList.get(i), i);				
			} else {
				Toast.makeText(
						context,
						uploadModel.getUploadPath().substring(
								uploadModel.getUploadPath().lastIndexOf("/")+1)
								+ "not exist, may be deleted", Toast.LENGTH_LONG).show();
				uploadModelList.get(i).setUploadSpeedTextView("上传失败");
				mAdapter.notifyDataSetChanged();
			}
		}

	}

	// 先获取Manager
	private void getUploadManager() {
		if (this.uploadManager == null) {
			try {
				this.uploadManager = new UploadManager(new FileRecorder(
						this.getFilesDir() + "/QiniuAndroid"),
						new KeyGenerator() {
							// 指定一个进度文件名，用文件路径和最后修改时间做hash
							// generator
							@Override
							public String gen(String key, File file) {
								String recorderName = System
										.currentTimeMillis() + ".progress";
								try {
									recorderName = UrlSafeBase64
											.encodeToString(Tools.sha1(file
													.getAbsolutePath()
													+ ":"
													+ file.lastModified()))
											+ ".progress";
								} catch (NoSuchAlgorithmException e) {
									Log.e("QiniuLab", e.getMessage());
								} catch (UnsupportedEncodingException e) {
									Log.e("QiniuLab", e.getMessage());
								}
								return recorderName;
							}
						});
			} catch (IOException e) {
				Log.e("QiniuLab", e.getMessage());
			}
		}
	}

	// 上传函数
	private void load(UploadModel uploadModel, final int i) {

		// 将文件路径中的文件名取出来
		String filename = "";
		if (filename == null || "".equals(filename)) {
			filename = uploadModel.getUploadPath().substring(
					uploadModel.getUploadPath().lastIndexOf("/") + 1);
		}
		String token = QiNiuConfig.getToken();// 获取上传凭证
		File file = new File(uploadModel.getUploadPath()); // 要上传的文件
		final long startTime = System.currentTimeMillis();
		final long fileLength = file.length();
		uploadLastOffset[i] = 0;
		uploadLastTimePoint[i] = startTime;
		uploadFileLength[i] = fileLength;

		// (下面用到)回调函数。(上传函数的参数)上传结束后调用此函数
		UpCompletionHandler upCompletionHandler = new UpCompletionHandler() {
			@Override
			public void complete(String key, ResponseInfo respInfo,
					JSONObject jsonData) {

				long lastMillis = System.currentTimeMillis() - startTime;
				if (respInfo.isOK()) {
					String time = Tools.formatMilliSeconds(lastMillis);
					updateFinish(i, time);
					delteFile(i);
				}
			}
		};

		// (下面用到)UploadOption，(上传函数的参数)上传进度，中途取消等。
		UploadOptions uploadOptions = new UploadOptions(null, null, false,
				new UpProgressHandler() {
					@Override
					public void progress(String key, double percent) {
						Log.i("uploadpercent",percent+"teacher");
						updateStatus(percent, i);// 实时更新上传进度
					}
				}, new UpCancellationSignal() {

					@Override
					public boolean isCancelled() {
						return cancelUpload; // 是否取消上传
					}
				});

		// 将上传文件，上传凭证，回调函数，上传情况函数 put 进manager进行上传
		uploadManager.put(file, filename, token, upCompletionHandler,
				uploadOptions);

	}
//deleteFile
	private void delteFile(final int i){
		String filename = uploadModelList.get(i).getUploadPath();
		String zipfilename = uploadModelList.get(i).getUploadPath().substring(0,
				uploadModelList.get(i).getUploadPath().lastIndexOf("("));
		String appfilename = uploadModelList.get(i).getUploadPath().substring(0,
				uploadModelList.get(i).getUploadPath().lastIndexOf("."));
		appfilename = appfilename.substring(appfilename.lastIndexOf("/")+1,appfilename.length());
		appfilename = gtfilePath+appfilename+".app";
		
		String socketfilename= uploadModelList.get(i).getUploadPath().substring(0,
				uploadModelList.get(i).getUploadPath().lastIndexOf("."));
		socketfilename = socketfilename.substring(socketfilename.lastIndexOf("/")+1,socketfilename.length());
		socketfilename = gtfilePath+socketfilename+".socket";
		
		File file = new File(filename);
		File zipfile = new File(zipfilename);
		File appfile = new File(appfilename);
		File socketfile = new File(socketfilename);
		if(file.exists() && file.isFile()){
			file.delete();
			Toast.makeText(context, filename + " deleted", Toast.LENGTH_SHORT)
			.show();
		}
		if(zipfile.exists() && zipfile.isFile()){
			zipfile.delete();
			Toast.makeText(context, zipfilename + " deleted", Toast.LENGTH_SHORT)
			.show();
		}
		
		
		if(appfile.exists() && appfile.isFile()){
			appfile.delete();
			Toast.makeText(context, appfilename + " deleted", Toast.LENGTH_SHORT)
			.show();
		}
		if(socketfile.exists() && socketfile.isFile()){
			socketfile.delete();
			Toast.makeText(context, socketfilename + " deleted", Toast.LENGTH_SHORT)
			.show();
		}
	}
	// 上传过程更新界面。
	private void updateStatus(final double percentage, final int i) {
		long now = System.currentTimeMillis();
		long deltaTime = now - uploadLastTimePoint[i];
		long currentOffset = (long) (percentage * uploadFileLength[i]);
		long deltaSize = currentOffset - uploadLastOffset[i];
		if (deltaTime <= 1000) {
			return;
		}

		final String speed = Tools.formatSpeed(deltaSize, deltaTime);
		// update
		uploadLastTimePoint[i] = now;
		uploadLastOffset[i] = currentOffset;

		AsyncRun.run(new Runnable() {
			@Override
			public void run() {

				int progress = (int) (percentage * 100);
				uploadModelList.get(i).setUploadProgressBar(progress);
				uploadModelList.get(i).setUploadPercentageTextView(
						progress + " %");
				uploadModelList.get(i).setUploadSpeedTextView(speed);
				mAdapter.notifyDataSetChanged();
			}
		});
	}

	// 上传完成更新界面
	private void updateFinish(final int i, final String time) {

		AsyncRun.run(new Runnable() {

			@Override
			public void run() {

				uploadModelList.get(i).setUploadProgressBar(100);
				uploadModelList.get(i).setUploadPercentageTextView(100 + " %");
				uploadModelList.get(i)
						.setUploadSpeedTextView("upload finished:" + time);
				mAdapter.notifyDataSetChanged();
			}
		});

	}

	// 取消上传
	public void cancelUpload(View view) {
		this.cancelUpload = true;

	}

	// 压缩文件夹
	public void compressExe(String srcPathName, String zipFilePath) {

		try {
			File srcdir = new File(srcPathName);
			if (!srcdir.exists()) {
				// Toast.makeText(context, srcPathName + " is not exist!",
				// Toast.LENGTH_SHORT).show();
			}
			Project prj = new Project();
			Zip zip_ = new Zip();
			zip_.setProject(prj);
			zip_.setDestFile(new File(zipFilePath));

			FileSet fileSet = new FileSet();
			fileSet.setProject(prj);
			fileSet.setDir(srcdir);
			// fileSet.setIncludes(filename); // 包括哪些文件或文件夹
			// fileSet.setExcludes(...); //排除哪些文件或文件夹
			zip_.addFileset(fileSet);

			zip_.execute();
			// Toast.makeText(context, "压缩完成", Toast.LENGTH_SHORT).show();
		} catch (Exception ex) {
			// Toast.makeText(context, ex.getMessage(),
			// Toast.LENGTH_SHORT).show();
		}
	}

}
