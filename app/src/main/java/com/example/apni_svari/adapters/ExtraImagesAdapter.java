package com.example.apni_svari.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apni_svari.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple horizontal adapter for additional car images.
 * Supports either Base64 strings or http/https URLs.
 */
public class ExtraImagesAdapter extends RecyclerView.Adapter<ExtraImagesAdapter.ImageViewHolder> {

    private final List<String> images = new ArrayList<>();

    public void setImages(List<String> newImages) {
        images.clear();
        if (newImages != null) {
            images.addAll(newImages);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_extra_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String value = images.get(position);

        if (looksLikeUrl(value)) {
            com.squareup.picasso.Picasso.get()
                    .load(value)
                    .placeholder(R.drawable.carimg)
                    .into(holder.image);
            return;
        }

        Bitmap bitmap = decodeBase64(value);
        if (bitmap != null) {
            holder.image.setImageBitmap(bitmap);
        } else {
            holder.image.setImageResource(R.drawable.carimg);
        }
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.extraImageView);
        }
    }

    private boolean looksLikeUrl(String value) {
        if (TextUtils.isEmpty(value)) return false;
        String v = value.trim();
        return v.startsWith("http://") || v.startsWith("https://");
    }

    private Bitmap decodeBase64(String encoded) {
        if (encoded == null || encoded.trim().isEmpty()) {
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

