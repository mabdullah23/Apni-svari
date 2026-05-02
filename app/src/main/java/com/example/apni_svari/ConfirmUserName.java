package com.example.apni_svari;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

public class ConfirmUserName extends AppCompatActivity {

    private EditText usernameInput;
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

            // Check if username already exists in Firestore
            db.collection("usernames").document(username).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().exists()) {
                                // Username already exists
                                Toast.makeText(ConfirmUserName.this, "Username already taken", Toast.LENGTH_SHORT).show();
                            } else {
                                // Username is available, save it
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

        // Save username in Firestore
        db.collection("usernames").document(username)
                .set(new java.util.HashMap<String, Object>() {{
                    put("uid", user.getUid());
                    put("email", user.getEmail());
                    put("timestamp", com.google.firebase.Timestamp.now());
                }})
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Update user profile with display name
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(username)
                                .build();
                        user.updateProfile(profileUpdates).addOnCompleteListener(t -> {
                            if (t.isSuccessful()) {
                                // Also save user profile in Firestore
                                db.collection("users").document(user.getUid())
                                        .set(new java.util.HashMap<String, Object>() {{
                                            put("username", username);
                                            put("email", user.getEmail());
                                            put("uid", user.getUid());
                                            put("createdAt", com.google.firebase.Timestamp.now());
                                        }})
                                        .addOnCompleteListener(t2 -> {
                                            if (t2.isSuccessful()) {
                                                // Navigate to MainUserPage
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

