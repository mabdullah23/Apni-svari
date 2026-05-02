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

import java.util.HashMap;
import java.util.Locale;

public class SigninFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_signin, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        EditText username = view.findViewById(R.id.signinUsername);
        EditText email = view.findViewById(R.id.signinEmail);
        EditText password = view.findViewById(R.id.signinPassword);
        EditText phone = view.findViewById(R.id.signinPhone); // NEW FIELD
        Button signupBtn = view.findViewById(R.id.signinButton);

        signupBtn.setOnClickListener(v -> {

            String u = username.getText().toString().trim();
            String e = email.getText().toString().trim().toLowerCase(Locale.ROOT);
            String p = password.getText().toString().trim();
            String ph = phone.getText().toString().trim();

            // VALIDATION
            if (u.isEmpty() || e.isEmpty() || p.isEmpty() || ph.isEmpty()) {
                Toast.makeText(getContext(), "Enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
                email.setError("Enter a valid email");
                return;
            }

            if (p.length() < 6) {
                password.setError("Password must be at least 6 characters");
                return;
            }

            // Simple phone validation (Pakistan-style or general)
            if (!ph.matches("^[0-9]{10,13}$")) {
                phone.setError("Enter a valid phone number (10–13 digits)");
                return;
            }

            // CREATE USER
            auth.createUserWithEmailAndPassword(e, p)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            FirebaseUser user = auth.getCurrentUser();
                            if (user == null) return;

                            // 1. Set display name
                            user.updateProfile(
                                    new UserProfileChangeRequest.Builder()
                                            .setDisplayName(u)
                                            .build()
                            );

                            // 2. Navigate immediately
                            startActivity(new Intent(getContext(), Ask_user.class));
                            if (getActivity() != null) {
                                getActivity().finish();
                            }

                            Toast.makeText(getContext(),
                                    "Account created successfully!",
                                    Toast.LENGTH_SHORT).show();

                            // 3. Save Firestore in background
                            HashMap<String, Object> userData = new HashMap<>();
                            userData.put("username", u);
                            userData.put("email", user.getEmail());
                            userData.put("phone", ph); // ✅ ADDED
                            userData.put("uid", user.getUid());
                            userData.put("createdAt", com.google.firebase.Timestamp.now());

                            db.collection("users")
                                    .document(user.getUid())
                                    .set(userData);

                            // Optional usernames collection
                            HashMap<String, Object> usernameData = new HashMap<>();
                            usernameData.put("uid", user.getUid());
                            usernameData.put("email", user.getEmail());
                            usernameData.put("phone", ph); // ✅ ADDED
                            usernameData.put("timestamp", com.google.firebase.Timestamp.now());

                            db.collection("usernames")
                                    .document(u)
                                    .set(usernameData);

                        } else {

                            Exception ex = task.getException();

                            Toast.makeText(getContext(),
                                    "Signup failed: " +
                                            (ex != null ? ex.getMessage() : "Unknown error"),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });

        return view;
    }
}