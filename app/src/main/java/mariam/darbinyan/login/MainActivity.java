package mariam.darbinyan.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    EditText username;
    EditText password;
    Button loginButton;
    Button testModeButton;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        testModeButton = findViewById(R.id.btn_test_mode);

        mAuth = FirebaseAuth.getInstance();


        if (testModeButton != null) {
            testModeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "Developer Mode: Logging in as Test User...", Toast.LENGTH_SHORT).show();

                    mAuth.signInWithEmailAndPassword("innovationcampus26@gmail.com", "Samsung2026")
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {

                                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                    intent.putExtra("USER_NAME", "innovationcampus26@gmail.com");
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(MainActivity.this, "Test Account Error: Make sure innovationcampus26@gmail.com exists in Firebase!", Toast.LENGTH_LONG).show();
                                }
                            });
                }
            });
        }


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = username.getText().toString().trim();
                String pass = password.getText().toString().trim();

                if (email.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();


                                if (user != null && ("innovationcampus26@gmail.com".equalsIgnoreCase(email) || user.isEmailVerified())) {
                                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                    intent.putExtra("USER_NAME", email);
                                    startActivity(intent);
                                    finish();
                                } else {

                                    Toast.makeText(MainActivity.this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                                    mAuth.signOut();
                                }
                            } else {
                                Toast.makeText(MainActivity.this, "Authentication Failed: " +
                                        task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        TextView signupLink = findViewById(R.id.signupText);
        signupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }
}