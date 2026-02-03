package com.example.appdevwardrobeinf246;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class mainapp extends AppCompatActivity {

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainapp);

        viewPager = findViewById(R.id.viewPager);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_wardrobe) {
                viewPager.setCurrentItem(0);
                return true;
            } else if (itemId == R.id.nav_outfits) {
                viewPager.setCurrentItem(1);
                return true;
            } else if (itemId == R.id.nav_profile) {
                viewPager.setCurrentItem(2);
                return true;
            }
            return false;
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0) {
                    bottomNavigationView.setSelectedItemId(R.id.nav_wardrobe);
                } else if (position == 1) {
                    bottomNavigationView.setSelectedItemId(R.id.nav_outfits);
                } else if (position == 2) {
                    bottomNavigationView.setSelectedItemId(R.id.nav_profile);
                }
            }
        });
    }

    private class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @Override
        public int getItemCount() {
            return 3;
        }

        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                String username = getIntent().getStringExtra("username");
                if (username == null) {
                    username = "User";
                }

                frag1 fragment = new frag1();
                Bundle args = new Bundle();
                args.putString("username", username);
                fragment.setArguments(args);
                return fragment;
            } else if (position == 1) {
                return new frag2();
            } else {
                return new frag3();
            }
        }
    }
}