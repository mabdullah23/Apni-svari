package com.example.apni_svari;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginFragment extends Fragment {

    private FirebaseAuth auth;
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
                        Toast.makeText(getContext(), "Google sign in failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        auth = FirebaseAuth.getInstance();

        TextInputEditText email = view.findViewById(R.id.loginEmail);
        TextInputEditText password = view.findViewById(R.id.loginPassword);
        Button loginBtn = view.findViewById(R.id.loginButton);
        TextView dontHave = view.findViewById(R.id.dontHaveAccount);
        androidx.cardview.widget.CardView googleSignInCard = view.findViewById(R.id.googleSignInCard);

        loginBtn.setOnClickListener(v -> {
            String e = email.getText().toString().trim();
            String p = password.getText().toString().trim();
            if (e.isEmpty() || p.isEmpty()) {
                Toast.makeText(getContext(), "Enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
                email.setError("Enter a valid email");
                return;
            }
            auth.signInWithEmailAndPassword(e, p)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(getContext(), Ask_user.class));
                            if (getActivity() != null) getActivity().finish();
                        } else {
                            Toast.makeText(getContext(), "Login failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        dontHave.setOnClickListener(v -> {
            if (getActivity() instanceof MainRegPage) {
                ((MainRegPage) getActivity()).loadFragment(new SigninFragment());
            }
        });

        googleSignInCard.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        return view;
    }

    private void firebaseAuthWithGoogle(String idToken) {
        if (idToken == null) {
            android.util.Log.e("GoogleSignIn", "ID Token is null");
            Toast.makeText(getContext(), "Google token is null. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
            auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        android.util.Log.d("GoogleSignIn", "Firebase auth successful");
                        Toast.makeText(getContext(), "Authenticating...", Toast.LENGTH_SHORT).show();
                        
                        // Check if user has a displayName (simple way to check if first time)
                        if (auth.getCurrentUser() != null && auth.getCurrentUser().getDisplayName() != null && !auth.getCurrentUser().getDisplayName().isEmpty()) {
                            // Has displayName = returning user
                            android.util.Log.d("GoogleSignIn", "Returning user, navigating to Ask_user");
                            Intent intent = new Intent(getContext(), Ask_user.class);
                            startActivity(intent);
                            if (getActivity() != null) {
                                getActivity().finish();
                            }
                        } else {
                            // No displayName = first time user
                            android.util.Log.d("GoogleSignIn", "New user, navigating to ConfirmUserName");
                            Intent intent = new Intent(getContext(), ConfirmUserName.class);
                            startActivity(intent);
                            if (getActivity() != null) {
                                getActivity().finish();
                            }
                        }
                    } else {
                        Exception ex = task.getException();
                        String msg = ex != null ? ex.getMessage() : "Unknown error";
                        android.util.Log.e("GoogleSignIn", "Firebase auth failed: " + msg, ex);
                        Toast.makeText(getContext(), "Auth failed: " + msg, Toast.LENGTH_LONG).show();
                    }
                });
        } catch (Exception e) {
            android.util.Log.e("GoogleSignIn", "Exception in firebaseAuthWithGoogle: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}

