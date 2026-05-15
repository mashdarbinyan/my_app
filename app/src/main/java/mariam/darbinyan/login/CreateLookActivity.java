package mariam.darbinyan.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;

public class CreateLookActivity extends AppCompatActivity {

    private EditText etLookName;
    // Updated to match your new 4-slot layout IDs
    private ImageView imgSlotDress, imgSlotJacket, imgSlotPants, imgSlotShoes;
    private Button btnSaveLook;

    // Updated string names to match your request
    private String selectedDressB64 = "";
    private String selectedJacketB64 = "";
    private String selectedPantsB64 = "";
    private String selectedShoesB64 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_look);

        etLookName = findViewById(R.id.etLookName);

        // Initialize the 4 slots
        imgSlotDress = findViewById(R.id.imgSlotDress);
        imgSlotJacket = findViewById(R.id.imgSlotJacket);
        imgSlotPants = findViewById(R.id.imgSlotPants);
        imgSlotShoes = findViewById(R.id.imgSlotShoes);

        btnSaveLook = findViewById(R.id.btnSaveLook);

        // Click listeners using unique RequestCodes
        imgSlotDress.setOnClickListener(v -> openSelection(DressActivity.class, 101));
        imgSlotJacket.setOnClickListener(v -> openSelection(JacketActivity.class, 104));
        imgSlotPants.setOnClickListener(v -> openSelection(PantsActivity.class, 102));
        imgSlotShoes.setOnClickListener(v -> openSelection(ShoesActivity.class, 103));

        btnSaveLook.setOnClickListener(v -> saveLookToFirebase());
    }

    // Pass the actual Activity class to make it cleaner
    private void openSelection(Class<?> activityClass, int requestCode) {
        Intent intent = new Intent(this, activityClass);
        intent.putExtra("SELECT_MODE", true);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            String pickedImage = data.getStringExtra("PICKED_IMAGE");

            // Convert Base64 to Bitmap to show in the UI
            byte[] decodedString = android.util.Base64.decode(pickedImage, android.util.Base64.DEFAULT);
            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            if (requestCode == 101) {
                selectedDressB64 = pickedImage;
                imgSlotDress.setImageBitmap(bitmap);
            } else if (requestCode == 102) {
                selectedPantsB64 = pickedImage;
                imgSlotPants.setImageBitmap(bitmap);
            } else if (requestCode == 103) {
                selectedShoesB64 = pickedImage;
                imgSlotShoes.setImageBitmap(bitmap);
            } else if (requestCode == 104) {
                selectedJacketB64 = pickedImage;
                imgSlotJacket.setImageBitmap(bitmap);
            }
        }
    }

    private void saveLookToFirebase() {
        String name = etLookName.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please give your look a name!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference lookRef = FirebaseDatabase.getInstance("https://mariam-sproject-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Users").child(userId).child("myLooks");

        String lookId = lookRef.push().getKey();

        HashMap<String, String> lookData = new HashMap<>();
        lookData.put("lookName", name);
        lookData.put("dress", selectedDressB64);
        lookData.put("jacket", selectedJacketB64);
        lookData.put("pants", selectedPantsB64);
        lookData.put("shoes", selectedShoesB64);

        lookRef.child(lookId).setValue(lookData).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Outfit Saved! ✨", Toast.LENGTH_LONG).show();
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}