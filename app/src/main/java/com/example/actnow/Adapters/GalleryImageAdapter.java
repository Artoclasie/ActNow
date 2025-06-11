package com.example.actnow.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.actnow.R;

import java.util.ArrayList;
import java.util.List;

public class GalleryImageAdapter extends RecyclerView.Adapter<GalleryImageAdapter.ImageViewHolder> {

    private Context context;
    private List<Object> images; // Список содержит String (URL) или Uri
    private OnImageDeleteListener deleteListener;

    public interface OnImageDeleteListener {
        void onImageDelete(int position);
    }

    public GalleryImageAdapter(Context context, List<Object> images, OnImageDeleteListener deleteListener) {
        this.context = context;
        this.images = images;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_gallery_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Object image = images.get(position);
        if (image instanceof String) {
            Glide.with(context)
                    .load((String) image)
                    .placeholder(R.drawable.default_image)
                    .error(R.drawable.default_image)
                    .into(holder.imageView);
        } else if (image instanceof Uri) {
            holder.imageView.setImageURI((Uri) image);
        }

        holder.deleteButton.setOnClickListener(v -> deleteListener.onImageDelete(position));
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public void updateImages(List<Object> newImages) {
        this.images.clear();
        this.images.addAll(newImages);
        notifyDataSetChanged();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton deleteButton;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_gallery_image);
            deleteButton = itemView.findViewById(R.id.btn_delete_image);
        }
    }
}