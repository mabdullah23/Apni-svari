package com.example.apni_svari;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ZsellerProductDetailFragment extends Fragment {

    private static final String ARG_IMAGE = "arg_image";
    private static final String ARG_NAME = "arg_name";
    private static final String ARG_MODEL = "arg_model";
    private static final String ARG_PRICE = "arg_price";
    private static final String ARG_OFFERED = "arg_offered";

    public static ZsellerProductDetailFragment newInstance(ZsellerProduct product) {
        ZsellerProductDetailFragment fragment = new ZsellerProductDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE, product.getImageBase64());
        args.putString(ARG_NAME, product.getCarName());
        args.putString(ARG_MODEL, product.getModel());
        args.putString(ARG_PRICE, product.getPrice());
        args.putString(ARG_OFFERED, product.getOfferedPrice());
        fragment.setArguments(args);
        return fragment;
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

        Bundle args = getArguments();
        if (args != null) {
            String imageBase64 = args.getString(ARG_IMAGE);
            String name = args.getString(ARG_NAME, "-");
            String model = args.getString(ARG_MODEL, "-");
            String price = args.getString(ARG_PRICE, "-");
            String offered = args.getString(ARG_OFFERED, "");

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
        }

        backArrow.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        return view;
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

