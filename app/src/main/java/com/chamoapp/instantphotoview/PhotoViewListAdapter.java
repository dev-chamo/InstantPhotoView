package com.chamoapp.instantphotoview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.chamoapp.instantphotoview.data.Photo;

import java.util.List;

/**
 * Created by Koo on 2016. 11. 1..
 */

public class PhotoViewListAdapter extends RecyclerView.Adapter<PhotoViewListAdapter.ViewHolder> implements View.OnLongClickListener{

    private Context mContext;
    private List<Photo> mPhotoList;

    public PhotoViewListAdapter(Context context, List<Photo> photoList) {
        mContext = context;
        mPhotoList = photoList;
    }

    @Override
    public PhotoViewListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(rootView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Photo photo = mPhotoList.get(position);
        String url = String.format("https://farm%s.staticflickr.com/%s/%s_%s_q.jpg", photo.getFarm(), photo.getServer(), photo.getId(), photo.getSecret());
        Glide.with(mContext).load(url).crossFade().into(holder.mCoverImage);

        holder.mItemLayout.setTag(photo);
        holder.mItemLayout.setOnLongClickListener(this);
    }

    @Override
    public int getItemCount() {
        return mPhotoList.size();
    }

    @Override
    public boolean onLongClick(View view) {
        if (view.getTag() != null && view.getTag() instanceof Photo){
            Photo photo = (Photo) view.getTag();
            ((PhotoClickListener) mContext).onPhotoClick(photo);
        }

        return false;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public LinearLayout mItemLayout;
        public ImageView mCoverImage;

        public ViewHolder(View rootView) {
            super(rootView);
            mItemLayout = (LinearLayout) rootView.findViewById(R.id.photo_list_cover_ly);
            mCoverImage = (ImageView) rootView.findViewById(R.id.photo_list_cover_iv);
        }
    }
}
