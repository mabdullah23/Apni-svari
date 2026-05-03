package com.example.apni_svari;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apni_svari.adapters.ExtraImagesAdapter;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ZsellerProductDetailFragment extends Fragment {

    private static final String ARG_ID = "arg_id";
    private static final String ARG_IMAGE = "arg_image";
    private static final String ARG_NAME = "arg_name";
    private static final String ARG_MODEL = "arg_model";
    private static final String ARG_PRICE = "arg_price";
    private static final String ARG_OFFERED = "arg_offered";
    private static final String ARG_EXTRA_IMAGES = "arg_extra_images";

    private String productId;
    private List<String> extraImagesList = new ArrayList<>();
    private ExtraImagesAdapter extraImagesAdapter;
    private ActivityResultLauncher<String> imagePickerLauncher;

    public static ZsellerProductDetailFragment newInstance(ZsellerProduct product) {
        ZsellerProductDetailFragment fragment = new ZsellerProductDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ID, product.getId());
        args.putString(ARG_IMAGE, product.getImageBase64());
        args.putString(ARG_NAME, product.getCarName());
        args.putString(ARG_MODEL, product.getModel());
        args.putString(ARG_PRICE, product.getPrice());
        args.putString(ARG_OFFERED, product.getOfferedPrice());
        args.putStringArrayList(ARG_EXTRA_IMAGES, new ArrayList<>(product.getExtraImages()));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                uploadExtraImage(uri);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_zseller_product_detail, container, false);

        ImageView backArrow = view.findViewById(R.id.backArrowImage);
        ImageView productImage = view.findViewById(R.id.detailProductImage);
        TextView nameText = view.findViewById(R.id.detailCarNameText);
        TextView modelText = view.findViewById(R.id.detailModelText);
        TextView priceText = view.findViewById(R.id.detailPriceText);
        TextView offeredText = view.findViewById(R.id.detailOfferedPriceText);
        ImageButton addExtraImageBtn = view.findViewById(R.id.addExtraImageBtn);
        RecyclerView extraImagesRecycler = view.findViewById(R.id.extraImagesRecycler);

        extraImagesRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        extraImagesAdapter = new ExtraImagesAdapter();
        extraImagesRecycler.setAdapter(extraImagesAdapter);

        Bundle args = getArguments();
        if (args != null) {
            productId = args.getString(ARG_ID);
            String imageBase64 = args.getString(ARG_IMAGE);
            String name = args.getString(ARG_NAME, "-");
            String model = args.getString(ARG_MODEL, "-");
            String price = args.getString(ARG_PRICE, "-");
            String offered = args.getString(ARG_OFFERED, "");
            List<String> extras = args.getStringArrayList(ARG_EXTRA_IMAGES);
            if (extras != null) {
                extraImagesList.addAll(extras);
            }

            Bitmap bitmap = base64ToBitmap(imageBase64);
            if (bitmap != null) {
                productImage.setImageBitmap(bitmap);
            } else {
                productImage.setImageResource(R.drawable.carimg);
            }

            nameText.setText(name);
            modelText.setText(getString(R.string.seller_model_value, model));
            priceText.setText(getString(R.string.seller_price_value, price));
            offeredText.setText(getString(R.string.seller_offered_price_value,
                    offered == null || offered.isEmpty() ? getString(R.string.seller_no_offer) : offered));
            
            extraImagesAdapter.setImages(extraImagesList);
        }

        backArrow.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        addExtraImageBtn.setOnClickListener(v -> {
            if (extraImagesList.size() >= 3) {
                Toast.makeText(getContext(), "Maximum 3 extra images allowed", Toast.LENGTH_SHORT).show();
            } else {
                imagePickerLauncher.launch("image/*");
            }
        });

        return view;
    }

    private void uploadExtraImage(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap == null) return;

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
            String base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);

            if (productId != null) {
                FirebaseFirestore.getInstance().collection("cars").document(productId)
                        .update("extraImages", FieldValue.arrayUnion(base64))
                        .addOnSuccessListener(unused -> {
                            extraImagesList.add(base64);
                            extraImagesAdapter.setImages(extraImagesList);
                            Toast.makeText(getContext(), "Image added", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add image", Toast.LENGTH_SHORT).show());
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error processing image", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap base64ToBitmap(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return null;
        }
        try {
            byte[] decodedBytes = Base64.decode(encoded, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            return null;
        }
    }
}
