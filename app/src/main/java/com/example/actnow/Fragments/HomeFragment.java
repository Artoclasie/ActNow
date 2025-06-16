package com.example.actnow.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.actnow.Adapters.HomeViewPagerAdapter;
import com.example.actnow.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;

public class HomeFragment extends Fragment {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private LinearLayout searchLayout;
    private ImageView ivSearch;
    private TextInputEditText etSearch;
    private ChipGroup filterChipGroup;
    private HomeViewPagerAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupViewPager();
        setupSearchView();
    }

    private void initializeViews(View view) {
        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);
        searchLayout = view.findViewById(R.id.searchLayout);
        ivSearch = view.findViewById(R.id.ivSearch);
        etSearch = view.findViewById(R.id.etSearch);
        filterChipGroup = view.findViewById(R.id.filterChipGroup);
    }

    private void setupViewPager() {
        adapter = new HomeViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Все события");
                    break;
                case 1:
                    tab.setText("Новости");
                    break;
                case 2:
                    tab.setText("Волонтёры");
                    break;
                default:
                    tab.setText("Ошибка");
            }
        }).attach();

        // Handle page changes
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateSearchFilters(position);
            }
        });
    }

    private void setupSearchView() {
        ivSearch.setOnClickListener(v -> {
            if (searchLayout.getVisibility() == View.VISIBLE) {
                searchLayout.setVisibility(View.GONE);
            } else {
                searchLayout.setVisibility(View.VISIBLE);
            }
        });

        updateSearchFilters(0);

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            performSearch();
            return true;
        });

        filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> performSearch());
    }

    private void updateSearchFilters(int position) {
        filterChipGroup.removeAllViews();

        switch (position) {
            case 0: // Events
                addFilterChip("Животные");
                addFilterChip("Ветераны");
                addFilterChip("Молодеж");
                addFilterChip("Экология");
                addFilterChip("Образование");
                addFilterChip("Спорт");
                break;
            case 1:
            case 2:
                addFilterChip("Организаторы");
                addFilterChip("Организации");
                addFilterChip("Волонтёры");
                break;
        }
    }

    private void addFilterChip(String text) {
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setCheckable(true);
        chip.setCheckedIconVisible(true);
        chip.setChipBackgroundColorResource(R.color.chip_background);
        chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.text));
        filterChipGroup.addView(chip);
    }

    private void performSearch() {
        String query = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";
        StringBuilder filters = new StringBuilder();

        for (int i = 0; i < filterChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) filterChipGroup.getChildAt(i);
            if (chip.isChecked()) {
                if (filters.length() > 0) filters.append(",");
                filters.append(chip.getText());
            }
        }

        Fragment currentFragment = adapter.getFragment(viewPager.getCurrentItem());
        if (currentFragment instanceof SearchableFragment) {
            ((SearchableFragment) currentFragment).onSearch(query, filters.toString());
        } else {
            Toast.makeText(requireContext(), "Поиск не поддерживается в этой вкладке", Toast.LENGTH_SHORT).show();
            Log.w("HomeFragment", "Fragment at position " + viewPager.getCurrentItem() + " does not implement SearchableFragment");
        }
    }

    public interface SearchableFragment {
        void onSearch(String query, String filters);
    }
}