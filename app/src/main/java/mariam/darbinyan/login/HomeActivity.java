package mariam.darbinyan.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    BottomNavigationView bottomNav;
    CardView dressCard, pantsCard, shoesCard, jacketCard, btnMyLooks;
    FirebaseAuth auth;

    private RecyclerView customCatRecyclerView;
    private CategoryAdapter categoryAdapter;
    private List<CategoryModel> customCategoryList;
    private Button btnAddCustomCategory;
    private String dbUrl = "https://mariam-sproject-default-rtdb.europe-west1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            if ("innovationcampus26@gmail.com".equalsIgnoreCase(currentUser.getEmail())) {
                setupUI(currentUser);
            } else {
                currentUser.reload().addOnCompleteListener(task -> {
                    if (currentUser.isEmailVerified()) {
                        setupUI(currentUser);
                    } else {
                        auth.signOut();
                        Toast.makeText(this, "Please verify your email.", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(HomeActivity.this, MainActivity.class));
                        finish();
                    }
                });
            }
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
        bottomNav = findViewById(R.id.bottom_navigation);

        View headerView = navigationView.getHeaderView(0);
        TextView emailTextView = headerView.findViewById(R.id.user_email_header);
        if (emailTextView != null) emailTextView.setText(user.getEmail());

        dressCard = findViewById(R.id.DressCard);
        pantsCard = findViewById(R.id.PantsCard);
        shoesCard = findViewById(R.id.ShoesCard);
        jacketCard = findViewById(R.id.JacketCard);
        btnMyLooks = findViewById(R.id.btnMyLooks);

        dressCard.setOnClickListener(v -> startActivity(new Intent(this, DressActivity.class)));
        pantsCard.setOnClickListener(v -> startActivity(new Intent(this, PantsActivity.class)));
        shoesCard.setOnClickListener(v -> startActivity(new Intent(this, ShoesActivity.class)));
        jacketCard.setOnClickListener(v -> startActivity(new Intent(this, JacketActivity.class)));
        if (btnMyLooks != null) btnMyLooks.setOnClickListener(v -> startActivity(new Intent(this, MyLooksActivity.class)));


        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.bottom_favorites) {
                startActivity(new Intent(this, FavoritesActivity.class));
            } else if (id == R.id.bottom_add) {
                startActivity(new Intent(this, ChatActivity.class));
            } else if (id == R.id.bottom_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
            }
            return true;
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) startActivity(new Intent(HomeActivity.this, AccountActivity.class));
            else if (id == R.id.nav_logout) { auth.signOut(); startActivity(new Intent(HomeActivity.this, MainActivity.class)); finish(); }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        setupCustomCategoriesList();
    }

    private void setupCustomCategoriesList() {
        btnAddCustomCategory = findViewById(R.id.btn_add_custom_category);
        customCatRecyclerView = findViewById(R.id.recyclerViewCustomCategories);
        customCategoryList = new ArrayList<>();

        categoryAdapter = new CategoryAdapter(customCategoryList, this::showDeleteConfirmationDialog);
        customCatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        customCatRecyclerView.setAdapter(categoryAdapter);
        if (btnAddCustomCategory != null) btnAddCustomCategory.setOnClickListener(v -> showCreateCategoryDialog());
        loadCustomCategoriesFromFirebase();
    }

    private void showDeleteConfirmationDialog(CategoryModel category) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Delete '" + category.getName() + "' and all its items?")
                .setPositiveButton("Delete", (dialog, which) -> deleteCategory(category))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteCategory(CategoryModel category) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference rootRef = FirebaseDatabase.getInstance(dbUrl).getReference("Users").child(userId);
        rootRef.child("custom_categories_list").child(category.getKey()).removeValue();
        rootRef.child(category.getKey()).removeValue().addOnSuccessListener(aVoid ->
                Toast.makeText(this, "Category and items deleted", Toast.LENGTH_SHORT).show());
    }

    private void showCreateCategoryDialog() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("e.g., Summer Outfits");
        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding / 2, padding, 0);
        container.addView(input);

        new AlertDialog.Builder(this)
                .setTitle("Create Custom Category")
                .setView(container)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) saveCustomCategoryToFirebase(name);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveCustomCategoryToFirebase(String name) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String key = "custom_" + name.toLowerCase().trim().replace(" ", "_");
        FirebaseDatabase.getInstance(dbUrl).getReference("Users").child(userId).child("custom_categories_list").child(key).setValue(name);
    }

    private void loadCustomCategoriesFromFirebase() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance(dbUrl).getReference("Users").child(userId).child("custom_categories_list")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        customCategoryList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            customCategoryList.add(new CategoryModel(data.getKey(), data.getValue(String.class)));
                        }
                        categoryAdapter.notifyDataSetChanged();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}