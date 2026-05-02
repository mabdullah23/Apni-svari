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
import com.example.apni_svari.models.Car;

import java.util.List;

public class CarsAdapter extends RecyclerView.Adapter<CarsAdapter.CarViewHolder> {

    public interface OnCarClickListener {
        void onCarClick(Car car);
    }

    private final List<Car> cars;
    private final OnCarClickListener listener;

    public CarsAdapter(List<Car> cars, OnCarClickListener listener) {
        this.cars = cars;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_car, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        Car car = cars.get(position);
        holder.carName.setText(car.getName() == null ? "Unnamed car" : car.getName());
        holder.ownerName.setText("Owner: " + (car.getOwnerName() == null ? "Unknown" : car.getOwnerName()));
        holder.price.setText("Price: ₹ " + car.getPrice());

        Bitmap bitmap = decodeImage(car.getImageBase64());
        if (bitmap != null) {
            holder.carImage.setImageBitmap(bitmap);
        } else if (car.getImageUrl() != null && !car.getImageUrl().isEmpty()) {
            com.squareup.picasso.Picasso.get().load(car.getImageUrl()).placeholder(R.drawable.carimg).into(holder.carImage);
        } else {
            holder.carImage.setImageResource(R.drawable.carimg);
        }

        holder.itemView.setOnClickListener(v -> listener.onCarClick(car));
    }

    @Override
    public int getItemCount() {
        return cars.size();
    }

    public void notifyDataSetChangedSafely() {
        notifyDataSetChanged();
    }

    static class CarViewHolder extends RecyclerView.ViewHolder {
        ImageView carImage;
        TextView carName;
        TextView ownerName;
        TextView price;

        CarViewHolder(@NonNull View itemView) {
            super(itemView);
            carImage = itemView.findViewById(R.id.carItemImage);
            carName = itemView.findViewById(R.id.carItemName);
            ownerName = itemView.findViewById(R.id.carItemOwner);
            price = itemView.findViewById(R.id.carItemPrice);
        }
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

