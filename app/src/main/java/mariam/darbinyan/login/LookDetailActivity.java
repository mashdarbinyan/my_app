package mariam.darbinyan.login;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.File;

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

        String name = getIntent().getStringExtra("lookName");
        tvName.setText(name);

        displayImage(getIntent().getStringExtra("dress"), imgDress);
        displayImage(getIntent().getStringExtra("jacket"), imgJacket);
        displayImage(getIntent().getStringExtra("pants"), imgPants);
        displayImage(getIntent().getStringExtra("shoes"), imgShoes);

        setupRemoveListener(imgDress, "dress", name);
        setupRemoveListener(imgJacket, "jacket", name);
        setupRemoveListener(imgPants, "pants", name);
        setupRemoveListener(imgShoes, "shoes", name);

        btnBack.setOnClickListener(v -> finish());
    }

    private void displayImage(String content, ImageView img) {
        if (content != null && !content.isEmpty()) {
            img.setVisibility(View.VISIBLE);
            if (content.startsWith("http")) {
                Glide.with(this).load(content).into(img);
            } else {
                Glide.with(this).load(new File(getFilesDir(), content)).into(img);
            }
        } else {
            img.setVisibility(View.GONE);
        }
    }

    private void setupRemoveListener(ImageView img, String itemType, String lookName) {
        img.setOnLongClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Remove Item")
                    .setMessage("Remove this " + itemType + "?")
                    .setPositiveButton("Remove", (dialog, which) -> {
                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference ref = FirebaseDatabase.getInstance(dbUrl).getReference("Users").child(userId).child("myLooks");
                        ref.orderByChild("lookName").equalTo(lookName).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot data : snapshot.getChildren()) data.getRef().child(itemType).setValue("");
                                img.setVisibility(View.GONE);
                            }
                            @Override public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }).setNegativeButton("Cancel", null).show();
            return true;
        });
    }
}
