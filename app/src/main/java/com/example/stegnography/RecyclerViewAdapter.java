package com.example.stegnography;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
	Context context;
	List<ImageUploadInfo> MainImageUploadInfoList;
	ImageUploadInfo UploadInfo;
	
	public RecyclerViewAdapter(Context context, List<ImageUploadInfo> TempList) {
		this.MainImageUploadInfoList = TempList;
		this.context = context;
	}
	
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_items, parent, false);
		ViewHolder viewHolder = new ViewHolder(view);
		return viewHolder;
	}
	
	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		UploadInfo = MainImageUploadInfoList.get(position);
		String filename=UploadInfo.getImageName();
		holder.imageNameTextView.setText(filename);
		Log.d("filename1",filename);
		//Loading image from Glide library.
		String imageurl=UploadInfo.getImageURL();
		Glide.with(context).load(imageurl).into(holder.imageView);
		holder.selectImage.setOnClickListener(v -> {
			Intent intent = new Intent(context, Encrypt.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("TITLE", filename);
			Log.d("file name",filename);
			intent.putExtra("URL", imageurl);
			context.startActivity(intent);
		});;
	}
	
	@Override
	public int getItemCount() {
		return MainImageUploadInfoList.size();
	}
	
	class ViewHolder extends RecyclerView.ViewHolder{
		public ImageView imageView;
		public TextView imageNameTextView;
		public Button selectImage;
		
		public ViewHolder(View itemView) {
			super(itemView);
			imageView = (ImageView) itemView.findViewById(R.id.imageView);
			imageNameTextView = (TextView) itemView.findViewById(R.id.ImageNameTextView);
			selectImage=(Button) itemView.findViewById(R.id.button2);
		}
	}
}