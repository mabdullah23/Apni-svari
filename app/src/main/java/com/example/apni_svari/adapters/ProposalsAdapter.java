package com.example.apni_svari.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
        holder.buyerName.setText("Requester: " + safeText(proposal.getBuyerName(), proposal.getBuyerId()));
        holder.proposedPrice.setText("Proposed price: ₹ " + proposal.getProposedPrice());
        holder.carModel.setText("Car model: " + safeText(proposal.getCarModel(), "-") );
        holder.status.setVisibility(View.GONE);

        holder.acceptBtn.setOnClickListener(v -> listener.onAccept(proposal));
        holder.rejectBtn.setOnClickListener(v -> listener.onReject(proposal));
    }

    @Override
    public int getItemCount() {
        return proposals.size();
    }

    static class ProposalViewHolder extends RecyclerView.ViewHolder {
        TextView buyerName;
        TextView proposedPrice;
        TextView carModel;
        TextView status;
        Button acceptBtn;
        Button rejectBtn;

        ProposalViewHolder(@NonNull View itemView) {
            super(itemView);
            buyerName = itemView.findViewById(R.id.proposalBuyerName);
            proposedPrice = itemView.findViewById(R.id.proposalPrice);
            carModel = itemView.findViewById(R.id.proposalCarModel);
            status = itemView.findViewById(R.id.proposalStatus);
            acceptBtn = itemView.findViewById(R.id.btnAcceptProposal);
            rejectBtn = itemView.findViewById(R.id.btnRejectProposal);
        }
    }

    private String safeText(String primary, String fallback) {
        return primary == null || primary.trim().isEmpty() ? fallback : primary;
    }
}

