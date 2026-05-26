package mariam.darbinyan.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CreateLookActivity extends AppCompatActivity {

    private EditText etLookName;
    private ImageView imgSlotDress, imgSlotJacket, imgSlotPants, imgSlotShoes;
    private Button btnSaveLook;
    private LinearLayout dynamicCategoryContainer, addedItemsContainer;

    private String selectedDress = "", selectedJacket = "", selectedPants = "", selectedShoes = "";
    private List<String> extraItemsList = new ArrayList<>();

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
        dynamicCategoryContainer = findViewById(R.id.dynamicCategoryContainer);
        addedItemsContainer = findViewById(R.id.addedItemsContainer); // Ensure this ID is in your XML

        imgSlotDress.setOnClickListener(v -> openSelection(DressActivity.class, 101));
        imgSlotJacket.setOnClickListener(v -> openSelection(JacketActivity.class, 104));
        imgSlotPants.setOnClickListener(v -> openSelection(PantsActivity.class, 102));
        imgSlotShoes.setOnClickListener(v -> openSelection(ShoesActivity.class, 103));

        loadCustomCategories();
        btnSaveLook.setOnClickListener(v -> saveLookToFirebase());
    }

    private void loadCustomCategories() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance("https://mariam-sproject-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Users").child(userId).child("custom_categories_list");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    String catName = data.getValue(String.class);
                    String catKey = data.getKey();
                    Button btn = new Button(CreateLookActivity.this);
                    btn.setText("Add from " + catName);
                    btn.setOnClickListener(v -> {
                        Intent intent = new Intent(CreateLookActivity.this, CustomSelectionActivity.class);
                        intent.putExtra("CATEGORY_KEY", catKey);
                        startActivityForResult(intent, 200);
                    });
                    dynamicCategoryContainer.addView(btn);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
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
            if (requestCode == 200) {
                // Add to extra items list and display in the new container
                extraItemsList.add(pickedImage);
                ImageView newItem = new ImageView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(120, 120);
                params.setMargins(10, 0, 10, 0);
                newItem.setLayoutParams(params);
                newItem.setScaleType(ImageView.ScaleType.CENTER_CROP);

                if (pickedImage.startsWith("http")) Glide.with(this).load(pickedImage).into(newItem);
                else Glide.with(this).load(new File(getFilesDir(), pickedImage)).into(newItem);

                addedItemsContainer.addView(newItem);
            } else {
                updateSlot(requestCode, pickedImage);
            }
        }
    }

    private void updateSlot(int requestCode, String pickedImage) {
        ImageView targetSlot = null;
        if (requestCode == 101) { targetSlot = imgSlotDress; selectedDress = pickedImage; }
        else if (requestCode == 102) { targetSlot = imgSlotPants; selectedPants = pickedImage; }
        else if (requestCode == 103) { targetSlot = imgSlotShoes; selectedShoes = pickedImage; }
        else if (requestCode == 104) { targetSlot = imgSlotJacket; selectedJacket = pickedImage; }

        if (targetSlot != null) {
            if (pickedImage.startsWith("http")) Glide.with(this).load(pickedImage).into(targetSlot);
            else Glide.with(this).load(new File(getFilesDir(), pickedImage)).into(targetSlot);
        }
    }

    private void saveLookToFirebase() {
        String name = etLookName.getText().toString().trim();
        if (name.isEmpty()) { Toast.makeText(this, "Name your look!", Toast.LENGTH_SHORT).show(); return; }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference lookRef = FirebaseDatabase.getInstance("https://mariam-sproject-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Users").child(userId).child("myLooks");

        String lookId = lookRef.push().getKey();
        HashMap<String, Object> lookData = new HashMap<>();
        lookData.put("lookName", name);
        lookData.put("dress", selectedDress);
        lookData.put("jacket", selectedJacket);
        lookData.put("pants", selectedPants);
        lookData.put("shoes", selectedShoes);
        lookData.put("extraItems", extraItemsList.toString());

        lookRef.child(lookId).setValue(lookData).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Outfit Saved! ✨", Toast.LENGTH_LONG).show();
            finish();
        });
    }
}