package com.example.actnow.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.actnow.Models.PostSearchModel;
import com.example.actnow.Models.ProfileModel;
import com.example.actnow.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class PostSearchAdapter extends RecyclerView.Adapter<PostSearchAdapter.viewholder> {

    // Используем список объектов PostSearchModel вместо строковых ID
    ArrayList<PostSearchModel> postList;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    // Правильное объявление коллекции
    CollectionReference collectionReference = firebaseFirestore.collection("Profiles");

    public PostSearchAdapter(ArrayList<PostSearchModel> postList) {
        this.postList = postList;
    }

    @NonNull
    @Override
    public viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.postcontent, parent, false);
        return new viewholder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull viewholder holder, int position) {
        PostSearchModel postModel = postList.get(position);

        // Сброс ранее отображённого содержимого
        holder.imv_post_uid.setImageDrawable(null);
        holder.imv_post_content.setImageDrawable(null);
        holder.et_post_content.setText("");
        holder.tv_post_uname.setText("");
        holder.tv_post_date.setText("");
        holder.tv_post_likes.setText("0");
        holder.tv_post_comments.setText("0");
        holder.imb_post_likes.setImageResource(R.drawable.like);
        holder.imb_content_delete.setVisibility(View.GONE);

        // Проверка на null для postModel.getUserId() - заменено на userId
        if (postModel.getId() != null && !postModel.getId().isEmpty()) {
            // Получаем данные профиля автора поста через userId
            collectionReference.document(postModel.getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        ProfileModel profileModel = task.getResult().toObject(ProfileModel.class);
                        if (profileModel != null) {
                            holder.tv_post_uname.setText(profileModel.getUsername());

                            // Загружаем аватар профиля через Cloudinary URL
                            String profileImageUrl = profileModel.getProfileImageUrl();
                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                Glide.with(holder.imv_post_uid.getContext())
                                        .load(profileImageUrl)
                                        .into(holder.imv_post_uid);
                            }
                        } else {
                            // Если профиль не найден или пуст, показываем сообщение
                            Toast.makeText(holder.itemView.getContext(), "Ошибка: Профиль не найден.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Обработка ошибки при запросе профиля
                        Toast.makeText(holder.itemView.getContext(), "Ошибка при загрузке профиля.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            // Сообщение о пустом userId
            Toast.makeText(holder.itemView.getContext(), "Ошибка: userId поста пустой.", Toast.LENGTH_SHORT).show();
        }

        // Загружаем изображение поста через Cloudinary URL, если оно не пустое
        String postImageUrl = postModel.getImageUrl();
        if (postImageUrl != null && !postImageUrl.isEmpty()) {
            Glide.with(holder.imv_post_content.getContext())
                    .load(postImageUrl)
                    .into(holder.imv_post_content);
            holder.imv_post_content.setVisibility(View.VISIBLE);
        } else {
            holder.imv_post_content.setVisibility(View.GONE);
        }

        // Загружаем текст контента поста
        holder.et_post_content.setText(postModel.getContent());

        // Форматируем дату
        String date = android.text.format.DateFormat.format("hh:mm aa   dd-MM-yyyy",
                Long.parseLong(postModel.getTimestamp())).toString();
        holder.tv_post_date.setText(date);

        // Обработка лайков
        if (postModel.getLikes() != null) {
            if (postModel.getLikes().contains(mAuth.getCurrentUser().getUid())) {
                holder.imb_post_likes.setImageResource(R.drawable.like_filled);
            }
            holder.tv_post_likes.setText(String.valueOf(postModel.getLikes().size()));
        }

        // Обработка комментариев
        if (postModel.getComments() != null) {
            holder.tv_post_comments.setText(String.valueOf(postModel.getComments().size()));
        }

        // Обработка клика на лайк
        holder.imb_post_likes.setOnClickListener(v -> {
            databaseReference = firebaseDatabase.getReference("Posts").child(postModel.getId());
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() == null) {
                        Toast.makeText(holder.itemView.getContext(), "Post Deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        ArrayList<String> likes = postModel.getLikes() != null ? postModel.getLikes() : new ArrayList<>();
                        if (holder.imb_post_likes.getDrawable().getConstantState().equals(
                                ResourcesCompat.getDrawable(holder.itemView.getResources(), R.drawable.like, null).getConstantState())) {
                            holder.imb_post_likes.setImageResource(R.drawable.like_filled);
                            likes.add(mAuth.getCurrentUser().getUid());
                            holder.tv_post_likes.setText(String.valueOf(likes.size()));
                        } else if (holder.imb_post_likes.getDrawable().getConstantState().equals(
                                ResourcesCompat.getDrawable(holder.itemView.getResources(), R.drawable.like_filled, null).getConstantState())) {
                            holder.imb_post_likes.setImageResource(R.drawable.like);
                            likes.remove(mAuth.getCurrentUser().getUid());
                            holder.tv_post_likes.setText(String.valueOf(likes.size()));
                        }
                        postModel.setLikes(likes);
                        databaseReference.setValue(postModel);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class viewholder extends RecyclerView.ViewHolder {
        ImageView imv_post_uid, imv_post_content;
        TextView tv_post_uname, tv_post_date, tv_post_comments, tv_post_likes;
        EditText et_post_content;
        ImageButton imb_post_comments, imb_post_likes, imb_content_delete;

        public viewholder(@NonNull View itemView) {
            super(itemView);
            imv_post_uid = itemView.findViewById(R.id.imv_post_uid);
            imv_post_content = itemView.findViewById(R.id.imv_post_content);
            tv_post_uname = itemView.findViewById(R.id.tv_post_uname);
            tv_post_date = itemView.findViewById(R.id.tv_post_date);
            tv_post_likes = itemView.findViewById(R.id.tv_post_likes);
            imb_post_likes = itemView.findViewById(R.id.imb_post_likes);
        }
    }
}
