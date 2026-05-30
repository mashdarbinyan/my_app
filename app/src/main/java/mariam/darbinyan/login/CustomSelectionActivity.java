package mariam.darbinyan.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class CustomSelectionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DressAdapter adapter; // You can reuse your DressAdapter!
    private List<String> imageList = new ArrayList<>();
    private String categoryKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dress);

        categoryKey = getIntent().getStringExtra("CATEGORY_KEY");
        recyclerView = findViewById(R.id.recyclerViewDresses);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));


        adapter = new DressAdapter(imageList, categoryKey, true);
        recyclerView.setAdapter(adapter);

        loadItemsFromFirebase();
    }

    private void loadItemsFromFirebase() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance("https://mariam-sproject-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Users").child(userId).child(categoryKey);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                imageList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    String url = data.getValue(String.class);
                    if (url != null) imageList.add(url);
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}