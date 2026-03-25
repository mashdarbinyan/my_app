package mariam.darbinyan.login;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.List;

public class PantsActivity extends AppCompatActivity {

    private androidx.recyclerview.widget.RecyclerView recyclerView;
    private List<String> pantsList;
    private DressAdapter adapter; // You can reuse DressAdapter!

    private void uploadImageToFirebase(Uri uri) {
        try {
            android.graphics.Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

            // Shrink and Compress
            android.graphics.Bitmap scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, 250, 250, true);
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 50, baos);

            byte[] b = baos.toByteArray();
            String imageEncoded = android.util.Base64.encodeToString(b, android.util.Base64.DEFAULT);

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            saveUrlToDatabase(userId, imageEncoded);

        } catch (Exception e) {
            Toast.makeText(this, "Process failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUrlToDatabase(String userId, String imageUrl) {
        String dbUrl = "https://mariam-sproject-default-rtdb.europe-west1.firebasedatabase.app/";

        // --- CHANGED TO "myPants" ---
        DatabaseReference dbRef = FirebaseDatabase.getInstance(dbUrl).getReference("Users")
                .child(userId)
                .child("myPants");

        String pantsId = dbRef.push().getKey();
        dbRef.child(pantsId).setValue(imageUrl).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Pants added to your wardrobe!", Toast.LENGTH_SHORT).show();
            }
        });
    }

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
        setContentView(R.layout.activity_pants);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Pants");
        }

        findViewById(R.id.fab_add_pants).setOnClickListener(v -> {
            mGetContent.launch("image/*");
        });

        recyclerView = findViewById(R.id.recyclerViewPants);
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 2));

        pantsList = new ArrayList<>();
        adapter = new DressAdapter(pantsList, "myPants");
        recyclerView.setAdapter(adapter);

        loadPants();
    }

    private void loadPants() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String dbUrl = "https://mariam-sproject-default-rtdb.europe-west1.firebasedatabase.app/";

        DatabaseReference dbRef = FirebaseDatabase.getInstance(dbUrl)
                .getReference("Users")
                .child(userId).child("myPants");

        dbRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                pantsList.clear();
                for (com.google.firebase.database.DataSnapshot data : snapshot.getChildren()) {
                    String url = data.getValue(String.class);
                    if (url != null) {
                        pantsList.add(url);
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
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}