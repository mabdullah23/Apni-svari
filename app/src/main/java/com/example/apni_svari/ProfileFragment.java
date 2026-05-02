package com.example.apni_svari;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private static final String PREFS_NAME = "profile_image_prefs";
    private static final String IMAGE_PREFIX = "profile_image_";

    private ShapeableImageView profileImageView;
    private TextView usernameText;
    private TextView emailText;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        profileImageView = view.findViewById(R.id.profileImageView);
        usernameText = view.findViewById(R.id.profileUsernameText);
        emailText = view.findViewById(R.id.profileEmailText);
        Button changeImageButton = view.findViewById(R.id.changeImageButton);
        Button removeImageButton = view.findViewById(R.id.removeImageButton);
        Button logoutButton = view.findViewById(R.id.logoutButton);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        handlePickedImage(uri);
                    }
                }
        );

        loadUserInfo();
        loadProfileImage();

        changeImageButton.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        removeImageButton.setOnClickListener(v -> {
            if (currentUser == null) return;
            getPrefs().edit().remove(getImageKey()).apply();
            profileImageView.setImageResource(R.drawable.profilepic);
            Toast.makeText(getContext(), "Profile picture removed", Toast.LENGTH_SHORT).show();
        });

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getContext(), MainRegPage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) getActivity().finish();
        });

        return view;
    }

    private void loadUserInfo() {
        if (currentUser == null) {
            usernameText.setText(getString(R.string.profile_username_value, getString(R.string.profile_empty_value)));
            emailText.setText(getString(R.string.profile_email_value, getString(R.string.profile_empty_value)));
            return;
        }

        String username = currentUser.getDisplayName();
        String email = currentUser.getEmail();

        usernameText.setText(getString(R.string.profile_username_value,
                username != null && !username.isEmpty() ? username : getString(R.string.profile_empty_value)));
        emailText.setText(getString(R.string.profile_email_value,
                email != null && !email.isEmpty() ? email : getString(R.string.profile_empty_value)));
    }

    private void handlePickedImage(Uri uri) {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(requireContext().getContentResolver().openInputStream(uri));
            if (bitmap == null) {
                Toast.makeText(getContext(), "Unable to load image", Toast.LENGTH_SHORT).show();
                return;
            }

            profileImageView.setImageBitmap(bitmap);

            String encoded = bitmapToBase64(bitmap);
            getPrefs().edit().putString(getImageKey(), encoded).apply();
            Toast.makeText(getContext(), "Profile picture updated", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Image update failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProfileImage() {
        if (currentUser == null) {
            profileImageView.setImageResource(R.drawable.profilepic);
            return;
        }

        String encoded = getPrefs().getString(getImageKey(), null);
        if (encoded == null || encoded.isEmpty()) {
            profileImageView.setImageResource(R.drawable.profilepic);
            return;
        }

        Bitmap bitmap = base64ToBitmap(encoded);
        if (bitmap != null) {
            profileImageView.setImageBitmap(bitmap);
        } else {
            profileImageView.setImageResource(R.drawable.profilepic);
        }
    }

    private SharedPreferences getPrefs() {
        return requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private String getImageKey() {
        String uid = currentUser != null ? currentUser.getUid() : "guest";
        return IMAGE_PREFIX + uid;
    }

    private String bitmapToBase64(Bitmap bitmap) {
        java.io.ByteArrayOutputStream byteArrayOutputStream = new java.io.ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream);
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
    }

    private Bitmap base64ToBitmap(String encoded) {
        try {
            byte[] decodedBytes = Base64.decode(encoded, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            return null;
        }
    }
}
