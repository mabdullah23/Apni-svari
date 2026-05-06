package com.example.apni_svari;

import android.os.Bundle;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ZMainSellerPage extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_z_main_seller_page);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        
        // Setup ActionBarDrawerToggle for hamburger menu
        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.seller_open_drawer,
                R.string.seller_close_drawer
        );
        // Ensure the hamburger (drawer) icon is white
        try {
            toggle.getDrawerArrowDrawable().setColor(ContextCompat.getColor(this, android.R.color.white));
        } catch (Exception ignored) {}
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        setupDrawerHeader();

        navigationView.setNavigationItemSelectedListener(item -> handleNavigationMenu(item));

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new ZsellerHome())
                    .commit();
            navigationView.setCheckedItem(R.id.nav_seller_home);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (drawerLayout.isDrawerOpen(androidx.core.view.GravityCompat.START)) {
            drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START);
        } else {
            // Go back to previous page instead of closing app
            onBackPressed();
        }
        return true;
    }

    private void setupDrawerHeader() {
        ImageView profileImage = navigationView.getHeaderView(0).findViewById(R.id.drawerProfileImage);
        TextView userName = navigationView.getHeaderView(0).findViewById(R.id.drawerUserName);
        TextView userEmail = navigationView.getHeaderView(0).findViewById(R.id.drawerUserEmail);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "No email");

            FirebaseFirestore.getInstance().collection("users")
                    .document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("username");
                            if (name == null) {
                                name = documentSnapshot.getString("name");
                            }
                            if (name == null) {
                                name = currentUser.getDisplayName();
                            }
                            if (name != null && !name.isEmpty()) {
                                userName.setText(name);
                            } else {
                                userName.setText("Seller");
                            }
                        }
                    })
                    .addOnFailureListener(e -> userName.setText("Seller"));
        }
    }

    private boolean handleNavigationMenu(android.view.MenuItem item) {
        int id = item.getItemId();
        Fragment fragment = null;
        boolean validSelection = true;

        if (id == R.id.nav_seller_home) {
            fragment = new ZsellerHome();
        } else if (id == R.id.nav_seller_history) {
            fragment = new ZsellerHistory();
        } else if (id == R.id.nav_seller_get_price) {
            fragment = new ZsellerGetPrice();
        } else if (id == R.id.nav_seller_message) {
            fragment = new ProposalsFragment();
        } else if (id == R.id.nav_seller_logout) {
            handleLogout();
            return true;
        } else {
            validSelection = false;
        }

        if (validSelection && fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
            drawerLayout.closeDrawers();
            return true;
        }

        return false;
    }

    private void handleLogout() {
        drawerLayout.closeDrawers();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(ZMainSellerPage.this, MainRegPage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // If drawer is open, close it first
        if (drawerLayout != null && drawerLayout.isDrawerOpen(androidx.core.view.GravityCompat.START)) {
            drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START);
            return;
        }

        // If there are fragments in back stack, pop one
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            return;
        }

        // Otherwise navigate back to Ask_user (what would you like to do) instead of closing the app
        Intent intent = new Intent(ZMainSellerPage.this, Ask_user.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}

