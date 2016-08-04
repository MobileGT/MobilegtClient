package com.mobilegt.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.mobilegt.model.UploadModel;
import com.mobilegt.myinterface.MyZip;
import com.mobilegt.ui.MainActivity;
import com.mobilegt.ui.ToyVpnClient;
import com.mobilegt.upload.AES;
import com.mobilegt.upload.Compress;
 
 
public class FtpUtil implements MyZip{
	private static Context context;
	static String directory = Constants.FTP_DIRECTORY;
//	private static ChannelSftp sftp;
	static SFTPChannel channel;
	static ChannelSftp chSftp;
	public static String zipFilePath;
	private static Handler msgHandler;
	public static boolean lastone = false;
	public FtpUtil(Context context, Handler msgHandler){
		this.context = context;
		this.msgHandler = msgHandler;
	}
 /**
  * 连接sftp服务器
  * @param host 主机
  * @param port 端口
  * @param username 用户名
  * @param password 密码
  * @return
  */
 public static ChannelSftp connect(String host, int port, String username,String password) {
          
     ChannelSftp sftp = null;
          try {
               JSch jsch = new JSch();
               jsch.getSession(username, host, port);
               Session sshSession = jsch.getSession(username, host, port);
               System.out.println("Session created.");
               sshSession.setPassword(password);
               Properties sshConfig = new Properties();
               sshConfig.put("StrictHostKeyChecking", "no");
               sshSession.setConfig(sshConfig);
               sshSession.connect();
               System.out.println("Session connected.");
               System.out.println("Opening Channel.");
               Channel channel = sshSession.openChannel("sftp");
               channel.connect();
               sftp = (ChannelSftp) channel;
               Log.i("connect","mobilegtConnected to " + host + ".");
               //System.out.println("登录成功");
          } catch (Exception e) {
        	  Log.e("TAG", "mobilegt " + e.toString());
          }
          return sftp;
 }
 
 /**
  * 上传文件
  * @param directory 上传的目录
  * @param uploadFile 要上传的文件
  * @param sftp
  */
 public void upload(String directory, String uploadFile, ChannelSftp sftp) {
   
     try {
               sftp.cd(directory);
               File file=new File(uploadFile);
               
//               String currentTime=DateFormat.format("yyyy_MM_dd_hh_mm_ss", new Date()).toString(); //获取时间
//               String filename=currentTime+".wav"; //文件名为当前时间来保存
String filename = file.getName();
               sftp.put(new FileInputStream(file), filename);   
               
             
             
               System.out.println("上传成功！");
        } catch (Exception e) {
           e.printStackTrace();
          }
 }
 
 /**
  * 下载文件
  * @param directory 下载目录
  * @param downloadFile 下载的文件
  * @param saveFile 存在本地的路径
  * @param sftp
  */
 public void download(String directory, String downloadFile,String saveFile, ChannelSftp sftp) {
         
       try {
           sftp.cd(directory);
           File file=new File(saveFile);
           sftp.get(downloadFile, new FileOutputStream(file));
          } catch (Exception e) {
           e.printStackTrace();
          }
 }
 
 /**
  * 删除文件
  * @param directory 要删除文件所在目录
  * @param deleteFile 要删除的文件
  * @param sftp
  */
 public void delete(String directory, String deleteFile, ChannelSftp sftp) {
     
     try {
       sftp.cd(directory);
       sftp.rm(deleteFile);
      } catch (Exception e) {
       e.printStackTrace();
      }
 }
 
