package mariam.darbinyan.login;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class PantsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<String> pantsList;
    private DressAdapter adapter;
    private String dbUrl = "https://mariam-sproject-default-rtdb.europe-west1.firebasedatabase.app/";

    // 1. Launcher for Gallery
    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        processAndUpload(bitmap);
                    } catch (Exception e) {
                        Toast.makeText(this, "Gallery failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    // 2. Launcher for Camera
    private final ActivityResultLauncher<Void> mTakePicture = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(),
            bitmap -> {
                if (bitmap != null) {
                    processAndUpload(bitmap);
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

        boolean isSelectMode = getIntent().getBooleanExtra("SELECT_MODE", false);
        FloatingActionButton fab = findViewById(R.id.fab_add_pants);

        if (isSelectMode) {
            fab.setVisibility(View.GONE);
        }

        fab.setOnClickListener(v -> {
            String[] options = {"Take Photo", "Choose from Gallery"};
            new AlertDialog.Builder(this)
                    .setTitle("Add New Pants")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            mTakePicture.launch(null);
                        } else {
                            mGetContent.launch("image/*");
                        }
                    })
                    .show();
        });

        recyclerView = findViewById(R.id.recyclerViewPants);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        pantsList = new ArrayList<>();
        adapter = new DressAdapter(pantsList, "myPants", isSelectMode);
        recyclerView.setAdapter(adapter);

        loadPants();
    }

    // UPDATED: Matrix scaling for perfect proportions
    private void processAndUpload(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float maxSide = 600f;
        float scale = Math.min(maxSide / width, maxSide / height);

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);

        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        saveUrlToDatabase(imageEncoded);
    }

    // UPDATED: Simplified method signature
    private void saveUrlToDatabase(String imageUrl) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference dbRef = FirebaseDatabase.getInstance(dbUrl)
                .getReference("Users")
                .child(userId)
                .child("myPants");

        String pantsId = dbRef.push().getKey();
        if (pantsId != null) {
            dbRef.child(pantsId).setValue(imageUrl).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Pants added!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadPants() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference dbRef = FirebaseDatabase.getInstance(dbUrl)
                .getReference("Users")
                .child(userId).child("myPants");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pantsList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    String url = data.getValue(String.class);
                    if (url != null) {
                        pantsList.add(url);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
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