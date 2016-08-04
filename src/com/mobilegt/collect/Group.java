package com.mobilegt.collect;

import java.io.Serializable;

public class Group implements Serializable {
	public Group (String appName, String version, String appRoot, String userId) {
		this.appName = appName;
		this.version = version;
		this.appRoot = appRoot;
		this.userId = userId;
	}
	private String appName;
	private String version;
	private String appRoot;
	private String userId;
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getAppRoot() {
		return appRoot;
	}
	public void setAppRoot(String appRoot) {
		this.appRoot = appRoot;
	}
	public String getUserId(){
		return userId;
	}
	public void setUserId(String userId){
		this.userId = userId;
	}
}
