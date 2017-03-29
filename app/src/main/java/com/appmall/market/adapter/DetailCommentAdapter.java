package com.appmall.market.adapter;

import com.appmall.market.R;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.appmall.market.adapter.ItemBuilder.CommentInfoHolder;
import com.appmall.market.bean.CommentList.Comment;

public class DetailCommentAdapter extends BaseAdapter {
	
	private LayoutInflater mInflater;
	private List<Comment> mData;
	
	public DetailCommentAdapter(Context context) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public void setData(List<Comment> data) {
		mData = data;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return mData == null ? 0 : mData.size();
	}
	
	@Override
	public Object getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	private static final String DATA_PATTERN = "yyyy-MM-dd";
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Comment info = (Comment) getItem(position);
		convertView = ItemBuilder.buildCommentInfoItemView(mInflater, position, convertView, parent);
		
		CommentInfoHolder holder = (CommentInfoHolder) convertView.getTag();
		holder.mStar.setRank(info.mStarLevel*2);
		if(!TextUtils.isEmpty(info.mDeviceName))
			holder.mModel.setText(info.mDeviceName);
		holder.mCommentText.setText(info.mContent);
		if(info.mIsNew == 1)
			holder.mVersion.setText("当前版本");
		else
			holder.mVersion.setText("老版本");
		
		Locale locale = Locale.getDefault();
		String format = parent.getResources().getString(R.string.update_time);
		SimpleDateFormat dataFormat = new SimpleDateFormat(DATA_PATTERN, locale);
		String updateTime = String.format(locale, format, dataFormat.format(new Date(info.mUpdateTime)));
		holder.mUpdateTime.setText(updateTime);
		
		return convertView;
	}
}