package com.example.actnow.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.actnow.Fragments.AllOrganaizerFragment;
import com.example.actnow.Fragments.AllVolunteerFragment;
import com.example.actnow.Fragments.EventFragment;

import java.util.HashMap;
import java.util.Map;

public class HomeViewPagerAdapter extends FragmentStateAdapter {
    private final Map<Integer, Fragment> fragments = new HashMap<>();

    public HomeViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
        // Инициализируем фрагменты один раз
        fragments.put(0, EventFragment.newInstance());
        fragments.put(1, AllVolunteerFragment.newInstance());
        fragments.put(2, AllOrganaizerFragment.newInstance());
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment = fragments.get(position);
        if (fragment == null) {
            switch (position) {
                case 0:
                    fragment = EventFragment.newInstance();
                    break;
                case 1:
                    fragment = AllVolunteerFragment.newInstance();
                    break;
                case 2:
                    fragment = AllOrganaizerFragment.newInstance();
                    break;
                default:
                    throw new IllegalStateException("Unexpected position: " + position);
            }
            fragments.put(position, fragment);
        }
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    public Fragment getFragment(int position) {
        return fragments.get(position);
    }
}