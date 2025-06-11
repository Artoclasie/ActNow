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

public class PlaceholderFragment extends Fragment {

    private static final String ARG_TAB_NAME = "tab_name";

    // Метод для создания экземпляра фрагмента с параметром
    public static PlaceholderFragment newInstance(String tabName) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TAB_NAME, tabName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_placeholder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Находим TextView из layout
        TextView textView = view.findViewById(R.id.placeholder_text);

        // Получаем имя таба из аргументов
        if (getArguments() != null) {
            String tabName = getArguments().getString(ARG_TAB_NAME, "Неизвестный таб");
            if (textView != null) {
                textView.setText("Содержимое таба: " + tabName + "\n(Фрагмент в разработке)");
            }
        } else {
            if (textView != null) {
                textView.setText("Содержимое таба: Неизвестно\n(Фрагмент в разработке)");
            }
        }
    }
}