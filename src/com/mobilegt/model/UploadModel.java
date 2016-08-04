package com.mobilegt.model;

public class UploadModel {

	private int uploadProgressBar;
	private String uploadPercentageTextView;
	private String uploadSpeedTextView;
	private String uploadPath;
	private String uploadFileLengthTextView;

	public UploadModel() {
		// TODO Auto-generated constructor stub
	}

	public String getUploadFileLengthTextView() {
		return uploadFileLengthTextView;
	}

	public void setUploadFileLengthTextView(String uploadFileLengthTextView) {
		this.uploadFileLengthTextView = uploadFileLengthTextView;
	}

	public String getUploadPercentageTextView() {
		return uploadPercentageTextView;
	}

	public String getUploadSpeedTextView() {
		return uploadSpeedTextView;
	}

	public String getUploadPath() {
		return uploadPath;
	}

	public int getUploadProgressBar() {
		return uploadProgressBar;
	}

	public void setUploadProgressBar(int uploadProgressBar) {
		this.uploadProgressBar = uploadProgressBar;
	}

	public void setUploadPercentageTextView(String uploadPercentageTextView) {
		this.uploadPercentageTextView = uploadPercentageTextView;
	}

	public void setUploadSpeedTextView(String uploadSpeedTextView) {
		this.uploadSpeedTextView = uploadSpeedTextView;
	}

	public void setUploadPath(String uploadPath) {
		this.uploadPath = uploadPath;
	}

}
