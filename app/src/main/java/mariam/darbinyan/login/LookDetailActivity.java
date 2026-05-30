package mariam.darbinyan.login;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private String lookId, lookName;
    private LinearLayout addedItemsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_look_detail);


        lookId = getIntent().getStringExtra("lookId");
        lookName = getIntent().getStringExtra("lookName");
        String extraItems = getIntent().getStringExtra("extraItems");

        TextView tvName = findViewById(R.id.tvDetailName);
        ImageView imgDress = findViewById(R.id.imgDetailDress);
        ImageView imgJacket = findViewById(R.id.imgDetailJacket);
        ImageView imgPants = findViewById(R.id.imgDetailPants);
        ImageView imgShoes = findViewById(R.id.imgDetailShoes);
        addedItemsContainer = findViewById(R.id.addedItemsContainer);
        Button btnDeleteLook = findViewById(R.id.btnDeleteLook);
        Button btnBack = findViewById(R.id.btnBack);

        tvName.setText(lookName);


        displayImage(getIntent().getStringExtra("dress"), imgDress);
        displayImage(getIntent().getStringExtra("jacket"), imgJacket);
        displayImage(getIntent().getStringExtra("pants"), imgPants);
        displayImage(getIntent().getStringExtra("shoes"), imgShoes);


        if (extraItems != null && !extraItems.isEmpty() && !extraItems.equals("[]")) {
            displayExtraItems(extraItems);
        }


        setupRemoveListener(imgDress, "dress");
        setupRemoveListener(imgJacket, "jacket");
        setupRemoveListener(imgPants, "pants");
        setupRemoveListener(imgShoes, "shoes");

        btnDeleteLook.setOnClickListener(v -> confirmDeleteLook());
        btnBack.setOnClickListener(v -> finish());
    }

    private void displayImage(String content, ImageView img) {
        if (content != null && !content.isEmpty()) {
            img.setVisibility(View.VISIBLE);
            if (content.startsWith("http")) Glide.with(this).load(content).into(img);
            else Glide.with(this).load(new File(getFilesDir(), content)).into(img);
        } else {
            img.setVisibility(View.GONE);
        }
    }

    private void displayExtraItems(String extraItemsString) {
        String cleaned = extraItemsString.replace("[", "").replace("]", "");
        String[] items = cleaned.split(", ");
        for (String imagePath : items) {
            ImageView iv = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(150, 150);
            params.setMargins(10, 0, 10, 0);
            iv.setLayoutParams(params);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            if (imagePath.startsWith("http")) Glide.with(this).load(imagePath).into(iv);
            else Glide.with(this).load(new File(getFilesDir(), imagePath)).into(iv);
            addedItemsContainer.addView(iv);
        }
    }

    private void setupRemoveListener(ImageView img, String itemType) {
        img.setOnLongClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Remove Item")
                    .setMessage("Remove this " + itemType + "?")
                    .setPositiveButton("Remove", (dialog, which) -> {
                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        FirebaseDatabase.getInstance(dbUrl).getReference("Users").child(userId)
                                .child("myLooks").child(lookId).child(itemType).setValue("");
                        img.setVisibility(View.GONE);
                    }).setNegativeButton("Cancel", null).show();
            return true;
        });
    }

    private void confirmDeleteLook() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Look")
                .setMessage("Are you sure you want to delete this look?")
                .setPositiveButton("Delete", (d, w) -> {
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    FirebaseDatabase.getInstance(dbUrl).getReference("Users").child(userId)
                            .child("myLooks").child(lookId).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Look Deleted", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                }).setNegativeButton("Cancel", null).show();
    }
}
