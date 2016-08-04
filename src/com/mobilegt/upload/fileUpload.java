package com.mobilegt.upload;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.mobilegt.upload.AES;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.mobilegt.model.UploadModel;
import com.mobilegt.myinterface.MyZip;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.KeyGenerator;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.qiniu.android.storage.persistent.FileRecorder;
import com.qiniu.android.utils.UrlSafeBase64;
import com.mobilegt.upload.QiNiuConfig;
import com.mobilegt.upload.Tools;

public class fileUpload implements MyZip {
	private UploadManager uploadManager;
	private List<UploadModel> uploadModelList = new ArrayList<UploadModel>();

	private String gtfilePath;
	private Context context;

	public fileUpload(Context context, String gtfilePath) {
		this.context = context;
		this.gtfilePath = gtfilePath;
	}

	public void prepareUpload() {
		String filePath = gtfilePath.substring(0, gtfilePath.lastIndexOf("/"));// 原来的路径的上一级目录

		String zipFilePath = filePath.substring(0,
				filePath.lastIndexOf("/") + 1) + "zip/";// 新目录，存放压缩后的文件

		File[] files = new File(zipFilePath).listFiles();
		if (files != null) {

			for (int i = 0; i < files.length; i++) {

				if (files[i].getPath()
						.substring(files[i].getPath().lastIndexOf("."))
						.equals(".zip(AES)")) {

					UploadModel uploadModel = new UploadModel();

					uploadModelList.add(uploadModel);
					Toast.makeText(context, files[i].getPath() + "add success",
							Toast.LENGTH_SHORT).show();
				}
			}
		}
		File file = new File(gtfilePath);
		File[] tmplist = file.listFiles();
		if (tmplist.length <= 0) {
			// title.setText("采集的数据文件已删除，请采集新数据后上传");
		} else {
			List<String> zipNameList = new ArrayList<String>();
			for (int i = 0; i < tmplist.length; i++) {
				if (tmplist[i].isFile()) {
					String zipName = tmplist[i].getName().substring(0,
							tmplist[i].getName().lastIndexOf("."));// 取出文件名，不包括后缀
					String zipPath = zipFilePath + zipName + ".zip";// 压缩后的路径+文件名+后缀

					/*
					 * 判断压缩文件是否已经存在 以及同名文件是否正被压缩
					 */
					if (!new File(zipPath).exists()
							&& zipNameList.indexOf(zipName) == -1) {

						new Compress(context, gtfilePath, zipPath, zipName,
								this).execute();

						zipNameList.add(zipName);
					}
				}
			}
		}
	}

	/*
	 * 压缩完成后的回调函数 zipPath 压缩后文件的存放路径. AESPath 加密后文件的存放路径
	 */
	@Override
	public void zipComplete(String zipPath) {
		// TODO Auto-generated method stub

		String AESPath = zipPath + "(AES)";

		AES.main(zipPath, AESPath);

		File file = new File(AESPath);

		uploadFile(file);

	}

	// 上传文件
	public void uploadFile(File file) {
		getUploadManager();

		if (file.exists()) {
			load(file);
		} else {

		}
	}

	// 先获取Manager
	private void getUploadManager() {
		if (this.uploadManager == null) {
			try {
				this.uploadManager = new UploadManager(new FileRecorder(
						context.getFilesDir() + "/QiniuAndroid"),
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
	private void load(final File file) {

		String token = QiNiuConfig.getToken();// 获取上传凭证

		// (下面用到)回调函数。(上传函数的参数)上传结束后调用此函数
		UpCompletionHandler upCompletionHandler = new UpCompletionHandler() {
			@Override
			public void complete(String key, ResponseInfo respInfo,
					JSONObject jsonData) {
				Toast.makeText(context, file.getName() + "Upload Success",
						Toast.LENGTH_SHORT).show();
				delteFile(file);
			}

		};

		// (下面用到)UploadOption，(上传函数的参数)上传进度，中途取消等。
		UploadOptions uploadOptions = new UploadOptions(null, null, false,
				new UpProgressHandler() {
					@Override
					public void progress(String key, double percent) {

					}
				}, null);

		// 将上传文件，上传凭证，回调函数，上传情况函数 put 进manager进行上传
		uploadManager.put(file, file.getName(), token, upCompletionHandler,
				uploadOptions);

	}

	private void delteFile(File file) {
		String filename = file.getPath();
		String zipfilename = filename.substring(0, filename.lastIndexOf("("));
		String appfilename = filename.substring(0, filename.lastIndexOf("."));
		appfilename = appfilename.substring(appfilename.lastIndexOf("/") + 1,
				appfilename.length());
		appfilename = gtfilePath + appfilename + ".app";

		String socketfilename = filename
				.substring(0, filename.lastIndexOf("."));
		socketfilename = socketfilename.substring(
				socketfilename.lastIndexOf("/") + 1, socketfilename.length());
		socketfilename = gtfilePath + socketfilename + ".socket";

		File file1 = new File(filename);
		File zipfile = new File(zipfilename);
		File appfile = new File(appfilename);
		File socketfile = new File(socketfilename);
		if (file1.exists() && file1.isFile()) {
			file1.delete();
			// Toast.makeText(context, filename + " deleted",
			// Toast.LENGTH_SHORT)
			// .show();
		}
		if (zipfile.exists() && zipfile.isFile()) {
			zipfile.delete();
			// Toast.makeText(context, zipfilename + " deleted",
			// Toast.LENGTH_SHORT)
			// .show();
		}

		if (appfile.exists() && appfile.isFile()) {
			appfile.delete();
			// Toast.makeText(context, appfilename + " deleted",
			// Toast.LENGTH_SHORT)
			// .show();
		}
		if (socketfile.exists() && socketfile.isFile()) {
			socketfile.delete();
			// Toast.makeText(context, socketfilename + " deleted",
			// Toast.LENGTH_SHORT)
			// .show();

		}
	}

}
