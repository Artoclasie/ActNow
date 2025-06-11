package com.example.actnow;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.actnow.Adapters.EventReportAdapter;
import com.example.actnow.Models.EventReportModel;
import com.example.actnow.Models.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class OrganizerProfileActivity extends AppCompatActivity {
    private ImageView ivProfileImage, ivCoverImage;
    private TextView tvUsername, tvBio, tvCity, tvEventsCount, tvRating, tvFollowers;
    private RecyclerView rvEventReports;
    private EventReportAdapter eventReportAdapter;
    private List<EventReportModel> eventReports;
    private FirebaseFirestore db;
    private String organizerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_profile);

        db = FirebaseFirestore.getInstance();
        organizerId = getIntent().getStringExtra("organizerId");
        if (organizerId == null) {
            Toast.makeText(this, "Организатор не найден", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        loadOrganizerData();
        loadEventReports();
    }

    private void initViews() {
        ivProfileImage = findViewById(R.id.imv_post_uid); // Изменено на правильный ID
        ivCoverImage = findViewById(R.id.iv_organizer_cover);
        tvUsername = findViewById(R.id.tv_organizer_username);
        tvBio = findViewById(R.id.tv_organizer_bio);
        tvCity = findViewById(R.id.tv_organizer_city);
        tvEventsCount = findViewById(R.id.tv_organizer_events_count);
        tvRating = findViewById(R.id.tv_organizer_rating);
        tvFollowers = findViewById(R.id.tv_organizer_followers);
        rvEventReports = findViewById(R.id.rv_event_reports);
    }

    private void setupRecyclerView() {
        eventReports = new ArrayList<>();
        eventReportAdapter = new EventReportAdapter(eventReports);
        rvEventReports.setLayoutManager(new LinearLayoutManager(this));
        rvEventReports.setAdapter(eventReportAdapter);
    }

    private void loadOrganizerData() {
        db.collection("Users")
                .document(organizerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserProfile organizer = documentSnapshot.toObject(UserProfile.class);
                        if (organizer != null) {
                            updateUI(organizer);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки данных организатора", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUI(UserProfile organizer) {
        tvUsername.setText(organizer.getUsername());
        tvBio.setText(organizer.getBio());
        tvCity.setText(organizer.getCity());
        tvEventsCount.setText(String.valueOf(organizer.getOrganizedEvents()));
        tvRating.setText(String.format("%.1f", organizer.getRating()));
        tvFollowers.setText(String.valueOf(organizer.getFollowersCount()));

        if (organizer.getProfileImage() != null && !organizer.getProfileImage().isEmpty()) {
            Glide.with(this)
                    .load(organizer.getProfileImage())
                    .into(ivProfileImage);
        }

        if (organizer.getBackgroundImageUrl() != null && !organizer.getBackgroundImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(organizer.getBackgroundImageUrl())
                    .into(ivCoverImage);
        }
    }

    private void loadEventReports() {
        db.collection("EventReports")
                .whereEqualTo("organizerId", organizerId)
                .orderBy("eventDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventReports.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        EventReportModel report = document.toObject(EventReportModel.class);
                        if (report != null) {
                            eventReports.add(report);
                        }
                    }
                    eventReportAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки отчетов", Toast.LENGTH_SHORT).show();
                });
    }
}