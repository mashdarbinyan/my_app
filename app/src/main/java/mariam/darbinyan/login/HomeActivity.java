package mariam.darbinyan.login;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;

public class HomeActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    BottomNavigationView bottomNav;
    CardView dressCard, pantsCard, shoesCard, jacketCard, btnMyLooks;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            currentUser.reload().addOnCompleteListener(task -> {
                if (currentUser.isEmailVerified()) {
                    setupUI(currentUser);
                } else {
                    auth.signOut();
                    Toast.makeText(this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(HomeActivity.this, MainActivity.class));
                    finish();
                }
            });
        } else {
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();
        }

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        if (prefs.getBoolean("DarkMode", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void setupUI(FirebaseUser user) {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Header Email Setup
        View headerView = navigationView.getHeaderView(0);
        TextView emailTextView = headerView.findViewById(R.id.user_email_header);
        if (emailTextView != null) {
            emailTextView.setText(user.getEmail());
        }

        dressCard = findViewById(R.id.DressCard);
        pantsCard = findViewById(R.id.PantsCard);
        shoesCard = findViewById(R.id.ShoesCard);
        jacketCard = findViewById(R.id.JacketCard);
        btnMyLooks = findViewById(R.id.btnMyLooks);

        // Category Click Listeners
        dressCard.setOnClickListener(v -> startActivity(new Intent(this, DressActivity.class)));
        pantsCard.setOnClickListener(v -> startActivity(new Intent(this, PantsActivity.class)));
        shoesCard.setOnClickListener(v -> startActivity(new Intent(this, ShoesActivity.class)));
        jacketCard.setOnClickListener(v -> startActivity(new Intent(this, JacketActivity.class)));

        if (btnMyLooks != null) {
            btnMyLooks.setOnClickListener(v -> startActivity(new Intent(this, MyLooksActivity.class)));
        }

        // --- DRAWER NAVIGATION SETUP ---
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // Already on home
            } else if (id == R.id.nav_profile) {
                // Opens your new Account/Profile interface
                startActivity(new Intent(HomeActivity.this, AccountActivity.class));
            } else if (id == R.id.nav_logout) {
                auth.signOut();
                startActivity(new Intent(HomeActivity.this, MainActivity.class));
                finish();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // --- BOTTOM NAVIGATION SETUP ---
        bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.bottom_favorites) {
                    startActivity(new Intent(HomeActivity.this, FavoritesActivity.class));
                    return true;
                } else if (id == R.id.bottom_add) {
                    startActivity(new Intent(HomeActivity.this, ChatActivity.class));
                    return true;
                } else if (id == R.id.bottom_settings) {
                    // Opens your new Settings interface
                    startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
                    return true;
                }
                return false;
            });
        }
    }
}