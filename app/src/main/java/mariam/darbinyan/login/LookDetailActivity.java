package mariam.darbinyan.login;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LookDetailActivity extends AppCompatActivity {

    private String dbUrl = "https://mariam-sproject-default-rtdb.europe-west1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_look_detail);

        TextView tvName = findViewById(R.id.tvDetailName);
        ImageView imgDress = findViewById(R.id.imgDetailDress);
        ImageView imgJacket = findViewById(R.id.imgDetailJacket);
        ImageView imgPants = findViewById(R.id.imgDetailPants);
        ImageView imgShoes = findViewById(R.id.imgDetailShoes);
        Button btnBack = findViewById(R.id.btnBack);

        // Get data from Intent
        String name = getIntent().getStringExtra("lookName");
        tvName.setText(name);

        // Display images
        displayImage(getIntent().getStringExtra("dress"), imgDress);
        displayImage(getIntent().getStringExtra("jacket"), imgJacket);
        displayImage(getIntent().getStringExtra("pants"), imgPants);
        displayImage(getIntent().getStringExtra("shoes"), imgShoes);

        // 1. Long-click to remove specific items from the look
        setupRemoveListener(imgDress, "dress", name);
        setupRemoveListener(imgJacket, "jacket", name);
        setupRemoveListener(imgPants, "pants", name);
        setupRemoveListener(imgShoes, "shoes", name);

        // 2. Click button to delete the FULL LOOK
        btnBack.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Full Look")
                    .setMessage("Are you sure you want to delete this entire outfit?")
                    .setPositiveButton("Delete", (dialog, which) -> deleteFullLook(name))
                    .setNegativeButton("Back", (dialog, which) -> finish())
                    .show();
        });
    }

    private void setupRemoveListener(ImageView img, String itemType, String lookName) {
        img.setOnLongClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Remove Item")
                    .setMessage("Remove this " + itemType + " from this look?")
                    .setPositiveButton("Remove", (dialog, which) -> {
                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference ref = FirebaseDatabase.getInstance(dbUrl)
                                .getReference("Users").child(userId).child("myLooks");

                        // Find the look by name and clear only the specific item field
                        ref.orderByChild("lookName").equalTo(lookName).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot data : snapshot.getChildren()) {
                                    data.getRef().child(itemType).setValue("");
                                }
                                img.setImageDrawable(null); // Clear from UI
                                Toast.makeText(LookDetailActivity.this, itemType + " removed from look", Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });
    }

    private void deleteFullLook(String lookName) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance(dbUrl)
                .getReference("Users").child(userId).child("myLooks");

        ref.orderByChild("lookName").equalTo(lookName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    data.getRef().removeValue();
                }
                Toast.makeText(LookDetailActivity.this, "Outfit Deleted", Toast.LENGTH_SHORT).show();
                finish(); // Close activity
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void displayImage(String b64, ImageView img) {
        if (b64 != null && !b64.isEmpty()) {
            byte[] decoded = Base64.decode(b64, Base64.DEFAULT);
            Bitmap bit = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            img.setImageBitmap(bit);
        } else {
            img.setVisibility(View.GONE); // Hide the box if no image exists
        }
    }
}
