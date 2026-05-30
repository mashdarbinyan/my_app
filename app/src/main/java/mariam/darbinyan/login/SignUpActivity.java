package mariam.darbinyan.login;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();


        android.widget.EditText emailField = findViewById(R.id.signUpEmail);
        android.widget.EditText passwordField = findViewById(R.id.signUpPassword);
        android.widget.Button regBtn = findViewById(R.id.registerButton);


        regBtn.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (email.isEmpty() || password.length() < 6) {
                android.widget.Toast.makeText(this, "Email required / Pass 6+ chars", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {

                                user.sendEmailVerification()
                                        .addOnCompleteListener(verifyTask -> {
                                            if (verifyTask.isSuccessful()) {
                                                Toast.makeText(this, "Verification email sent! Please check your inbox.", Toast.LENGTH_LONG).show();

                                                mAuth.signOut();
                                                finish();
                                            } else {
                                                Toast.makeText(this, "Failed to send email: " + verifyTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
        android.widget.TextView loginLink = findViewById(R.id.loginTextLink);
        loginLink.setOnClickListener(v -> {
            finish();
        });
    }
}
