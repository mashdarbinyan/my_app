package mariam.darbinyan.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MyLooksActivity extends AppCompatActivity {

    private RecyclerView rvMyLooks;
    private FloatingActionButton fabNewLook;

    private java.util.ArrayList<java.util.Map<String, String>> lookList;
    private LookAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_looks);

        rvMyLooks = findViewById(R.id.rvMyLooks);
        fabNewLook = findViewById(R.id.fabNewLook);

        // Setup the list (we'll add the adapter later)
        rvMyLooks.setLayoutManager(new LinearLayoutManager(this));

        // When you click +, go to the Creation screen
        fabNewLook.setOnClickListener(v -> {
            Intent intent = new Intent(MyLooksActivity.this, CreateLookActivity.class);
            startActivity(intent);
        });

        lookList = new java.util.ArrayList<>();
        adapter = new LookAdapter(lookList);
        rvMyLooks.setAdapter(adapter);

        loadLooksFromFirebase();
    }

    private void loadLooksFromFirebase() {
        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        com.google.firebase.database.DatabaseReference ref = com.google.firebase.database.FirebaseDatabase.getInstance("https://mariam-sproject-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Users").child(userId).child("myLooks");

        ref.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                lookList.clear();
                for (com.google.firebase.database.DataSnapshot data : snapshot.getChildren()) {
                    java.util.Map<String, String> look = (java.util.Map<String, String>) data.getValue();
                    lookList.add(look);
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
        });
    }
}