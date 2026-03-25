package mariam.darbinyan.login;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class DressActivity extends AppCompatActivity {

    private androidx.recyclerview.widget.RecyclerView recyclerView;
    private java.util.List<String> dressList;
    private DressAdapter adapter;

    private void uploadImageToFirebase(Uri uri) {
        try {
            android.graphics.Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

            // 1. Shrink it more (250x250 is plenty for a grid view)
            android.graphics.Bitmap scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, 250, 250, true);

            // 2. Compress it more (50% quality)
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 50, baos);

            byte[] b = baos.toByteArray();
            String imageEncoded = android.util.Base64.encodeToString(b, android.util.Base64.DEFAULT);

            // 3. Save to Database
            String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
            saveUrlToDatabase(userId, imageEncoded);

        } catch (Exception e) {
            Toast.makeText(this, "Process failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void saveUrlToDatabase(String userId, String imageUrl) {
        String dbUrl = "https://mariam-sproject-default-rtdb.europe-west1.firebasedatabase.app/";
        // 5. Save the URL under the user's specific folder in the database
        DatabaseReference dbRef =
                FirebaseDatabase.getInstance(dbUrl).getReference("Users")
                        .child(userId)
                        .child("myDresses");

        String dressId = dbRef.push().getKey(); // Generates a unique ID for this specific dress
        dbRef.child(dressId).setValue(imageUrl).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Dress added to your wardrobe!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // This launcher handles the result of picking an image
    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    uploadImageToFirebase(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dress);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // 2. Initialize the Add Button
        com.google.android.material.floatingactionbutton.FloatingActionButton fab =
                findViewById(R.id.fab_add_dress);

        findViewById(R.id.fab_add_dress).setOnClickListener(v -> {
            // This line opens the phone's gallery
            mGetContent.launch("image/*");
        });

        recyclerView = findViewById(R.id.recyclerViewDresses);
// This makes it a 2-column grid
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 2));

        dressList = new java.util.ArrayList<>();
        adapter = new DressAdapter(dressList, "myDresses");
        recyclerView.setAdapter(adapter);

// Load the photos when the app starts
        loadDresses();
    }
    private void loadDresses() {
        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();

        // --- FIX IS HERE: Add your Belgium URL ---
        String dbUrl = "https://mariam-sproject-default-rtdb.europe-west1.firebasedatabase.app/";

        com.google.firebase.database.DatabaseReference dbRef =
                com.google.firebase.database.FirebaseDatabase.getInstance(dbUrl) // Use the URL!
                        .getReference("Users")
                        .child(userId).child("myDresses");

        dbRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                dressList.clear();
                for (com.google.firebase.database.DataSnapshot data : snapshot.getChildren()) {
                    String url = data.getValue(String.class);
                    if (url != null) {
                        dressList.add(url);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Go back to HomeActivity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}