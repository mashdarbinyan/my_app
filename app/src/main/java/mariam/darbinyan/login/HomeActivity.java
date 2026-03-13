package mariam.darbinyan.login;

import android.os.Bundle;
import android.widget.TextView;
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
import android.view.MenuItem;
import androidx.annotation.NonNull;
import android.view.View;

public class HomeActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    BottomNavigationView bottomNav;
    CardView dressCard, pantsCard, shoesCard, jacketCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);


        dressCard = findViewById(R.id.DressCard);
        pantsCard = findViewById(R.id.PantsCard);
        shoesCard = findViewById(R.id.ShoesCard);
        jacketCard = findViewById(R.id.JacketCard);

        dressCard.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, DressActivity.class);
            startActivity(intent);
        });

        pantsCard.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, PantsActivity.class);
            startActivity(intent);
        });

        shoesCard.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ShoesActivity.class);
            startActivity(intent);
        });

        jacketCard.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, JacketActivity.class);
            startActivity(intent);
        });


        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_logout) {
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
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

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_add) {
                showBottomSheet();
            }
            return true;
        });

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        View headerView = navigationView.getHeaderView(0);
        TextView emailTextView = headerView.findViewById(R.id.user_email_header);

        if (currentUser != null) {
            emailTextView.setText(currentUser.getEmail());
        }
    }

    private void showBottomSheet() {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheet =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_layout, null);
        bottomSheet.setContentView(view);
        bottomSheet.show();
    }

}