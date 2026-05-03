package com.example.apni_svari;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apni_svari.adapters.HistoryCarsAdapter;
import com.example.apni_svari.data.FirestoreRepository;
import com.example.apni_svari.models.HistoryCar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private final List<HistoryCar> historyCars = new ArrayList<>();
    private final FirestoreRepository repository = new FirestoreRepository();
    private HistoryCarsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.historyRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new HistoryCarsAdapter(historyCars);
        recyclerView.setAdapter(adapter);

        loadHistory();
    }

    private void loadHistory() {
        String buyerId = FirebaseAuth.getInstance().getCurrentUser() == null
                ? null
                : FirebaseAuth.getInstance().getCurrentUser().getUid();

        repository.fetchHistoryCarsForBuyer(buyerId, loaded -> {
            historyCars.clear();
            historyCars.addAll(loaded);
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        });
    }
}

