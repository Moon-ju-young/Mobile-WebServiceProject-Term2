package com.example.photoviewer;


import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    public static class Post {
        public String title, text;
        public Bitmap bitmap;

        public Post(String title, String text, Bitmap bitmap) {
            this.title = title;
            this.text = text;
            this.bitmap = bitmap;
        }
    }

    private List<Post> postList;

    public ImageAdapter(List<Post> postList) {
        this.postList = postList;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
// 이미지항목을나타낼뷰생성
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.textViewTitle.setText(post.title);
        holder.textViewText.setText(post.text);
        holder.imageView.setImageBitmap(post.bitmap);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textViewTitle, textViewText;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewItem);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewText = itemView.findViewById(R.id.textViewText);
        }
    }
}
