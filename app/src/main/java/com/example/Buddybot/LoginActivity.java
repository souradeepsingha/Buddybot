package com.example.Buddybot;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.Buddybot.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        button=findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

    }

    public void not_have_account_clicked(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    public void LoginClicked(View view) {
        //steps
        // 1) Input Data
        // 2) validate data
        // 3) Login - Firebase Auth
        // 4) Check user type Firebase auth (user/admin)
        // 5) If user move to user dashboard,
        //      If admin move to admin dashboard
        validateData();
    }

    private void validateData() {
        String email = binding.emailEt.getText().toString().trim();
        String pass = binding.passwordEt.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Enter the email address...", Toast.LENGTH_SHORT).show();
        } else if (pass.isEmpty()) {
            Toast.makeText(this, "Enter password...", Toast.LENGTH_SHORT).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid Email...", Toast.LENGTH_SHORT).show();
        } else {
            loginUser(email, pass);
        }
    }

    private void loginUser(String email, String pass) {
        // 3) Login - Firebase Auth
        progressDialog.setMessage("Logging in...");
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(authResult -> checkUser())
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Login failed due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkUser() {
        // 4) Check user type Firebase auth (user/admin)
        // 5) If user move to user dashboard,
        //      If admin move to admin dashboard
        progressDialog.setMessage("Checking User...");

        final String uid = firebaseAuth.getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference("Users")
                .child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        //usertype can be user/admin
                        String userType = snapshot.child("userType").getValue(String.class);
                        if ("user".equals(userType)) {
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        } else if ("admin".equals(userType)) {
                            Toast.makeText(LoginActivity.this, "admin", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Handle database error if needed
                    }
                });
    }
}
