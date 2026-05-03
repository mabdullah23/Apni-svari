package com.example.apni_svari;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class ConfirmUserName extends AppCompatActivity {

    private TextInputEditText usernameInput;
    private Button confirmBtn;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_user_name);

        usernameInput = findViewById(R.id.usernameInput);
        confirmBtn = findViewById(R.id.confirmButton);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        confirmBtn.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            if (username.isEmpty()) {
                Toast.makeText(this, "Enter a username", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("usernames").document(username).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().exists()) {
                                Toast.makeText(ConfirmUserName.this, "Username already taken", Toast.LENGTH_SHORT).show();
                            } else {
                                saveUsername(username);
                            }
                        } else {
                            Toast.makeText(ConfirmUserName.this, "Error checking username: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    private void saveUsername(String username) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        Map<String, Object> usernameData = new HashMap<>();
        usernameData.put("uid", user.getUid());
        usernameData.put("email", user.getEmail());
        if (user.getPhoneNumber() != null) usernameData.put("phone", user.getPhoneNumber());
        usernameData.put("timestamp", com.google.firebase.Timestamp.now());

        db.collection("usernames").document(username)
                .set(usernameData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(username)
                                .build();
                        user.updateProfile(profileUpdates).addOnCompleteListener(t -> {
                            if (t.isSuccessful()) {
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("username", username);
                                userData.put("name", username);
                                userData.put("email", user.getEmail());
                                userData.put("uid", user.getUid());
                                if (user.getPhoneNumber() != null) userData.put("phone", user.getPhoneNumber());
                                
                                db.collection("users").document(user.getUid())
                                        .set(userData, SetOptions.merge())
                                        .addOnCompleteListener(t2 -> {
                                            if (t2.isSuccessful()) {
                                                startActivity(new Intent(ConfirmUserName.this, Ask_user.class));
                                                finish();
                                            } else {
                                                Toast.makeText(ConfirmUserName.this, "Error saving profile", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(ConfirmUserName.this, "Error updating profile", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(ConfirmUserName.this, "Error saving username", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
