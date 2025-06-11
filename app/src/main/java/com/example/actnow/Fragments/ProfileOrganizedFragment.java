package com.example.actnow.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.actnow.Adapters.EventAdapter;
import com.example.actnow.Models.EventModel;
import com.example.actnow.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ProfileOrganizedFragment extends Fragment {

    private static final String TAG = "ProfileOrganizedFragment";
    private static final String ARG_USER_ID = "userId";

    private RecyclerView recyclerView;
    private TextView tvEmptyState;
    private TextInputEditText etSearch;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EventAdapter adapter;
    private List<EventModel> eventList = new ArrayList<>();
    private List<EventModel> filteredEventList = new ArrayList<>();
    private String userId;

    public static ProfileOrganizedFragment newInstance(String userId) {
        ProfileOrganizedFragment fragment = new ProfileOrganizedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_organized, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.recyclerView);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        etSearch = view.findViewById(R.id.etSearch);

        if (recyclerView == null || tvEmptyState == null || etSearch == null) {
            Log.e(TAG, "RecyclerView, tvEmptyState, or etSearch is null!");
            Toast.makeText(requireContext(), "Ошибка: Элементы не найдены", Toast.LENGTH_LONG).show();
            return;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setNestedScrollingEnabled(false);
        adapter = new EventAdapter(requireContext(), filteredEventList, event -> {
            try {
                EventDetailFragment fragment = new EventDetailFragment();
                Bundle args = new Bundle();
                args.putString("eventId", event.getEventId());
                args.putString("title", event.getTitle());
                args.putString("coverImage", event.getCoverImage());
                Log.d(TAG, "Navigating to EventDetailFragment: eventId=" + event.getEventId());
                fragment.setArguments(args);
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            } catch (Exception e) {
                Log.e(TAG, "Navigation error: " + e.getMessage(), e);
            }
        });
        recyclerView.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEvents(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        Chip chipPast = view.findViewById(R.id.chipPast);
        Chip chipFilled = view.findViewById(R.id.chipFilled);
        Chip chipUpcoming = view.findViewById(R.id.chipUpcoming);

        chipPast.setOnCheckedChangeListener((buttonView, isChecked) -> filterEvents(etSearch.getText().toString()));
        chipFilled.setOnCheckedChangeListener((buttonView, isChecked) -> filterEvents(etSearch.getText().toString()));
        chipUpcoming.setOnCheckedChangeListener((buttonView, isChecked) -> filterEvents(etSearch.getText().toString()));

        loadOrganizedEvents();
    }

    private void loadOrganizedEvents() {
        if (userId == null) {
            Log.e(TAG, "userId is null!");
            Toast.makeText(requireContext(), "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Loading organized events for userId: " + userId);
        db.collection("events")
                .whereEqualTo("organizerId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!isAdded()) return;

                    eventList.clear();
                    if (!querySnapshot.isEmpty()) {
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            EventModel event = document.toObject(EventModel.class);
                            event.setEventId(document.getId());
                            eventList.add(event);
                            Log.d(TAG, "Loaded event: " + event.getTitle());
                        }
                    }
                    filteredEventList.clear();
                    filteredEventList.addAll(eventList);
                    filterEvents(etSearch.getText().toString());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Ошибка загрузки организованных мероприятий: " + e.getMessage(), e);
                    Toast.makeText(requireContext(), "Ошибка загрузки мероприятий", Toast.LENGTH_SHORT).show();
                    updateVisibility();
                });
    }

    private void filterEvents(String query) {
        filteredEventList.clear();
        Set<String> selectedFilters = new HashSet<>();
        Chip chipPast = requireView().findViewById(R.id.chipPast);
        Chip chipFilled = requireView().findViewById(R.id.chipFilled);
        Chip chipUpcoming = requireView().findViewById(R.id.chipUpcoming);

        if (chipPast.isChecked()) selectedFilters.add("past");
        if (chipFilled.isChecked()) selectedFilters.add("filled");
        if (chipUpcoming.isChecked()) selectedFilters.add("upcoming");

        String lowerCaseQuery = query.toLowerCase(Locale.getDefault());
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (EventModel event : eventList) {
            boolean matchesQuery = (event.getTitle() != null && event.getTitle().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) ||
                    (event.getAddress() != null && event.getAddress().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery));
            boolean matchesFilter = true;

            if (!selectedFilters.isEmpty()) {
                matchesFilter = false;
                if (selectedFilters.contains("past") && event.getEndDate() != null && event.getEndDate().toDate().before(currentDate)) {
                    matchesFilter = true;
                }
                if (selectedFilters.contains("upcoming") && event.getStartDate() != null && event.getStartDate().toDate().after(currentDate)) {
                    matchesFilter = true;
                }
                if (selectedFilters.contains("filled") && event.getCurrentParticipants() != null && event.getMaxParticipants() != null &&
                        event.getCurrentParticipants() >= event.getMaxParticipants()) {
                    matchesFilter = true;
                }
            }

            if (matchesQuery && matchesFilter) {
                filteredEventList.add(event);
            }
        }

        adapter.notifyDataSetChanged();
        updateVisibility();
    }

    private void updateVisibility() {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> {
            if (filteredEventList.isEmpty()) {
                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText("Нет организованных мероприятий");
                recyclerView.setVisibility(View.GONE);
            } else {
                tvEmptyState.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }
}