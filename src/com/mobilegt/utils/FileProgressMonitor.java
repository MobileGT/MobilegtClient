package com.mobilegt.utils;



import java.io.File;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.jcraft.jsch.SftpProgressMonitor;
import com.mobilegt.ui.ToyVpnClient;

public class FileProgressMonitor extends TimerTask implements SftpProgressMonitor {
    
    private long progressInterval = 5 * 1000; // 默认间隔时间为5秒
    
    private boolean isEnd = false; // 记录传输是否结束
    
    private long transfered; // 记录已传输的数据总大小
    
    private long fileSize; // 记录文件总大小
    
    private Timer timer; // 定时器对象
    
    private boolean isScheduled = false; // 记录是否已启动timer记时器
    
    private File file;
    private static Context context;
    public FileProgressMonitor(File file, Context context) {
        this.fileSize = file.length();
        this.file = file;
        this.context = context;
    }
    
    @Override
    public void run() {
        if (!isEnd()) { // 判断传输是否已结束
            System.out.println("Transfering is in progress.");
            long transfered = getTransfered();
            if (transfered != fileSize) { // 判断当前已传输数据大小是否等于文件总大小
                System.out.println("Current transfered: " + transfered + " bytes");
                sendProgressMessage(transfered);
            } else {
                System.out.println("File transfering is done.");
                setEnd(true); // 如果当前已传输数据大小等于文件总大小，说明已完成，设置end
            }
        } else {
            System.out.println("Transfering done. Cancel timer.");
            stop(); // 如果传输结束，停止timer记时器
            return;
        }
    }
    
    public void stop() {
        System.out.println("Try to stop progress monitor.");
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
            isScheduled = false;
        }
        System.out.println("Progress monitor stoped.");
    }
    
    public void start() {
        System.out.println("Try to start progress monitor.");
        if (timer == null) {
            timer = new Timer();
        }
        timer.schedule(this, 1000, progressInterval);
        isScheduled = true;
        System.out.println("Progress monitor started.");
    }
    
    /**
     * 打印progress信息
     * @param transfered
     */
    private void sendProgressMessage(long transfered) {
        if (fileSize != 0) {
            double d = ((double)transfered * 100)/(double)fileSize;
            DecimalFormat df = new DecimalFormat( "#.##"); 
            System.out.println("Sending progress message: " + df.format(d) + "%");
        } else {
            System.out.println("Sending progress message: " + transfered);
        }
    }

    /**
     * 实现了SftpProgressMonitor接口的count方法
     */
    public boolean count(long count) {
        if (isEnd()) return false;
        if (!isScheduled) {
            start();
        }
        add(count);
        return true;
    }

    /**
     * 实现了SftpProgressMonitor接口的end方法
     */
    public void end() {
        setEnd(true);
        delteFile(file);
//        FtpUtil.showData(file.getName() + "transfer success");
        Log.i("end","transfering end");
        System.out.println("transfering end.");
        
    }
    
    private synchronized void add(long count) {
        transfered = transfered + count;
    }
    
    private synchronized long getTransfered() {
        return transfered;
    }
    
    public synchronized void setTransfered(long transfered) {
        this.transfered = transfered;
    }
    
    private synchronized void setEnd(boolean isEnd) {
        this.isEnd = isEnd;
    }
    
    private synchronized boolean isEnd() {
        return isEnd;
    }

    public void init(int op, String src, String dest, long max) {
        // Not used for putting InputStream
    }
    private void delteFile(File file) {
		String filename = file.getPath();
		String zipfilename = filename.substring(0, filename.lastIndexOf("("));
		String appfilename = filename.substring(0, filename.lastIndexOf("."));
		appfilename = appfilename.substring(appfilename.lastIndexOf("/") + 1,
				appfilename.length());
		appfilename = ToyVpnClient.outfilePath + appfilename + ".app";

		String socketfilename = filename
				.substring(0, filename.lastIndexOf("."));
		socketfilename = socketfilename.substring(
				socketfilename.lastIndexOf("/") + 1, socketfilename.length());
		socketfilename = ToyVpnClient.outfilePath  + socketfilename + ".socket";

		File file1 = new File(filename);
		File zipfile = new File(zipfilename);
		File appfile = new File(appfilename);
		File socketfile = new File(socketfilename);
		Log.i("zipfile",zipfilename+"mobilegt");
		Log.i("aesfile",filename+"mobilegt");
		if (file1.exists() && file1.isFile()) {
			file1.delete();
			// Toast.makeText(context, filename + " deleted",
			// Toast.LENGTH_SHORT)
			// .show();
			Log.i("delete",filename+"deleted in mobilegt");
		}
		if (zipfile.exists() && zipfile.isFile()) {
			zipfile.delete();
			// Toast.makeText(context, zipfilename + " deleted",
			// Toast.LENGTH_SHORT)
			// .show();
			Log.i("delete",zipfilename+"deleted in mobilegt");
		}

		if (appfile.exists() && appfile.isFile()) {
			appfile.delete();
			// Toast.makeText(context, appfilename + " deleted",
			// Toast.LENGTH_SHORT)
			// .show();
			Log.i("delete",appfilename+"deleted in mobilegt");
		}
		if (socketfile.exists() && socketfile.isFile()) {
			socketfile.delete();
			// Toast.makeText(context, socketfilename + " deleted",
			// Toast.LENGTH_SHORT)
			// .show();
			Log.i("delete",socketfilename+"deleted in mobilegt");
		}
	}
}