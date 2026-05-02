package com.example.apni_svari;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainUserPage extends AppCompatActivity {

    private View buyerDetailContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_user_page);
        buyerDetailContainer = findViewById(R.id.buyerDetailContainer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0 && buyerDetailContainer != null) {
                buyerDetailContainer.setVisibility(View.GONE);
            }
        });

        android.widget.TextView nameView = findViewById(R.id.userNameText);
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            nameView.setText(getString(R.string.hello_user_with_name, user.getDisplayName()));
        } else if (user != null && user.getEmail() != null) {
            nameView.setText(getString(R.string.hello_user_with_name, user.getEmail()));
        }

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        UserPagerAdapter adapter = new UserPagerAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(5);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                viewPager.setCurrentItem(0, true);
                return true;
            } else if (id == R.id.nav_search) {
                viewPager.setCurrentItem(1, true);
                return true;
            } else if (id == R.id.nav_favourite) {
                viewPager.setCurrentItem(2, true);
                return true;
            } else if (id == R.id.nav_profile) {
                viewPager.setCurrentItem(3, true);
                return true;
            } else if (id == R.id.nav_sell) {
                viewPager.setCurrentItem(4, true);
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
                        bottomNavigationView.setSelectedItemId(R.id.nav_home);
                        break;
                    case 1:
                        bottomNavigationView.setSelectedItemId(R.id.nav_search);
                        break;
                    case 2:
                        bottomNavigationView.setSelectedItemId(R.id.nav_favourite);
                        break;
                    case 3:
                        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
                        break;
                    case 4:
                    default:
                        bottomNavigationView.setSelectedItemId(R.id.nav_sell);
                        break;
                }
            }
        });

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    public void openBuyerDetailFragment(androidx.fragment.app.Fragment fragment) {
        if (buyerDetailContainer != null) {
            buyerDetailContainer.setVisibility(View.VISIBLE);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.buyerDetailContainer, fragment)
                .addToBackStack(null)
                .commit();
    }
}