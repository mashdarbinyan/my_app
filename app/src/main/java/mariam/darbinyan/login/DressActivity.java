package mariam.darbinyan.login;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DressActivity extends AppCompatActivity {

    // This launcher handles the result of picking an image
    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    // This is where you get the photo URI!
                    // For now, let's just log it or show a toast
                    Toast.makeText(this, "Photo selected!", Toast.LENGTH_SHORT).show();

                    // LATER: You will save this 'uri' to Firebase
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dress);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // 2. Initialize the Add Button
        com.google.android.material.floatingactionbutton.FloatingActionButton fab =
                findViewById(R.id.fab_add_dress);

        findViewById(R.id.fab_add_dress).setOnClickListener(v -> {
            // This line opens the phone's gallery
            mGetContent.launch("image/*");
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Go back to HomeActivity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}