package com.mobilegt.upload;

import org.json.JSONException;

import com.qiniu.api.auth.AuthException;
import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.rs.PutPolicy;

/**
 * Created by lixiaodaoaaa on 14/10/12.
 */
public final class QiNiuConfig {
	public static final String token = getToken();
	public static final String QINIU_AK = "V6Md7k8fYSrrjIlGlw5lFZnKjJMmTMqL-taB_LMh"; //七牛云存储的AccessKey
	public static final String QINIU_SK = "iu-UWo_sbdHz_VMBtfI5WCYso7Ms8yNkPlWLY26O";//七牛云存储的SecretKey
	public static final String QINIU_BUCKNAME = "test";//空间名称
	
	public static String getToken() {

		Mac mac = new Mac(QiNiuConfig.QINIU_AK, QiNiuConfig.QINIU_SK);
		PutPolicy putPolicy = new PutPolicy(QiNiuConfig.QINIU_BUCKNAME);
		
		putPolicy.returnBody = "{\"name\": $(fname),\"size\": \"$(fsize)\",\"w\": \"$(imageInfo.width)\",\"h\": \"$(imageInfo.height)\",\"key\":$(etag)}";
		try {
			String uptoken = putPolicy.token(mac);
			System.out.println("debug:uptoken = " + uptoken);
			return uptoken;
		} catch (AuthException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
}