 /**
  * 列出目录下的文件
  * @param directory 要列出的目录
  * @param sftp
  * @return
  * @throws SftpException
  */
 public Vector listFiles(String directory, ChannelSftp sftp) throws SftpException{
   
     return sftp.ls(directory);
  
 }
 public void prepareUpload() {
	 String gtfilePath = ToyVpnClient.outfilePath;
	 try{
	 connectServer();
	 Log.i("connect","connectserver mobilegt");
		String filePath = gtfilePath.substring(0, gtfilePath.lastIndexOf("/"));// 原来的路径的上一级目录

		zipFilePath = filePath.substring(0,
				filePath.lastIndexOf("/") + 1) + "zip/";// 新目录，存放压缩后的文件

		File[] files = new File(zipFilePath).listFiles();
		if (files != null) {

			for (int i = 0; i < files.length; i++) {

				if (files[i].getPath()
						.substring(files[i].getPath().lastIndexOf("."))
						.equals(".zip(AES)")) {	
					Log.i("add",files[i].getPath() +"addsuccess mobilegt");
//					showData(files[i].getPath() + "add success");
					
					chSftp.put(files[i].getPath(), directory, new FileProgressMonitor(files[i],context), ChannelSftp.OVERWRITE);
//					Toast.makeText(context, files[i].getPath() + "add success",
//							Toast.LENGTH_SHORT).show();					
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
						Log.i("compress","compresssed mobilegt" + zipName);
						zipNameList.add(zipName);
					}
				}
			}
//			chSftp.quit();
//	        channel.closeChannel();
		}
	 }catch(Exception e){
		 Log.e("exception", e.toString() + "mobilegt");
	 }
	}
 
 public SFTPChannel getSFTPChannel() {
     return new SFTPChannel();
 }
 public void connectServer() {
	 Map<String, String> sftpDetails = new HashMap<String, String>();
     // 设置主机ip，端口，用户名，密码
//     sftpDetails.put(SFTPConstants.SFTP_REQ_HOST, "10.81.10.250");
//     sftpDetails.put(SFTPConstants.SFTP_REQ_USERNAME, "lz");
//     sftpDetails.put(SFTPConstants.SFTP_REQ_PASSWORD, "liuzhen");
//     sftpDetails.put(SFTPConstants.SFTP_REQ_PORT, "22");
   
     sftpDetails.put(SFTPConstants.SFTP_REQ_HOST, Constants.VPN_SERVER);
     sftpDetails.put(SFTPConstants.SFTP_REQ_USERNAME, Constants.USER);
     sftpDetails.put(SFTPConstants.SFTP_REQ_PASSWORD, Constants.PSWD);
     sftpDetails.put(SFTPConstants.SFTP_REQ_PORT, Constants.FTP_PORT);
     
     channel = getSFTPChannel();
     chSftp = channel.getChannel(sftpDetails, 60000);
     
//     sftp=connect(host, port, username, password);
//     try {
//    	 chSftp.cd(directory);
//	} catch (SftpException e) {
//		// TODO Auto-generated catch block
//		Log.e("TAG", "mobilegt " + e.toString());
//	}
     if(chSftp!=null)
     Log.i("connectserver","mobilegt finished connect");

     Message msg = msgHandler.obtainMessage();  
     msg.arg1 = 1;  
     msgHandler.sendMessage(msg); 
 }
 //上传文件
  
//  public static void showData(String msg){
//	  Looper.prepare();
//	  Toast.makeText(context, msg,Toast.LENGTH_SHORT).show();
////	  Looper.loop();
//  }
// public static void Sftp_server(String filePath) {
// 
////      String Imsi = "imsi";   
//      
//     
//       
//      //sf.download(directory, downloadFile, saveFile, sftp);
//      //sf.delete(directory, deleteFile, sftp);
//             
//          try{
//        	  
//               
//               System.out.println("finished");
//               
//               upload(directory, uploadFile, sftp);//上传文件到服务器
//               
//              }catch(Exception e){
//                  System.out.println("exceptionnnnnnnnnnn");
//                  Log.i("connectserver","mobilegt exceptionnnnnnnnnnn");
//                  Log.e("TAG", "mobilegt " + e.toString());
//              }
////              directory = "/data/test/wav/"+imsi;
//              
//              
// }
 @Override
	public void zipComplete(String zipPath) {
		// TODO Auto-generated method stub

		String AESPath = zipPath + "(AES)";

		AES.main(zipPath, AESPath);
		Log.i("aes","aes mobilegt");
		File file = new File(AESPath);
		try {
			chSftp.put(AESPath, directory, new FileProgressMonitor(file,context), ChannelSftp.OVERWRITE);
		} catch (SftpException e) {
			// TODO Auto-generated catch block
			Log.e("TAGmobilegt", "mobilegt " + e.toString());
		}
//		uploadFile(file);
//		upload(directory, AESPath, chSftp);//上传文件到服务器
	}
 
  public static void main(String []args){
	  String file = "/sdcard/gt"; //要上传的文件
//	  FtpUtil.Sftp_server(file); //上传文件
	  System.out.println("已上传");
  }
}