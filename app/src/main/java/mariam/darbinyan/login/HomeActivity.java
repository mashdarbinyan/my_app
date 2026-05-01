package mariam.darbinyan.login;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import android.view.View;

public class HomeActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    BottomNavigationView bottomNav;
    CardView dressCard, pantsCard, shoesCard, jacketCard;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        // CHECK VERIFICATION STATUS
        if (currentUser != null) {
            currentUser.reload().addOnCompleteListener(task -> {
                if (currentUser.isEmailVerified()) {
                    // User is verified, set up the UI normally
                    setupUI(currentUser);
                } else {
                    // Not verified, kick them back to Login
                    auth.signOut();
                    Toast.makeText(this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(HomeActivity.this, MainActivity.class));
                    finish();
                }
            });
        } else {
            // No user logged in at all
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();
        }
    }

    private void setupUI(FirebaseUser user) {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Header Email Setup
        View headerView = navigationView.getHeaderView(0);
        TextView emailTextView = headerView.findViewById(R.id.user_email_header);
        emailTextView.setText(user.getEmail());

        dressCard = findViewById(R.id.DressCard);
        pantsCard = findViewById(R.id.PantsCard);
        shoesCard = findViewById(R.id.ShoesCard);
        jacketCard = findViewById(R.id.JacketCard);

        // Click Listeners
        dressCard.setOnClickListener(v -> startActivity(new Intent(this, DressActivity.class)));
        pantsCard.setOnClickListener(v -> startActivity(new Intent(this, PantsActivity.class)));
        shoesCard.setOnClickListener(v -> startActivity(new Intent(this, ShoesActivity.class)));
        jacketCard.setOnClickListener(v -> startActivity(new Intent(this, JacketActivity.class)));

        // Navigation Setup
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_logout) {
                auth.signOut();
                startActivity(new Intent(HomeActivity.this, MainActivity.class));
                finish();
            } else if (id == R.id.bottom_add) {
                showBottomSheet();
            }
            drawerLayout.closeDrawers();
            return true;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

// new
        bottomNav = findViewById(R.id.bottom_navigation);

        if (bottomNav != null) { // Prevents crash if ID is wrong
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.bottom_favorites) {
                    startActivity(new Intent(HomeActivity.this, FavoritesActivity.class));
                    return true;
                }
                else if (id == R.id.bottom_add) {
                    // This is your AI Chat button
                    startActivity(new Intent(HomeActivity.this, ChatActivity.class));
                    return true;
                }
                else if (id == R.id.bottom_settings) {
                    Toast.makeText(this, "Settings coming soon!", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });
        } else {
            android.util.Log.e("DEBUG_MYLOOK", "Bottom Navigation View is NULL. Check activity_home.xml IDs!");
        }
    }

    private void showBottomSheet() {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheet =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_layout, null);
        android.widget.Button btnStartChat = view.findViewById(R.id.btn_start_chat);

        btnStartChat.setOnClickListener(v -> {
            bottomSheet.dismiss();
            startActivity(new Intent(HomeActivity.this, ChatActivity.class));
        });

        bottomSheet.setContentView(view);
        bottomSheet.show();
    }
}