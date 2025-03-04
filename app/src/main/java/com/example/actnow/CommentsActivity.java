package com.example.actnow;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.actnow.Adapters.CommentAdapter;
import com.example.actnow.Models.CommentsModel;
import com.example.actnow.Models.PostModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CommentsActivity extends AppCompatActivity {

    ImageButton likes;
    TextView post_comments, post_likes;
    String name, postid;
    RecyclerView rv_comments;
    EditText et_comment;
    Button btn_comment;

    FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    PostModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        // Получаем postId из Intent
        String postId = getIntent().getStringExtra("postId");

        // Проверяем, чтобы postId не был null
        if (postId == null || postId.isEmpty()) {
            Toast.makeText(this, "Ошибка загрузки комментариев", Toast.LENGTH_SHORT).show();
            finish(); // Закрываем Activity, если postId пустой
            return;
        }

        // Используем postId для получения комментариев
        initvar(postId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                model = snapshot.getValue(PostModel.class);

                if (model != null && model.getLikes() != null) {
                    if (model.getLikes().contains(mAuth.getCurrentUser().getUid())) {
                        likes.setImageResource(R.drawable.like_filled);
                    } else {
                        likes.setImageResource(R.drawable.like);
                    }
                    post_likes.setText(String.valueOf(model.getLikes().size()));
                }

                if (model != null && model.getComments() != null) {
                    post_comments.setText(String.valueOf(model.getComments().size()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CommentsActivity.this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
            }
        });

        likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> likeslist = new ArrayList<>();
                if (model != null) {
                    if (likes.getDrawable().getConstantState().equals(ResourcesCompat.getDrawable(
                            getResources(), R.drawable.like, null).getConstantState())) {
                        likes.setImageResource(R.drawable.like_filled);
                        if (model.getLikes() != null) {
                            likeslist = new ArrayList<>(model.getLikes());
                        }
                        likeslist.add(mAuth.getCurrentUser().getUid());
                        post_likes.setText(String.valueOf(likeslist.size()));
                        model.setLikes(likeslist);
                        databaseReference.setValue(model);
                    } else if (likes.getDrawable().getConstantState().equals(ResourcesCompat.getDrawable(
                            getResources(), R.drawable.like_filled, null).getConstantState())) {
                        likes.setImageResource(R.drawable.like);
                        likeslist = new ArrayList<>(model.getLikes());
                        likeslist.remove(mAuth.getCurrentUser().getUid());
                        post_likes.setText(String.valueOf(likeslist.size()));
                        model.setLikes(likeslist);
                        databaseReference.setValue(model);
                    }
                }
            }
        });

        ArrayList<CommentsModel> commentsModelArrayList = new ArrayList<>();

        CommentAdapter adapter = new CommentAdapter(commentsModelArrayList, postid);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rv_comments.setLayoutManager(layoutManager);
        rv_comments.setAdapter(adapter);

        databaseReference.child("comments").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                commentsModelArrayList.add(snapshot.getValue(CommentsModel.class));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        btn_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_comment.getText().toString().equals("")) {
                    et_comment.setError("Введите комментарий");
                } else {
                    ArrayList<String> likes = new ArrayList<>();
                    Long timestamp = System.currentTimeMillis();
                    CommentsModel comment = new CommentsModel(mAuth.getCurrentUser().getUid(), et_comment.getText().toString(), String.valueOf(timestamp), likes);
                    commentsModelArrayList.add(comment);
                    model.setComments(commentsModelArrayList);  // Изменено на List<CommentsModel>

                    databaseReference.setValue(model);

                    commentsModelArrayList.remove(comment);
                    et_comment.setText("");
                }
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    // Передаем postId как параметр
    void initvar(String postId) {
        postid = postId;

        likes = findViewById(R.id.imb_post_likes);
        rv_comments = findViewById(R.id.rv_comments);

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        // Инициализируем databaseReference с использованием postId
        databaseReference = firebaseDatabase.getReference("Posts").child(postId);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Комментарии");
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
