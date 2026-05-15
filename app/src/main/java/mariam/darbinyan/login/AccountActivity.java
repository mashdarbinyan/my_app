package mariam.darbinyan.login;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;

public class AccountActivity extends AppCompatActivity {

    private TextView userEmail, wardrobeCount, styleLevel;
    private EditText editNickname, editDob;
    private Button btnSave;
    private FirebaseAuth auth;
    private DatabaseReference dbRef;
    private final String dbUrl = "https://mariam-sproject-default-rtdb.europe-west1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        // Initialize Views
        userEmail = findViewById(R.id.txt_user_email);
        wardrobeCount = findViewById(R.id.stat_total_items);
        styleLevel = findViewById(R.id.stat_style_level);
        editNickname = findViewById(R.id.edit_nickname);
        editDob = findViewById(R.id.edit_dob);
        btnSave = findViewById(R.id.btn_save_profile);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            userEmail.setText(user.getEmail());
            String userId = user.getUid();

            // 1. Load Wardrobe Numbers
            loadWardrobeStats(userId);

            // 2. Load Profile Details (Nickname/DOB)
            loadUserData(userId);

            // 3. Set Save Button Listener
            btnSave.setOnClickListener(v -> saveUserProfile(userId));
        }
    }

    private void loadUserData(String userId) {
        DatabaseReference profileRef = FirebaseDatabase.getInstance(dbUrl)
                .getReference("Users").child(userId).child("ProfileDetails");

        profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String nick = snapshot.child("nickname").getValue(String.class);
                    String dob = snapshot.child("dob").getValue(String.class);
                    if (nick != null) editNickname.setText(nick);
                    if (dob != null) editDob.setText(dob);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void saveUserProfile(String userId) {
        String nick = editNickname.getText().toString().trim();
        String dob = editDob.getText().toString().trim();

        DatabaseReference profileRef = FirebaseDatabase.getInstance(dbUrl)
                .getReference("Users").child(userId).child("ProfileDetails");

        HashMap<String, Object> profileData = new HashMap<>();
        profileData.put("nickname", nick);
        profileData.put("dob", dob);

        profileRef.setValue(profileData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(AccountActivity.this, "Profile Saved!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(AccountActivity.this, "Update Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadWardrobeStats(String userId) {
        dbRef = FirebaseDatabase.getInstance(dbUrl).getReference("Users").child(userId);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                long totalItems = 0;
                // Loop through categories to count total clothes
                for (DataSnapshot category : snapshot.getChildren()) {
                    // Skip the ProfileDetails node so it doesn't count as clothing
                    if (!category.getKey().equals("ProfileDetails")) {
                        totalItems += category.getChildrenCount();
                    }
                }

                wardrobeCount.setText("Total Items: " + totalItems);

                if (totalItems > 50) {
                    styleLevel.setText("Status: Fashion Icon");
                } else if (totalItems > 20) {
                    styleLevel.setText("Status: Trendsetter");
                } else {
                    styleLevel.setText("Status: Aspiring Stylist");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}