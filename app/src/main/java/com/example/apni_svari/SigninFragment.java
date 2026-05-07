package com.example.apni_svari;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;

public class SigninFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data == null) return;
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account != null) {
                            String idToken = account.getIdToken();
                            firebaseAuthWithGoogle(idToken);
                        }
                    } catch (ApiException e) {
                        Toast.makeText(getContext(), "Google sign up failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        );

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_signin, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        TextInputEditText username = view.findViewById(R.id.signinUsername);
        TextInputEditText email = view.findViewById(R.id.signinEmail);
        TextInputEditText password = view.findViewById(R.id.signinPassword);
        TextInputEditText phone = view.findViewById(R.id.signinPhone);
        Button signupBtn = view.findViewById(R.id.signinButton);
        ImageView googleSignUp = view.findViewById(R.id.googleSignUpBtn);

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
                            userData.put("phone", ph);
                            userData.put("uid", user.getUid());
                            userData.put("createdAt", com.google.firebase.Timestamp.now());

                            db.collection("users")
                                    .document(user.getUid())
                                    .set(userData);

                            // Optional usernames collection
                            HashMap<String, Object> usernameData = new HashMap<>();
                            usernameData.put("uid", user.getUid());
                            usernameData.put("email", user.getEmail());
                            usernameData.put("phone", ph);
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

        googleSignUp.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        return view;
    }

    private void firebaseAuthWithGoogle(String idToken) {
        if (idToken == null) {
            android.util.Log.e("GoogleSignUp", "ID Token is null");
            Toast.makeText(getContext(), "Google token is null. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
            auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        android.util.Log.d("GoogleSignUp", "Firebase auth successful");
                        Toast.makeText(getContext(), "Signing up...", Toast.LENGTH_SHORT).show();
                        
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // Save user to Firestore with error handling
                            HashMap<String, Object> userData = new HashMap<>();
                            userData.put("username", user.getDisplayName() != null ? user.getDisplayName() : user.getEmail());
                            userData.put("email", user.getEmail());
                            userData.put("phone", "");
                            userData.put("uid", user.getUid());
                            userData.put("createdAt", com.google.firebase.Timestamp.now());

                            db.collection("users")
                                    .document(user.getUid())
                                    .set(userData)
                                    .addOnSuccessListener(unused -> {
                                        android.util.Log.d("GoogleSignUp", "User saved to Firestore");
                                        Toast.makeText(getContext(), "Account created! Redirecting...", Toast.LENGTH_SHORT).show();
                                        
                                        Intent intent = new Intent(getContext(), Ask_user.class);
                                        startActivity(intent);
                                        if (getActivity() != null) {
                                            getActivity().finish();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        android.util.Log.e("GoogleSignUp", "Firestore save failed: " + e.getMessage(), e);
                                        Toast.makeText(getContext(), "Profile save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        } else {
                            android.util.Log.e("GoogleSignUp", "Current user is null after authentication");
                            Toast.makeText(getContext(), "User authentication failed", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Exception ex = task.getException();
                        String msg = ex != null ? ex.getMessage() : "Unknown error";
                        android.util.Log.e("GoogleSignUp", "Firebase auth failed: " + msg, ex);
                        Toast.makeText(getContext(), "Sign up failed: " + msg, Toast.LENGTH_LONG).show();
                    }
                });
        } catch (Exception e) {
            android.util.Log.e("GoogleSignUp", "Exception in firebaseAuthWithGoogle: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
