package com.example.apni_svari;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

public class SearchFragment extends Fragment {

    private EditText searchCarName, searchCarModel, searchCarPrice;
    private Button searchButton;
    private RecyclerView searchResultsRecycler;
    private final List<Car> searchResults = new ArrayList<>();
    private CarsAdapter adapter;
    private final FirestoreRepository repository = new FirestoreRepository();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Initialize views
        searchCarName = view.findViewById(R.id.searchCarName);
        searchCarModel = view.findViewById(R.id.searchCarModel);
        searchCarPrice = view.findViewById(R.id.searchCarPrice);
        searchButton = view.findViewById(R.id.searchButton);
        searchResultsRecycler = view.findViewById(R.id.searchResultsRecycler);

        // Setup RecyclerView
        searchResultsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CarsAdapter(searchResults, car -> {
            if (getActivity() instanceof MainUserPage) {
                ((MainUserPage) getActivity()).openBuyerDetailFragment(CarDetailFragment.newInstance(car.getId()));
            }
        });
        searchResultsRecycler.setAdapter(adapter);

        // Setup search button click listener
        searchButton.setOnClickListener(v -> performSearch());

        return view;
    }

    private void performSearch() {
        String name = searchCarName.getText().toString().trim();
        String model = searchCarModel.getText().toString().trim();
        String priceStr = searchCarPrice.getText().toString().trim();

        // Validate all fields are filled
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter car name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (model.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter car model", Toast.LENGTH_SHORT).show();
            return;
        }

        if (priceStr.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter price", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Please enter a valid price", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() == null
                ? null
                : FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Perform search with filtering
        repository.searchCars(name, model, price, currentUserId, loadedCars -> {
            searchResults.clear();
            searchResults.addAll(loadedCars);

            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }

            if (loadedCars.isEmpty()) {
                Toast.makeText(requireContext(), "No cars found matching your search", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Found " + loadedCars.size() + " car(s)", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
