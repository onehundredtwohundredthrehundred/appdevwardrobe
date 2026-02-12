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
            } else if (itemId == R.id.nav_wash) {
                viewPager.setCurrentItem(2);
                return true;
            } else if (itemId == R.id.nav_settings) {
                viewPager.setCurrentItem(3);
                return true;
            }
            return false;
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        bottomNavigationView.setSelectedItemId(R.id.nav_wardrobe);
                        break;
                    case 1:
                        bottomNavigationView.setSelectedItemId(R.id.nav_outfits);
                        break;
                    case 2:
                        bottomNavigationView.setSelectedItemId(R.id.nav_wash);
                        break;
                    case 3:
                        bottomNavigationView.setSelectedItemId(R.id.nav_settings);
                        break;
                }
            }
        });
    }

    public void switchToWashTab() {
        viewPager.setCurrentItem(2);
        bottomNavigationView.setSelectedItemId(R.id.nav_wash);
    }

    private class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @Override
        public int getItemCount() {
            return 4;
        }

        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    String username = getIntent().getStringExtra("username");
                    if (username == null) {
                        username = "User";
                    }
                    frag1 fragment = new frag1();
                    Bundle args = new Bundle();
                    args.putString("username", username);
                    fragment.setArguments(args);
                    return fragment;

                case 1:
                    return new frag2();

                case 2:
                    return new frag3();

                case 3:
                    return new frag4();

                default:
                    return new frag1();
            }
        }
    }
}