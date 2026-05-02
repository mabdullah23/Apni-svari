package com.example.apni_svari;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ZsellerHomeAdapter extends RecyclerView.Adapter<ZsellerHomeAdapter.ProductViewHolder> {

    public interface OnProductActionListener {
        void onProductClick(ZsellerProduct product);
        void onDeleteClick(ZsellerProduct product, int position);
    }

    private final List<ZsellerProduct> products;
    private final OnProductActionListener listener;

    public ZsellerHomeAdapter(List<ZsellerProduct> products, OnProductActionListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_zseller_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ZsellerProduct product = products.get(position);

        holder.carName.setText(product.getCarName());
        holder.model.setText(holder.itemView.getContext().getString(R.string.seller_model_value, product.getModel()));
        holder.price.setText(holder.itemView.getContext().getString(R.string.seller_price_value, product.getPrice()));
        holder.offeredPrice.setText(holder.itemView.getContext().getString(
                R.string.seller_offered_price_value,
                product.getOfferedPrice() == null || product.getOfferedPrice().isEmpty()
                        ? holder.itemView.getContext().getString(R.string.seller_no_offer)
                        : product.getOfferedPrice()));

        Bitmap bitmap = base64ToBitmap(product.getImageBase64());
        if (bitmap != null) {
            holder.productImage.setImageBitmap(bitmap);
        } else {
            holder.productImage.setImageResource(R.drawable.carimg);
        }

        holder.deleteIcon.setOnClickListener(v -> listener.onDeleteClick(product, holder.getAdapterPosition()));
        holder.itemView.setOnClickListener(v -> listener.onProductClick(product));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        ImageView deleteIcon;
        TextView carName;
        TextView model;
        TextView price;
        TextView offeredPrice;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            deleteIcon = itemView.findViewById(R.id.deleteIcon);
            carName = itemView.findViewById(R.id.carNameText);
            model = itemView.findViewById(R.id.modelText);
            price = itemView.findViewById(R.id.priceText);
            offeredPrice = itemView.findViewById(R.id.offeredPriceText);
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

