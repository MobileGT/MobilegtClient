package com.mobilegt.upload;

import java.io.File;


import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;

import com.mobilegt.myinterface.MyZip;

import android.R.integer;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class Compress extends AsyncTask<Void, integer, Boolean> {

	private File srcdir;
	private Zip zip_;
	private Project prj;
	private String srcPathName, zipFilePath,zipName;
	private Context context;
//	private ProgressDialog progressDialog;
	
	private MyZip myZip;

	@Override
	protected Boolean doInBackground(Void... params) {
		// TODO Auto-generated method stub

		zip_.execute();

		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		// TODO Auto-generated method stub
		if (result) {
//			progressDialog.dismiss();
			myZip.zipComplete(zipFilePath);
			
		}
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		try {
			srcdir = new File(srcPathName);
			if (!srcdir.exists()) {
				Toast.makeText(context, srcPathName + " is not exist!",
						Toast.LENGTH_SHORT).show();
			}
			prj = new Project();
			zip_ = new Zip();
			zip_.setProject(prj);
			zip_.setDestFile(new File(zipFilePath));

			FileSet fileSet = new FileSet();
			fileSet.setProject(prj);
			fileSet.setDir(srcdir);
			fileSet.setIncludes(zipName+".*"); // 包括哪些文件或文件夹
			// fileSet.setExcludes(...); //排除哪些文件或文件夹
			zip_.addFileset(fileSet);

		} catch (Exception ex) {
			Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
		}
//		progressDialog = new ProgressDialog(context);
//		progressDialog.setTitle("正在压缩文件");
//		progressDialog.setMessage("请稍候，这可能需要几分钟的时间");
//		progressDialog.setCancelable(false);
//		progressDialog.show();
		super.onPreExecute();
	}


	// 压缩文件夹
	public Compress(Context context, String srcPathName, String zipFilePath,String zipName,MyZip myZip) {

		this.context = context;
		this.srcPathName = srcPathName;
		this.zipFilePath = zipFilePath;
		this.zipName=zipName;
		this.myZip=myZip;

	}
	

}
