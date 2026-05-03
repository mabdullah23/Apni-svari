package com.example.apni_svari;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.WriteBatch;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZsellerHome extends Fragment implements ZsellerHomeAdapter.OnProductActionListener {

    private final List<ZsellerProduct> productList = new ArrayList<>();
    private ZsellerHomeAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private ListenerRegistration productsListener;

    private ActivityResultLauncher<String> imagePickerLauncher;
    private String selectedImageBase64;
    private ImageView dialogImagePreview;
    private String ownerPhoneNumber = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_zseller_home, container, false);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                handleImageSelection(uri);
            }
        });

        RecyclerView recyclerView = view.findViewById(R.id.sellerProductRecycler);
        ImageView addIcon = view.findViewById(R.id.addProductImage);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ZsellerHomeAdapter(productList, this);
        recyclerView.setAdapter(adapter);

        addIcon.setOnClickListener(v -> showAddProductDialog());

        loadProducts();
        fetchOwnerDetails();

        return view;
    }

    private void fetchOwnerDetails() {
        if (currentUser == null) return;
        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ownerPhoneNumber = documentSnapshot.getString("phone");
                        if (ownerPhoneNumber == null || ownerPhoneNumber.isEmpty()) {
                            ownerPhoneNumber = documentSnapshot.getString("phoneNumber");
                        }
                    }
                });
    }

    private void showAddProductDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_product, null);
        dialogImagePreview = dialogView.findViewById(R.id.dialogProductImage);
        TextInputEditText nameInput = dialogView.findViewById(R.id.carNameInput);
        TextInputEditText modelInput = dialogView.findViewById(R.id.carModelInput);
        TextInputEditText priceInput = dialogView.findViewById(R.id.carPriceInput);
        View selectImageButton = dialogView.findViewById(R.id.selectImageButton);

        selectedImageBase64 = null;
        dialogImagePreview.setImageResource(R.drawable.carimg);

        selectImageButton.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.seller_add_product_dialog_title)
                .setView(dialogView)
                .setPositiveButton(R.string.seller_save, null)
                .setNegativeButton(R.string.seller_cancel, (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String model = modelInput.getText().toString().trim();
            String price = priceInput.getText().toString().trim();

            if (name.isEmpty() || model.isEmpty() || price.isEmpty() || selectedImageBase64 == null) {
                Toast.makeText(getContext(), R.string.seller_fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                Double.parseDouble(price);
            } catch (NumberFormatException ex) {
                priceInput.setError("Enter a valid number");
                return;
            }

            saveProduct(name, model, price, selectedImageBase64);
            dialog.dismiss();
        }));

        dialog.show();
    }

    private void saveProduct(String name, String model, String price, String imageBase64) {
        if (currentUser == null) {
            return;
        }

        String documentId = db.collection("cars").document().getId();

        Map<String, Object> carData = new HashMap<>();
        carData.put("ownerId", currentUser.getUid());
        carData.put("ownerName", resolveOwnerName());
        carData.put("ownerPhone", resolveOwnerPhone());
        carData.put("name", name);
        carData.put("carName", name);
        carData.put("model", model);
        carData.put("price", Double.parseDouble(price));
        carData.put("imageBase64", imageBase64);
        carData.put("createdAt", com.google.firebase.Timestamp.now());
        carData.put("extraImages", new ArrayList<String>());

        db.collection("cars").document(documentId)
                .set(carData)
                .addOnSuccessListener(unused -> Toast.makeText(getContext(), R.string.seller_product_added, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadProducts() {
        if (currentUser == null) {
            return;
        }

        if (productsListener != null) {
            productsListener.remove();
        }

        productsListener = db.collection("cars")
                .whereEqualTo("ownerId", currentUser.getUid())
                .addSnapshotListener((querySnapshots, error) -> {
                    if (error != null) {
                        return;
                    }

                    if (querySnapshots != null) {
                        productList.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshots.getDocuments()) {
                            ZsellerProduct product = new ZsellerProduct(
                                    doc.getId(),
                                    doc.getString("imageBase64"),
                                    firstNonEmpty(doc.getString("carName"), doc.getString("name")),
                                    doc.getString("model"),
                                    String.valueOf(doc.get("price")),
                                    ""
                            );
                            List<String> extras = (List<String>) doc.get("extraImages");
                            if (extras != null) product.setExtraImages(extras);
                            productList.add(product);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void handleImageSelection(Uri uri) {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(requireContext().getContentResolver().openInputStream(uri));
            if (bitmap == null) {
                return;
            }
            selectedImageBase64 = bitmapToBase64(bitmap);
            if (dialogImagePreview != null) {
                dialogImagePreview.setImageBitmap(bitmap);
            }
        } catch (Exception ignored) {
        }
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (productsListener != null) {
            productsListener.remove();
        }
    }

    @Override
    public void onProductClick(ZsellerProduct product) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, ZsellerProductDetailFragment.newInstance(product))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDeleteClick(ZsellerProduct product, int position) {
        if (product.getId() == null || product.getId().isEmpty()) {
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.seller_delete_product)
                .setMessage("Are you sure you want to delete this product?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    WriteBatch batch = db.batch();
                    batch.delete(db.collection("cars").document(product.getId()));
                    batch.commit()
                            .addOnSuccessListener(unused -> {
                                if (position >= 0 && position < productList.size()) {
                                    productList.remove(position);
                                    adapter.notifyItemRemoved(position);
                                }
                                Toast.makeText(getContext(), R.string.seller_product_deleted, Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private String resolveOwnerName() {
        if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().trim().isEmpty()) {
            return currentUser.getDisplayName();
        }
        return currentUser.getEmail() != null ? currentUser.getEmail() : currentUser.getUid();
    }

    private String resolveOwnerPhone() {
        if (ownerPhoneNumber != null && !ownerPhoneNumber.trim().isEmpty()) return ownerPhoneNumber;
        return currentUser.getPhoneNumber() == null ? "" : currentUser.getPhoneNumber();
    }

    private String firstNonEmpty(String primary, String fallback) {
        return (primary != null && !primary.trim().isEmpty()) ? primary : fallback;
    }
}
