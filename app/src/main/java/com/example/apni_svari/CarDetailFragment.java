package com.example.apni_svari;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.apni_svari.data.FirestoreRepository;
import com.example.apni_svari.models.Car;
import com.example.apni_svari.models.Proposal;
import com.example.apni_svari.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CarDetailFragment extends Fragment {

    private static final String ARG_CAR_ID = "arg_car_id";

    private final FirestoreRepository repository = new FirestoreRepository();
    private String carId;
    private Car currentCar;

    private ImageView carImage;
    private TextView priceText;
    private TextView ownerText;
    private TextView modelText;
    private EditText proposedPriceInput;
    private Button sendProposalBtn;

    public static CarDetailFragment newInstance(String carId) {
        CarDetailFragment fragment = new CarDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CAR_ID, carId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_car_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        carImage = view.findViewById(R.id.carDetailImage);
        priceText = view.findViewById(R.id.carDetailPrice);
        ownerText = view.findViewById(R.id.carDetailOwner);
        modelText = view.findViewById(R.id.carDetailModel);
        proposedPriceInput = view.findViewById(R.id.proposedPriceInput);
        sendProposalBtn = view.findViewById(R.id.sendProposalButton);

        view.findViewById(R.id.carDetailBack).setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        sendProposalBtn.setOnClickListener(v -> submitProposal());

        Bundle args = getArguments();
        if (args != null) {
            carId = args.getString(ARG_CAR_ID);
        }

        if (!TextUtils.isEmpty(carId)) {
            loadCar();
        } else {
            Toast.makeText(requireContext(), "Missing car id", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadCar() {
        FirebaseFirestore.getInstance().collection("cars").document(carId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        Toast.makeText(requireContext(), "Car not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    currentCar = snapshot.toObject(Car.class);
                    if (currentCar == null) {
                        currentCar = new Car();
                    }
                    currentCar.setId(snapshot.getId());
                    if (TextUtils.isEmpty(currentCar.getName())) {
                        currentCar.setName(snapshot.getString("carName"));
                    }
                    if (TextUtils.isEmpty(currentCar.getOwnerId())) {
                        currentCar.setOwnerId(snapshot.getString("ownerId"));
                    }
                    if (TextUtils.isEmpty(currentCar.getOwnerId())) {
                        currentCar.setOwnerId(snapshot.getString("ownerUid"));
                    }
                    if (TextUtils.isEmpty(currentCar.getOwnerName())) {
                        currentCar.setOwnerName(snapshot.getString("ownerName"));
                    }
                    if (TextUtils.isEmpty(currentCar.getImageBase64())) {
                        currentCar.setImageBase64(snapshot.getString("imageBase64"));
                    }
                    if (TextUtils.isEmpty(currentCar.getImageUrl())) {
                        currentCar.setImageUrl(snapshot.getString("imageUrl"));
                    }
                    if (TextUtils.isEmpty(currentCar.getModel())) {
                        currentCar.setModel(snapshot.getString("model"));
                    }
                    Number price = snapshot.getDouble("price");
                    if (price != null) {
                        currentCar.setPrice(price.doubleValue());
                    }

                    bindCarData();
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void bindCarData() {
        if (currentCar == null) {
            return;
        }

        if (currentCar.getImageBase64() != null && !currentCar.getImageBase64().isEmpty()) {
            try {
                byte[] decoded = android.util.Base64.decode(currentCar.getImageBase64(), android.util.Base64.DEFAULT);
                android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                if (bitmap != null) {
                    carImage.setImageBitmap(bitmap);
                } else {
                    carImage.setImageResource(R.drawable.carimg);
                }
            } catch (Exception e) {
                carImage.setImageResource(R.drawable.carimg);
            }
        } else if (currentCar.getImageUrl() != null && !currentCar.getImageUrl().isEmpty()) {
            com.squareup.picasso.Picasso.get().load(currentCar.getImageUrl()).placeholder(R.drawable.carimg).into(carImage);
        } else {
            carImage.setImageResource(R.drawable.carimg);
        }

        priceText.setText("Price: ₹ " + currentCar.getPrice());
        ownerText.setText("Owner: " + safe(currentCar.getOwnerName(), currentCar.getOwnerId()));
        modelText.setText("Model: " + safe(currentCar.getModel(), "-") );
    }

    private void submitProposal() {
        if (currentCar == null) {
            Toast.makeText(requireContext(), "Car not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        String priceStr = proposedPriceInput.getText().toString().trim();
        if (TextUtils.isEmpty(priceStr)) {
            proposedPriceInput.setError("Enter proposed price");
            return;
        }

        double proposedPrice;
        try {
            proposedPrice = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            proposedPriceInput.setError("Invalid number");
            return;
        }

        String buyerId = getCurrentUserId();
        if (TextUtils.isEmpty(buyerId)) {
            Toast.makeText(requireContext(), "Please sign in again", Toast.LENGTH_SHORT).show();
            return;
        }

        repository.fetchUserById(buyerId, user -> {
            Proposal proposal = new Proposal();
            proposal.setCarId(currentCar.getId());
            proposal.setBuyerId(buyerId);
            proposal.setBuyerName(resolveBuyerName(user));
            proposal.setOwnerId(currentCar.getOwnerId());
            proposal.setProposedPrice(proposedPrice);
            proposal.setCarModel(currentCar.getModel());
            proposal.setStatus("pending");
            proposal.setTimestamp(System.currentTimeMillis());

            repository.createProposal(proposal, (success, errorMessage) -> requireActivity().runOnUiThread(() -> {
                if (success) {
                    Toast.makeText(requireContext(), "Proposal sent", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                } else {
                    Toast.makeText(requireContext(), errorMessage == null ? "Failed to send proposal" : errorMessage, Toast.LENGTH_SHORT).show();
                }
            }));
        });
    }

    private String resolveBuyerName(User user) {
        if (user != null && !TextUtils.isEmpty(user.getName())) {
            return user.getName();
        }
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            if (!TextUtils.isEmpty(FirebaseAuth.getInstance().getCurrentUser().getDisplayName())) {
                return FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
            }
            if (!TextUtils.isEmpty(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                return FirebaseAuth.getInstance().getCurrentUser().getEmail();
            }
        }
        return buyerIdFallback();
    }

    private String buyerIdFallback() {
        return getCurrentUserId();
    }

    private String getCurrentUserId() {
        return FirebaseAuth.getInstance().getCurrentUser() == null ? null : FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}

