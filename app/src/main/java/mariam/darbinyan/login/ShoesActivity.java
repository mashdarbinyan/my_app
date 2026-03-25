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

public class ShoesActivity extends AppCompatActivity {

    private androidx.recyclerview.widget.RecyclerView recyclerView;
    private List<String> shoesList;
    private DressAdapter adapter;
    private String dbUrl = "https://mariam-sproject-default-rtdb.europe-west1.firebasedatabase.app/";

    private void uploadImageToFirebase(Uri uri) {
        try {
            android.graphics.Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            // Same 250x250 size to keep the database fast
            android.graphics.Bitmap scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, 250, 250, true);
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 50, baos);
            String imageEncoded = android.util.Base64.encodeToString(baos.toByteArray(), android.util.Base64.DEFAULT);

            saveUrlToDatabase(imageEncoded);
        } catch (Exception e) {
            Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUrlToDatabase(String imageUrl) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // --- PATH CHANGED TO myShoes ---
        DatabaseReference dbRef = FirebaseDatabase.getInstance(dbUrl).getReference("Users")
                .child(userId).child("myShoes");

        dbRef.push().setValue(imageUrl).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Shoes added to your wardrobe!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> { if (uri != null) uploadImageToFirebase(uri); }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shoes);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Shoes");
        }

        // Make sure this ID matches activity_shoes.xml
        findViewById(R.id.fab_add_shoes).setOnClickListener(v -> mGetContent.launch("image/*"));

        recyclerView = findViewById(R.id.recyclerViewShoes);
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 2));

        shoesList = new ArrayList<>();
        adapter = new DressAdapter(shoesList, "myShoes");
        recyclerView.setAdapter(adapter);

        loadShoes();
    }

    private void loadShoes() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // --- PATH CHANGED TO myShoes ---
        DatabaseReference dbRef = FirebaseDatabase.getInstance(dbUrl).getReference("Users")
                .child(userId).child("myShoes");

        dbRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                shoesList.clear();
                for (com.google.firebase.database.DataSnapshot data : snapshot.getChildren()) {
                    String url = data.getValue(String.class);
                    if (url != null) shoesList.add(url);
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