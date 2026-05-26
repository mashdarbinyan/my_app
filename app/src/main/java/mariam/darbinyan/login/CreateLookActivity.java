package mariam.darbinyan.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.io.File;
import java.util.HashMap;

public class CreateLookActivity extends AppCompatActivity {

    private EditText etLookName;
    private ImageView imgSlotDress, imgSlotJacket, imgSlotPants, imgSlotShoes;
    private Button btnSaveLook;

    // These now store URLs or file names
    private String selectedDress = "";
    private String selectedJacket = "";
    private String selectedPants = "";
    private String selectedShoes = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_look);

        etLookName = findViewById(R.id.etLookName);
        imgSlotDress = findViewById(R.id.imgSlotDress);
        imgSlotJacket = findViewById(R.id.imgSlotJacket);
        imgSlotPants = findViewById(R.id.imgSlotPants);
        imgSlotShoes = findViewById(R.id.imgSlotShoes);
        btnSaveLook = findViewById(R.id.btnSaveLook);

        imgSlotDress.setOnClickListener(v -> openSelection(DressActivity.class, 101));
        imgSlotJacket.setOnClickListener(v -> openSelection(JacketActivity.class, 104));
        imgSlotPants.setOnClickListener(v -> openSelection(PantsActivity.class, 102));
        imgSlotShoes.setOnClickListener(v -> openSelection(ShoesActivity.class, 103));

        btnSaveLook.setOnClickListener(v -> saveLookToFirebase());
    }

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
            ImageView targetSlot = null;

            if (requestCode == 101) { targetSlot = imgSlotDress; selectedDress = pickedImage; }
            else if (requestCode == 102) { targetSlot = imgSlotPants; selectedPants = pickedImage; }
            else if (requestCode == 103) { targetSlot = imgSlotShoes; selectedShoes = pickedImage; }
            else if (requestCode == 104) { targetSlot = imgSlotJacket; selectedJacket = pickedImage; }

            if (targetSlot != null) {
                // Use Glide to load based on whether it's a URL or a file name
                if (pickedImage.startsWith("http")) {
                    Glide.with(this).load(pickedImage).into(targetSlot);
                } else {
                    File file = new File(getFilesDir(), pickedImage);
                    Glide.with(this).load(file).into(targetSlot);
                }
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
        lookData.put("dress", selectedDress);
        lookData.put("jacket", selectedJacket);
        lookData.put("pants", selectedPants);
        lookData.put("shoes", selectedShoes);

        lookRef.child(lookId).setValue(lookData).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Outfit Saved! ✨", Toast.LENGTH_LONG).show();
            finish();
        });
    }
}