package com.example.apni_svari.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apni_svari.R;
import com.example.apni_svari.models.Proposal;

import java.util.List;

public class ProposalsAdapter extends RecyclerView.Adapter<ProposalsAdapter.ProposalViewHolder> {

    public interface OnProposalActionListener {
        void onAccept(Proposal proposal);
        void onReject(Proposal proposal);
    }

    private final List<Proposal> proposals;
    private final OnProposalActionListener listener;

    public ProposalsAdapter(List<Proposal> proposals, OnProposalActionListener listener) {
        this.proposals = proposals;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProposalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_proposal, parent, false);
        return new ProposalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProposalViewHolder holder, int position) {
        Proposal proposal = proposals.get(position);
        
        holder.buyerName.setText(safeText(proposal.getBuyerName(), proposal.getBuyerId()));
        holder.buyerEmail.setText(safeText(proposal.getBuyerEmail(), "Email not available"));
        holder.proposedPrice.setText("₹ " + formatPrice(proposal.getProposedPrice()));
        holder.originalPrice.setText("₹ " + formatPrice(proposal.getOriginalPrice()));
        
        // Display car name and model
        String carDisplay = safeText(proposal.getCarName(), proposal.getCarModel());
        if (!carDisplay.equals(proposal.getCarModel()) && !safeText(proposal.getCarModel(), "").isEmpty()) {
            carDisplay += " - " + proposal.getCarModel();
        }
        holder.carModel.setText(carDisplay);
        
        String statusText = safeText(proposal.getStatus(), "pending");
        holder.status.setText(statusText.substring(0, 1).toUpperCase() + statusText.substring(1));
        
        // Display car image if available
        if (proposal.getCarImage() != null && !proposal.getCarImage().isEmpty()) {
            try {
                byte[] decoded = Base64.decode(proposal.getCarImage(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                if (bitmap != null) {
                    holder.carImage.setImageBitmap(bitmap);
                } else {
                    holder.carImage.setImageResource(R.drawable.carimg);
                }
            } catch (Exception e) {
                holder.carImage.setImageResource(R.drawable.carimg);
            }
        } else {
            holder.carImage.setImageResource(R.drawable.carimg);
        }

        holder.acceptBtn.setOnClickListener(v -> listener.onAccept(proposal));
        holder.rejectBtn.setOnClickListener(v -> listener.onReject(proposal));
    }
    
    private String formatPrice(double price) {
        if (price == 0) {
            return "0";
        }
        return String.valueOf((long) price);
    }

    @Override
    public int getItemCount() {
        return proposals.size();
    }

    static class ProposalViewHolder extends RecyclerView.ViewHolder {
        TextView buyerName;
        TextView buyerEmail;
        TextView proposedPrice;
        TextView originalPrice;
        TextView carModel;
        TextView status;
        ImageView carImage;
        Button acceptBtn;
        Button rejectBtn;

        ProposalViewHolder(@NonNull View itemView) {
            super(itemView);
            buyerName = itemView.findViewById(R.id.proposalBuyerName);
            buyerEmail = itemView.findViewById(R.id.proposalBuyerEmail);
            proposedPrice = itemView.findViewById(R.id.proposalPrice);
            originalPrice = itemView.findViewById(R.id.proposalOriginalPrice);
            carModel = itemView.findViewById(R.id.proposalCarModel);
            status = itemView.findViewById(R.id.proposalStatus);
            carImage = itemView.findViewById(R.id.proposalCarImage);
            acceptBtn = itemView.findViewById(R.id.btnAcceptProposal);
            rejectBtn = itemView.findViewById(R.id.btnRejectProposal);
        }
    }

    private String safeText(String primary, String fallback) {
        return primary == null || primary.trim().isEmpty() ? fallback : primary;
    }
}

