package com.example.Buddybot;
import android.app.ProgressDialog;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.Buddybot.databinding.ActivityRegisterBinding;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private String name = "";
    private String email = "";
    private String pass = "";
    private String cpass = "";
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
        button=findViewById(R.id.register);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               validatedata();
            }
        });

    }



    public void registerclicked(View view) {
        // 1) Input data
        // 2) Validate Data
        // 3) Create Account
        // 4) Save user info -Firebase Realtime Database
        validatedata();
    }

    private void validatedata() {
        // 1) Input data
        name = binding.nameEt.getText().toString().trim();
        email = binding.RegisterEmailEt.getText().toString().trim();
        pass = binding.RegisterPassEt.getText().toString().trim();
        cpass = binding.RegisterConfirmPassEt.getText().toString().trim();
        Log.e("sougata", pass);
        if (name.isEmpty()) {
            Toast.makeText(this, "Enter your name...", Toast.LENGTH_SHORT).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid Email...", Toast.LENGTH_SHORT).show();
        } else if (pass.isEmpty()) {
            Toast.makeText(this, "Enter password...", Toast.LENGTH_SHORT).show();
        } else if (cpass.isEmpty()) {
            Toast.makeText(this, "Confirm your password...", Toast.LENGTH_SHORT).show();
        } else if (!pass.equals(cpass)) {
            Toast.makeText(this, "Password doesn't match...", Toast.LENGTH_SHORT).show();
        } else {
            createUserAccount();
        }
    }

    private void createUserAccount() {
        // 3) Create Account
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(authResult -> {
                    //user created , now add user info in db
                    updateUserInfo();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed Creating account due to " + e.getMessage() + "...", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUserInfo() {
        // 4) Save user info -Firebase Realtime Database
        progressDialog.setMessage("Saving user Info...");

        //timestamp
        long timestamp = System.currentTimeMillis();

        //get current user uid , since user is registered we can get it
        String uid = firebaseAuth.getUid();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("email", email);
        hashMap.put("name", name);
        hashMap.put("profileImage", ""); // will do in profile edit
        hashMap.put("userType", "user"); // possible values are user and admin , will change value to admin manually in firebase db
        hashMap.put("timestamp", timestamp);

        FirebaseDatabase.getInstance().getReference("Users")
                .child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(aVoid -> {
                    //user info saved , open user dashboard
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, "account created...", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                })
                .addOnFailureListener(e -> {
                    //failed adding data to db
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, "Failed saving data to DB due to " + e.getMessage() + "...", Toast.LENGTH_SHORT).show();
                });
    }


}
