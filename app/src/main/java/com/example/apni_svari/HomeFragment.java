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

import com.example.apni_svari.adapters.CarsAdapter;
import com.example.apni_svari.data.FirestoreRepository;
import com.example.apni_svari.models.Car;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private final List<Car> cars = new ArrayList<>();
    private CarsAdapter adapter;
    private final FirestoreRepository repository = new FirestoreRepository();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.homeCarsRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CarsAdapter(cars, car -> {
            if (getActivity() instanceof MainUserPage) {
                ((MainUserPage) getActivity()).openBuyerDetailFragment(CarDetailFragment.newInstance(car.getId()));
            }
        });
        recyclerView.setAdapter(adapter);

        loadCars();
        return view;
    }

    private void loadCars() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() == null
                ? null
                : FirebaseAuth.getInstance().getCurrentUser().getUid();

        repository.fetchCarsForBuyer(currentUserId, loadedCars -> {
            cars.clear();
            cars.addAll(loadedCars);
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        });
    }
}
