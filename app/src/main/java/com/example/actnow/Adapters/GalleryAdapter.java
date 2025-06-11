package com.example.actnow.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.actnow.R;

import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {
    private List<String> imageUrls;
    private boolean showDeleteButton;
    private OnImageDeleteListener deleteListener; // Исправлено: GalleryAdapter.OnImageDeleteListener
    private OnImageClickListener clickListener;

    // Интерфейс для обработки удаления изображения
    public interface OnImageDeleteListener {
        void onImageDelete(int position, String imageUrl);
    }

    // Интерфейс для обработки клика по изображению
    public interface OnImageClickListener {
        void onImageClick(String imageUrl);
    }

    public GalleryAdapter(List<String> imageUrls, boolean showDeleteButton, OnImageDeleteListener deleteListener, OnImageClickListener clickListener) {
        this.imageUrls = imageUrls;
        this.showDeleteButton = showDeleteButton;
        this.deleteListener = deleteListener;
        this.clickListener = clickListener;
    }

    public GalleryAdapter(List<String> imageUrls) {
        this(imageUrls, false, null, null);
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_image, parent, false);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        Log.d("GalleryAdapter", "Binding image at position " + position + ": " + imageUrl +
                ", showDeleteButton=" + showDeleteButton);
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.default_image)
                .error(R.drawable.default_image)
                .into(holder.ivGalleryImage);

        // Управляем видимостью кнопки удаления
        holder.btnDeleteImage.setVisibility(showDeleteButton ? View.VISIBLE : View.GONE);

        // Обработчик клика по кнопке удаления
        if (showDeleteButton && deleteListener != null) {
            holder.btnDeleteImage.setOnClickListener(v -> deleteListener.onImageDelete(position, imageUrl));
        } else {
            holder.btnDeleteImage.setOnClickListener(null);
        }

        // Клик по изображению
        holder.ivGalleryImage.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onImageClick(imageUrl);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class GalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivGalleryImage;
        ImageButton btnDeleteImage;

        GalleryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGalleryImage = itemView.findViewById(R.id.iv_gallery_image);
            btnDeleteImage = itemView.findViewById(R.id.btn_delete_image);
        }
    }
}