package mariam.darbinyan.login;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;

import java.util.List;

public class FavoritesActivity extends androidx.appcompat.app.AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        RecyclerView recyclerView = findViewById(R.id.favoritesRecyclerView);

        recyclerView.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 2));

        List<String> favoriteImages = new java.util.ArrayList<>();
        DressAdapter adapter = new DressAdapter(favoriteImages, "favorites", false);
        recyclerView.setAdapter(adapter);


        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String userId = user.getUid();

            String dbUrl = "https://mariam-sproject-default-rtdb.europe-west1.firebasedatabase.app/";

            com.google.firebase.database.DatabaseReference favRef =
                    com.google.firebase.database.FirebaseDatabase.getInstance(dbUrl)
                            .getReference("Users")
                            .child(userId)
                            .child("favorites");

            favRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                    favoriteImages.clear();
                    for (com.google.firebase.database.DataSnapshot data : snapshot.getChildren()) {
                        String img = data.getValue(String.class);
                        if (img != null) {
                            favoriteImages.add(img);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
            });
        } else {
            android.widget.Toast.makeText(this, "Please log in to see favorites", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}
