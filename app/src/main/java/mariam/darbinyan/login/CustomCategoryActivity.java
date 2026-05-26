package mariam.darbinyan.login;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import androidx.appcompat.widget.Toolbar;
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
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;

public class CustomCategoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<String> itemsList;
    private DressAdapter adapter;
    private String dbUrl = "https://mariam-sproject-default-rtdb.europe-west1.firebasedatabase.app/";

    private String categoryKey;
    private String categoryDisplayName;

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

        // 1. Point cleanly to your dedicated custom XML file!
        setContentView(R.layout.activity_custom_category);

        categoryDisplayName = getIntent().getStringExtra("CATEGORY_NAME");
        categoryKey = getIntent().getStringExtra("CATEGORY_KEY");
        if (categoryDisplayName == null) categoryDisplayName = "My Category";

        // 2. Dynamically set the big title text on the page to match the user's chosen name
        android.widget.TextView txtTitle = findViewById(R.id.txtCustomCategoryTitle);
        if (txtTitle != null) {
            txtTitle.setText(categoryDisplayName);
        }

        // Set up Toolbar explicitly to handle dynamic titles correctly
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        // Set up Action Bar title mapping securely
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(categoryDisplayName);
        }

        boolean isSelectMode = getIntent().getBooleanExtra("SELECT_MODE", false);
        FloatingActionButton fab = findViewById(R.id.fab_add_custom_item); // New FAB ID

        if (isSelectMode) {
            fab.setVisibility(View.GONE);
        }

        fab.setOnClickListener(v -> {
            String[] options = {"Take Photo", "Choose from Gallery"};
            new AlertDialog.Builder(this)
                    .setTitle("Add to " + categoryDisplayName)
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            mTakePicture.launch(null);
                        } else {
                            mGetContent.launch("image/*");
                        }
                    })
                    .show();
        });

        // 3. Bind to the isolated custom recycler grid container
        recyclerView = findViewById(R.id.recyclerViewCustomItems); // New RecyclerView ID
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        itemsList = new ArrayList<>();
        adapter = new DressAdapter(itemsList, categoryKey, isSelectMode);
        recyclerView.setAdapter(adapter);

        loadCategoryItems();
    }

    private void processAndUpload(Bitmap bitmap) {
        Toast.makeText(this, "AI is processing...", Toast.LENGTH_SHORT).show();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] bitmapData = baos.toByteArray();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "clothing.jpg",
                        RequestBody.create(MediaType.parse("image/jpeg"), bitmapData))
                .addFormDataPart("format", "PNG")
                .build();

        Request request = new Request.Builder()
                .url("https://api.picsart.io/tools/1.0/removebg")
                .post(requestBody)
                .addHeader("X-Picsart-API-Key", getString(R.string.picsart_api_key))
                .build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(CustomCategoryActivity.this, "AI Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override

            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonString = response.body().string(); // Get the JSON text
                    try {
                        // Parse the JSON to get the "url" field
                        org.json.JSONObject json = new org.json.JSONObject(jsonString);
                        String imageUrl = json.getJSONObject("data").getString("url");

                        // Save this URL to the database
                        runOnUiThread(() -> saveUrlToDatabase(imageUrl));
                    } catch (Exception e) {
                        android.util.Log.e("FILE_DEBUG", "JSON Parsing Error: " + e.getMessage());
                    }
                }
            }
        });
    }

    private void saveUrlToDatabase(String fileName) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance(dbUrl).getReference("Users")
                .child(userId).child(categoryKey).push().setValue(fileName);
    }

    private void loadCategoryItems() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference dbRef = FirebaseDatabase.getInstance(dbUrl)
                .getReference("Users")
                .child(userId).child(categoryKey);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemsList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    String url = data.getValue(String.class);
                    if (url != null) {
                        itemsList.add(url);
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