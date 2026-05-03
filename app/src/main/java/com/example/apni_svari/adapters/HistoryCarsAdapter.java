package com.example.apni_svari.adapters;

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

import com.example.apni_svari.R;
import com.example.apni_svari.models.HistoryCar;

import java.util.List;

public class HistoryCarsAdapter extends RecyclerView.Adapter<HistoryCarsAdapter.HistoryViewHolder> {

    private final List<HistoryCar> historyCars;

    public HistoryCarsAdapter(List<HistoryCar> historyCars) {
        this.historyCars = historyCars;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_car, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryCar historyCar = historyCars.get(position);

        holder.carName.setText(historyCar.getCarName() == null || historyCar.getCarName().trim().isEmpty()
                ? "Unknown car"
                : historyCar.getCarName());
        holder.ownerName.setText("Owner: " + safe(historyCar.getOwnerName(), historyCar.getOwnerId()));
        holder.model.setText("Model: " + safe(historyCar.getModel(), "-") );
        holder.acceptedPrice.setText("Accepted price: ₹ " + historyCar.getAcceptedPrice());

        Bitmap bitmap = decodeImage(historyCar.getImageBase64());
        if (bitmap != null) {
            holder.carImage.setImageBitmap(bitmap);
        } else if (historyCar.getImageUrl() != null && !historyCar.getImageUrl().isEmpty()) {
            com.squareup.picasso.Picasso.get().load(historyCar.getImageUrl()).placeholder(R.drawable.carimg).into(holder.carImage);
        } else {
            holder.carImage.setImageResource(R.drawable.carimg);
        }
    }

    @Override
    public int getItemCount() {
        return historyCars.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        ImageView carImage;
        TextView carName;
        TextView ownerName;
        TextView model;
        TextView acceptedPrice;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            carImage = itemView.findViewById(R.id.historyCarImage);
            carName = itemView.findViewById(R.id.historyCarName);
            ownerName = itemView.findViewById(R.id.historyCarOwner);
            model = itemView.findViewById(R.id.historyCarModel);
            acceptedPrice = itemView.findViewById(R.id.historyCarAcceptedPrice);
        }
    }

    private String safe(String primary, String fallback) {
        return primary == null || primary.trim().isEmpty() ? fallback : primary;
    }

    private Bitmap decodeImage(String encoded) {
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

