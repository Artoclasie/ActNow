package com.example.actnow.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.actnow.Adapters.EventAdapter;
import com.example.actnow.Models.EventModel;
import com.example.actnow.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProfileRegisteredEventsFragment extends Fragment {

    private static final String ARG_USER_ID = "userId";
    private static final String TAG = "ProfileRegisteredEvents";
    private String userId;
    private RecyclerView recyclerView;
    private TextView tvMessage;
    private EventAdapter adapter;
    private List<EventModel> eventList = new ArrayList<>();
    private FirebaseFirestore db;

    public static ProfileRegisteredEventsFragment newInstance(String userId) {
        ProfileRegisteredEventsFragment fragment = new ProfileRegisteredEventsFragment();
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
            Log.d(TAG, "onCreate: userId = " + userId);
        } else {
            Log.e(TAG, "onCreate: No arguments provided");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_registered_events, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_events);
        tvMessage = view.findViewById(R.id.tv_message);

        if (recyclerView == null || tvMessage == null) {
            Log.e(TAG, "RecyclerView or tvMessage is null!");
            return view;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setNestedScrollingEnabled(false);
        adapter = new EventAdapter(requireContext(), eventList, event -> {
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

        db = FirebaseFirestore.getInstance();
        loadRegisteredEvents();

        return view;
    }

    private void loadRegisteredEvents() {
        Log.d(TAG, "loadRegisteredEvents started for userId: " + userId);
        if (userId == null) {
            Log.e(TAG, "userId is null!");
            tvMessage.setText("Ошибка: пользователь не авторизован");
            tvMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        Date currentDate = new Date();
        db.collection("events")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!isAdded()) return;

                    eventList.clear();
                    for (QueryDocumentSnapshot eventDoc : querySnapshot) {
                        String eventId = eventDoc.getId();
                        db.collection("events").document(eventId).collection("Participants")
                                .whereEqualTo("userId", userId.trim())
                                .get()
                                .addOnSuccessListener(participantsSnapshot -> {
                                    if (!participantsSnapshot.isEmpty()) {
                                        EventModel event = eventDoc.toObject(EventModel.class);
                                        event.setEventId(eventId);
                                        String status = eventDoc.getString("status");
                                        if (status == null) {
                                            if (event.getEndDate() != null && event.getEndDate().toDate().before(currentDate)) {
                                                status = "completed";
                                            } else {
                                                status = "active";
                                            }
                                        }
                                        if ("active".equals(status)) {
                                            eventList.add(event);
                                            Log.d(TAG, "Loaded registered event: " + event.getTitle());
                                        }
                                    }
                                    updateVisibility();
                                    adapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error loading participants for event " + eventId, e);
                                    updateVisibility();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading events", e);
                    tvMessage.setText("Ошибка загрузки данных");
                    tvMessage.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                });
    }

    private void updateVisibility() {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> {
            if (eventList.isEmpty()) {
                tvMessage.setVisibility(View.VISIBLE);
                tvMessage.setText("Нет зарегистрированных мероприятий");
                recyclerView.setVisibility(View.GONE);
            } else {
                tvMessage.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }
}