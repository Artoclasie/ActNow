package com.example.actnow.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.actnow.Adapters.EventAdapter;
import com.example.actnow.Models.EventModel;
import com.example.actnow.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventFragment extends Fragment implements HomeFragment.SearchableFragment {
    private static final String TAG = "EventFragment";
    private RecyclerView rvPosts;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private EventAdapter eventAdapter;
    private List<EventModel> events = new ArrayList<>();
    private List<EventModel> allEvents = new ArrayList<>();
    private FirebaseFirestore db;

    public EventFragment() {
        // Required empty public constructor
    }

    public static EventFragment newInstance() {
        return new EventFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event, container, false);
        initViews(view);
        setupRecyclerView();
        loadEvents();
        return view;
    }

    private void initViews(View view) {
        rvPosts = view.findViewById(R.id.rvPosts);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progress_bar);
        swipeRefreshLayout.setOnRefreshListener(this::loadEvents);
    }

    private void setupRecyclerView() {
        eventAdapter = new EventAdapter(requireContext(), events, event -> {
            EventDetailFragment fragment = new EventDetailFragment();
            Bundle args = new Bundle();
            args.putString("eventId", event.getEventId());
            args.putString("title", event.getTitle());
            args.putString("coverImage", event.getCoverImage());
            Log.d(TAG, "Passing event to DetailFragment: eventId=" + event.getEventId() + ", coverImage=" + event.getCoverImage());
            fragment.setArguments(args);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        rvPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPosts.setAdapter(eventAdapter);
    }

    private void loadEvents() {
        swipeRefreshLayout.setRefreshing(true);
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        Date currentDate = new Date();
        Log.d(TAG, "Current date: " + currentDate);

        db.collection("events")
                .orderBy("startDate", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allEvents.clear();
                    events.clear();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        EventModel event = snapshot.toObject(EventModel.class);
                        if (event != null && event.getStartDate() != null) {
                            event.setEventId(snapshot.getId());
                            String status = snapshot.getString("status");
                            Log.d(TAG, "Event " + event.getEventId() + " status from Firestore: " + status);
                            Log.d(TAG, "Event " + event.getEventId() + " endDate: " + event.getEndDate());

                            // Определяем статус, если он не задан или нужно обновить
                            if (status == null || "active".equals(status)) {
                                if (event.getEndDate() != null && event.getEndDate().toDate().before(currentDate)) {
                                    status = "completed";
                                    Log.d(TAG, "Updating status to 'completed' for event " + event.getEventId());
                                    db.collection("events").document(event.getEventId())
                                            .update("status", "completed")
                                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Status updated to completed"))
                                            .addOnFailureListener(e -> Log.e(TAG, "Failed to update status", e));
                                } else if (event.getCurrentParticipants() != null && event.getMaxParticipants() != null &&
                                        event.getCurrentParticipants() >= event.getMaxParticipants()) {
                                    status = "full";
                                    Log.d(TAG, "Status set to 'full'");
                                } else {
                                    status = "active";
                                    Log.d(TAG, "Status set to 'active'");
                                }
                            }
                            event.setStatus(status);

                            // Фильтруем: только "active" и с свободными местами
                            if ("active".equals(status) &&
                                    event.getCurrentParticipants() != null && event.getMaxParticipants() != null &&
                                    event.getCurrentParticipants() < event.getMaxParticipants()) {
                                Log.d(TAG, "Loaded event: eventId=" + event.getEventId() + ", coverImage=" + event.getCoverImage() + ", status=" + status);
                                allEvents.add(event);
                            } else {
                                Log.d(TAG, "Skipping event: eventId=" + event.getEventId() + ", status=" + status +
                                        ", currentParticipants=" + event.getCurrentParticipants() + ", maxParticipants=" + event.getMaxParticipants());
                            }
                        } else {
                            Log.w(TAG, "Skipping event with missing startDate or null event: " + snapshot.getId());
                        }
                    }
                    events.addAll(allEvents);
                    eventAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    if (events.isEmpty()) {
                        Toast.makeText(getContext(), "Мероприятия не найдены", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    swipeRefreshLayout.setRefreshing(false);
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    Log.e(TAG, "Failed to load events", e);
                    Toast.makeText(getContext(), "Ошибка загрузки мероприятий", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onSearch(String query, String filters) {
        Log.d(TAG, "Search query: " + query + ", filters: " + filters);
        if (query.isEmpty() && filters.isEmpty()) {
            loadEvents();
            return;
        }

        events.clear();
        for (EventModel event : allEvents) {
            String title = event.getTitle() != null ? event.getTitle().toLowerCase() : "";
            List<String> tags = event.getTags() != null ? event.getTags() : new ArrayList<>();
            boolean matchesQuery = query.isEmpty() || title.contains(query.toLowerCase());

            boolean matchesFilters = filters.isEmpty();
            if (!filters.isEmpty()) {
                String[] filterArray = filters.split(",");
                for (String filter : filterArray) {
                    if (tags.contains(filter)) {
                        matchesFilters = true;
                        break;
                    }
                }
            }

            // Учитываем фильтр по статусу и местам даже при поиске
            if (matchesQuery && matchesFilters && "active".equals(event.getStatus()) &&
                    event.getCurrentParticipants() != null && event.getMaxParticipants() != null &&
                    event.getCurrentParticipants() < event.getMaxParticipants()) {
                events.add(event);
            }
        }

        // Сортировка по дате начала
        events.sort((e1, e2) -> e1.getStartDate().compareTo(e2.getStartDate()));

        eventAdapter.notifyDataSetChanged();
        Log.d(TAG, "Search results, events size: " + events.size());
    }
}