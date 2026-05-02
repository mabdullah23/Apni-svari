package com.example.apni_svari;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apni_svari.adapters.ProposalsAdapter;
import com.example.apni_svari.data.FirestoreRepository;
import com.example.apni_svari.models.Proposal;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class ProposalsFragment extends Fragment {

    private static final int REQ_SMS = 901;

    private final List<Proposal> proposals = new ArrayList<>();
    private final FirestoreRepository repository = new FirestoreRepository();

    private ProposalsAdapter adapter;
    private Proposal pendingProposal;
    private String pendingAction;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_proposals, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.proposalsRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ProposalsAdapter(proposals, new ProposalsAdapter.OnProposalActionListener() {
            @Override
            public void onAccept(Proposal proposal) {
                runProposalAction(proposal, "accept");
            }

            @Override
            public void onReject(Proposal proposal) {
                runProposalAction(proposal, "reject");
            }
        });
        recyclerView.setAdapter(adapter);

        loadProposals();
    }

    private void loadProposals() {
        String ownerId = getCurrentUserId();
        repository.fetchProposalsForOwner(ownerId, loaded -> {
            proposals.clear();
            proposals.addAll(loaded);
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void runProposalAction(Proposal proposal, String action) {
        if (hasSmsPermission()) {
            executeProposalAction(proposal, action);
            return;
        }

        pendingProposal = proposal;
        pendingAction = action;
        requestPermissions(new String[]{Manifest.permission.SEND_SMS}, REQ_SMS);
    }

    private void executeProposalAction(Proposal proposal, String action) {
        if ("accept".equals(action)) {
            repository.acceptProposal(proposal, (success, errorMessage) -> requireActivity().runOnUiThread(() -> {
                if (success) {
                    Toast.makeText(requireContext(), "Proposal accepted", Toast.LENGTH_SHORT).show();
                    loadProposals();
                } else {
                    Toast.makeText(requireContext(), errorMessage == null ? "Failed to accept proposal" : errorMessage, Toast.LENGTH_SHORT).show();
                }
            }));
        } else {
            repository.rejectProposal(proposal, (success, errorMessage) -> requireActivity().runOnUiThread(() -> {
                if (success) {
                    Toast.makeText(requireContext(), "Proposal rejected", Toast.LENGTH_SHORT).show();
                    loadProposals();
                } else {
                    Toast.makeText(requireContext(), errorMessage == null ? "Failed to reject proposal" : errorMessage, Toast.LENGTH_SHORT).show();
                }
            }));
        }
    }

    private boolean hasSmsPermission() {
        return ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private String getCurrentUserId() {
        return FirebaseAuth.getInstance().getCurrentUser() == null ? null : FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_SMS && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (pendingProposal != null && pendingAction != null) {
                executeProposalAction(pendingProposal, pendingAction);
            }
        } else if (requestCode == REQ_SMS) {
            Toast.makeText(requireContext(), "SMS permission is required to notify buyers.", Toast.LENGTH_SHORT).show();
        }
        pendingProposal = null;
        pendingAction = null;
    }
}

