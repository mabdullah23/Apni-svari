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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

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

        return view;
    }

    private void showAddProductDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_product, null);
        dialogImagePreview = dialogView.findViewById(R.id.dialogProductImage);
        EditText nameInput = dialogView.findViewById(R.id.carNameInput);
        EditText modelInput = dialogView.findViewById(R.id.carModelInput);
        EditText priceInput = dialogView.findViewById(R.id.carPriceInput);
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

            saveProduct(name, model, price, selectedImageBase64);
            dialog.dismiss();
        }));

        dialog.show();
    }

    private void saveProduct(String name, String model, String price, String imageBase64) {
        if (currentUser == null) {
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("ownerUid", currentUser.getUid());
        data.put("carName", name);
        data.put("model", model);
        data.put("price", price);
        data.put("offeredPrice", "");
        data.put("imageBase64", imageBase64);
        data.put("createdAt", com.google.firebase.Timestamp.now());

        db.collection("seller_products")
                .add(data)
                .addOnSuccessListener(doc -> {
                    // Real-time listener will handle updating the UI
                    Toast.makeText(getContext(), R.string.seller_product_added, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadProducts() {
        if (currentUser == null) {
            return;
        }

        // Remove previous listener if it exists
        if (productsListener != null) {
            productsListener.remove();
        }

        // Set up real-time listener
        productsListener = db.collection("seller_products")
                .whereEqualTo("ownerUid", currentUser.getUid())
                .addSnapshotListener((querySnapshots, error) -> {
                    if (error != null) {
                        return;
                    }

                    if (querySnapshots != null) {
                        productList.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshots.getDocuments()) {
                            productList.add(new ZsellerProduct(
                                    doc.getId(),
                                    doc.getString("imageBase64"),
                                    doc.getString("carName"),
                                    doc.getString("model"),
                                    doc.getString("price"),
                                    doc.getString("offeredPrice")
                            ));
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
        // Remove listener to prevent memory leaks
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

        // Show confirmation dialog
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.seller_delete_product)
                .setMessage("Are you sure you want to delete this product?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("seller_products")
                            .document(product.getId())
                            .delete()
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
}

