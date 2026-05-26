package mariam.darbinyan.login;

import android.graphics.Bitmap;
import android.os.Bundle;
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
import java.io.IOException;
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

public class JacketActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<String> jacketList;
    private DressAdapter adapter;
    private String dbUrl = "https://mariam-sproject-default-rtdb.europe-west1.firebasedatabase.app/";

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> { if (uri != null) processImage(uri); });

    private final ActivityResultLauncher<Void> mTakePicture = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(),
            bitmap -> { if (bitmap != null) processAndUpload(bitmap); });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_jacket);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Jackets");
        }

        boolean isSelectMode = getIntent().getBooleanExtra("SELECT_MODE", false);
        FloatingActionButton fab = findViewById(R.id.fab_add_jackets);

        if (isSelectMode) fab.setVisibility(View.GONE);

        fab.setOnClickListener(v -> {
            new AlertDialog.Builder(this).setTitle("Add New Jacket").setItems(new String[]{"Take Photo", "Choose from Gallery"}, (dialog, which) -> {
                if (which == 0) mTakePicture.launch(null); else mGetContent.launch("image/*");
            }).show();
        });

        recyclerView = findViewById(R.id.recyclerViewJackets);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        jacketList = new ArrayList<>();
        adapter = new DressAdapter(jacketList, "myJackets", isSelectMode);
        recyclerView.setAdapter(adapter);
        loadJackets();
    }

    private void processImage(android.net.Uri uri) {
        try {
            processAndUpload(android.provider.MediaStore.Images.Media.getBitmap(getContentResolver(), uri));
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void processAndUpload(Bitmap bitmap) {
        Toast.makeText(this, "AI is removing background...", Toast.LENGTH_SHORT).show();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, baos);
        byte[] bitmapData = baos.toByteArray();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "jacket.png", RequestBody.create(MediaType.parse("image/png"), bitmapData))
                .build();

        Request request = new Request.Builder()
                .url("https://api.picsart.io/tools/1.0/removebg")
                .post(requestBody)
                .addHeader("X-Picsart-API-Key", getString(R.string.picsart_api_key))
                .build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(JacketActivity.this, "AI Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonString = response.body().string();
                        String imageUrl = new org.json.JSONObject(jsonString).getJSONObject("data").getString("url");
                        runOnUiThread(() -> saveUrlToDatabase(imageUrl));
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
        });
    }

    private void saveUrlToDatabase(String imageUrl) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance(dbUrl).getReference("Users").child(userId).child("myJackets").push().setValue(imageUrl);
    }

    private void loadJackets() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance(dbUrl).getReference("Users").child(userId).child("myJackets").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                jacketList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    String url = data.getValue(String.class);
                    if (url != null) jacketList.add(url);
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();
        return super.onOptionsItemSelected(item);
    }
}