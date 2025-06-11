package com.example.actnow.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.actnow.R;

public class ProfileCommentsFragment extends Fragment {

    private static final String ARG_USER_ID = "userId";

    public static ProfileCommentsFragment newInstance(String userId) {
        ProfileCommentsFragment fragment = new ProfileCommentsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_comments, container, false);
        TextView tvMessage = view.findViewById(R.id.tv_message);
        tvMessage.setText("Комментарии пользователя (в разработке)");
        return view;
    }
}