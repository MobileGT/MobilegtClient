package com.mobilegt.adapter;

import java.util.List;

import com.mobilegt.demo.R;
import com.mobilegt.model.UploadModel;



import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

public class UploadAdapter extends BaseAdapter {

	private List<UploadModel> mList;
	private LayoutInflater mInflater;

	public UploadAdapter(Context context, List<UploadModel> mList) {
		this.mList = mList;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@SuppressLint({ "ViewHolder", "InflateParams" })
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		ViewHolder viewHolder ;
		if (convertView == null) {

			convertView = mInflater.inflate(R.layout.item_upload, null);
			
			viewHolder=new ViewHolder();
			viewHolder.Percentage = (TextView) convertView
					.findViewById(R.id.upload_percentage_textview);
			viewHolder.Speed = (TextView) convertView
					.findViewById(R.id.upload_speed_textview);
			viewHolder.Path = (TextView) convertView
					.findViewById(R.id.upload_path);
			viewHolder.FileLength= (TextView) convertView
					.findViewById(R.id.upload_file_length_textview);
			viewHolder.Progressbar = (ProgressBar) convertView
					.findViewById(R.id.upload_progressbar);
			convertView.setTag(viewHolder);
		}else {
			viewHolder=(ViewHolder) convertView.getTag();
		}


		viewHolder.Percentage.setText(mList.get(position)
				.getUploadPercentageTextView());
		viewHolder.Speed.setText(mList.get(position)
				.getUploadSpeedTextView());
		viewHolder.Path.setText(mList.get(position).getUploadPath());
		viewHolder.FileLength.setText(mList.get(position)
				.getUploadFileLengthTextView());
		viewHolder.Progressbar.setProgress(mList.get(position)
				.getUploadProgressBar());
		return convertView;
	}

	class ViewHolder {
		TextView Percentage;
		TextView Speed;
		TextView Path;
		TextView FileLength;
		ProgressBar Progressbar;

	}

}
