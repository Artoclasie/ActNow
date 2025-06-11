package com.example.actnow.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.actnow.R;

public class ImageViewerFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_viewer, container, false);
        ImageView ivImage = view.findViewById(R.id.iv_image);

        String imageUrl = getArguments() != null ? getArguments().getString("imageUrl") : null;
        if (imageUrl != null) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.default_image)
                    .error(R.drawable.default_image)
                    .into(ivImage);
        } else {
            ivImage.setImageResource(R.drawable.default_image);
        }

        // Закрытие фрагмента при клике на изображение
        view.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        return view;
    }
}