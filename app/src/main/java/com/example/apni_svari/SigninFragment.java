package com.example.apni_svari;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

public class SigninFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signin, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        EditText username = view.findViewById(R.id.signinUsername);
        EditText email = view.findViewById(R.id.signinEmail);
        EditText password = view.findViewById(R.id.signinPassword);
        Button signupBtn = view.findViewById(R.id.signinButton);

        signupBtn.setOnClickListener(v -> {
            String u = username.getText().toString().trim();
            String e = email.getText().toString().trim();
            String p = password.getText().toString().trim();
            if (u.isEmpty() || e.isEmpty() || p.isEmpty()) {
                Toast.makeText(getContext(), "Enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
                email.setError("Enter a valid email");
                return;
            }
            if (p.length() < 6) {
                password.setError("Password should be at least 6 characters");
                return;
            }

            auth.createUserWithEmailAndPassword(e, p)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(u)
                                        .build();
                                user.updateProfile(profileUpdates).addOnCompleteListener(t -> {
                                    if (t.isSuccessful()) {
                                        // Save user profile to Firestore
                                        db.collection("users").document(user.getUid())
                                                .set(new java.util.HashMap<String, Object>() {{
                                                    put("username", u);
                                                    put("email", user.getEmail());
                                                    put("uid", user.getUid());
                                                    put("createdAt", com.google.firebase.Timestamp.now());
                                                }})
                                                .addOnCompleteListener(t2 -> {
                                                    if (t2.isSuccessful()) {
                                                        // Also save username to usernames collection for duplicate checking
                                                        db.collection("usernames").document(u)
                                                                .set(new java.util.HashMap<String, Object>() {{
                                                                    put("uid", user.getUid());
                                                                    put("email", user.getEmail());
                                                                    put("timestamp", com.google.firebase.Timestamp.now());
                                                                }})
                                                                .addOnCompleteListener(t3 -> {
                                                                    // Navigate to MainUserPage
                                                                    startActivity(new Intent(getContext(), MainUserPage.class));
                                                                    if (getActivity() != null) getActivity().finish();
                                                                });
                                                    } else {
                                                        Toast.makeText(getContext(), "Error saving profile", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(getContext(), "Error updating display name", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            Exception ex = task.getException();
                            String errorMsg = ex != null ? ex.getMessage() : "Unknown error";
                            Toast.makeText(getContext(), "Signup failed: " + errorMsg, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        return view;
    }
}

