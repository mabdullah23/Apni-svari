package com.example.apni_svari;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

public class ZMainSellerPage extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_z_main_seller_page);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);


        toolbar.inflateMenu(R.menu.seller_top_menu);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_open_drawer) {
                drawerLayout.openDrawer(androidx.core.view.GravityCompat.START);
                return true;
            }
            return false;
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment fragment = null;

            if (id == R.id.nav_seller_home) {
                fragment = new ZsellerHome();
            } else if (id == R.id.nav_seller_history) {
                fragment = new ZsellerHistory();
            } else if (id == R.id.nav_seller_get_price) {
                fragment = new ZsellerGetPrice();
            } else if (id == R.id.nav_seller_message) {
                fragment = new ProposalsFragment();
            }

            if (fragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .commit();
                drawerLayout.closeDrawers();
            }

            return false;
        });

        // Load home fragment by default
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new ZsellerHome())
                    .commit();
            navigationView.setCheckedItem(R.id.nav_seller_home);
        }
    }

}

