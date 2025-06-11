package com.example.actnow.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.actnow.Fragments.FollowListDetailFragment;

public class FollowPagerAdapter extends FragmentStateAdapter {

    private final String userId;

    public FollowPagerAdapter(Fragment fragment, String userId) {
        super(fragment);
        this.userId = userId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return FollowListDetailFragment.newInstance(userId, position == 0 ? "following" : "followers");
    }

    @Override
    public int getItemCount() {
        return 2; // Подписки и Подписчики
    }
}