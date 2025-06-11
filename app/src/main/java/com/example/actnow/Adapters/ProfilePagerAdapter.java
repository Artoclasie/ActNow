package com.example.actnow.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.actnow.Fragments.ProfileAttendedEventsFragment;
import com.example.actnow.Fragments.ProfileCommentsFragment;
import com.example.actnow.Fragments.ProfileOrganizedFragment;
import com.example.actnow.Fragments.ProfilePostsFragment;
import com.example.actnow.Fragments.ProfileRegisteredEventsFragment;

import java.util.List;

public class ProfilePagerAdapter extends FragmentStateAdapter {

    private final String userId;
    private final String accountType;
    private final List<String> tabTitles;
    private final FragmentManager fragmentManager;

    public ProfilePagerAdapter(Fragment fragment, String userId, String accountType, List<String> tabTitles) {
        super(fragment);
        this.userId = userId;
        this.accountType = accountType;
        this.tabTitles = tabTitles;
        this.fragmentManager = fragment.getChildFragmentManager();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (tabTitles.get(position)) {
            case "Записаны":
                return ProfileRegisteredEventsFragment.newInstance(userId);
            case "Посты":
                return ProfilePostsFragment.newInstance(userId);
            case "Пройдены":
                return ProfileAttendedEventsFragment.newInstance(userId);
            case "Организованные":
                return ProfileOrganizedFragment.newInstance(userId);
            default:
                return ProfilePostsFragment.newInstance(userId);
        }
    }

    @Override
    public int getItemCount() {
        return tabTitles.size();
    }

}