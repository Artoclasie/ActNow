package com.example.actnow.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.actnow.Fragments.AnotherProfileFragment;
import com.example.actnow.Fragments.ProfileFragment;
import com.example.actnow.Models.PostReferenceModel;
import com.example.actnow.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostContentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_POST = 0;
    private static final int TYPE_LOADING = 1;

    private List<PostReferenceModel> postList;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final Context context;
    private boolean showLoading = false;

    public PostContentAdapter(List<PostReferenceModel> postReferenceModels, Context context) {
        this.postList = new ArrayList<>();
        if (postReferenceModels != null) {
            this.postList.addAll(postReferenceModels);
        }
        this.context = context;
        Log.d("PostAdapter", "Adapter created with " + postList.size() + " posts");
        for (PostReferenceModel post : postList) {
            Log.d("PostAdapter", "Post in adapter - ID: " + post.getId() +
                    ", AuthorId: " + post.getAuthorId() + ", Content: " + post.getContent() +
                    ", Username: " + post.getUsername() + ", LikesCount: " + post.getLikesCount());
        }
    }

    public void showLoading(boolean show) {
        if (showLoading != show) {
            showLoading = show;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (position == postList.size() && showLoading) ? TYPE_LOADING : TYPE_POST;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_LOADING) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        } else {
            View item = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.postcontent, parent, false);
            return new PostViewHolder(item);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PostViewHolder) {
            PostReferenceModel post = postList.get(position);
            PostViewHolder postHolder = (PostViewHolder) holder;
            bindPostView(postHolder, post);
        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingHolder = (LoadingViewHolder) holder;
            loadingHolder.progressBar.setIndeterminate(true);
        }
    }

    public void updatePosts(List<PostReferenceModel> newPosts) {
        this.postList.clear();
        if (newPosts != null) {
            this.postList.addAll(newPosts);
        }
        notifyDataSetChanged();
        Log.d("PostAdapter", "Updated with " + postList.size() + " posts");
        for (PostReferenceModel post : postList) {
            Log.d("PostAdapter", "Post in list: " + post.getContent() + ", AuthorId: " + post.getAuthorId() +
                    ", Username: " + post.getUsername() + ", LikesCount: " + post.getLikesCount());
        }
    }

    private void bindPostView(PostViewHolder holder, PostReferenceModel post) {
        Log.d("PostAdapter", "Binding post: " + post.getId() + ", content: " + post.getContent() +
                ", authorId: " + post.getAuthorId() + ", username: " + post.getUsername() +
                ", likesCount: " + post.getLikesCount());

        String currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        // Убираем кнопку "Отписаться/Подписаться" в этом контексте
        holder.tv_follow.setVisibility(View.GONE);

        // Устанавливаем данные
        holder.tv_post_uname.setText(post.getUsername() != null ? post.getUsername() : "Неизвестный пользователь");
        holder.tv_post_city.setText(post.getCity() != null ? post.getCity() : "");
        holder.tv_post_location.setText(post.getContent() != null ? post.getContent() : "");
        holder.tv_post_date.setText(post.getDate() != null ? post.getDate() : "");
        holder.tv_post_likes.setText(String.valueOf(post.getLikesCount() > 0 ? post.getLikesCount() : 0));

        // Загружаем иконку автора
        if (post.getProfileImageUrl() != null && !post.getProfileImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(post.getProfileImageUrl())
                    .circleCrop()
                    .placeholder(R.drawable.ic_person)
                    .into(holder.imv_post_uid);
        } else {
            holder.imv_post_uid.setImageResource(R.drawable.ic_person);
        }

        // Обработка изображения поста
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            holder.imv_post_content.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(post.getImageUrl())
                    .into(holder.imv_post_content);
        } else {
            holder.imv_post_content.setVisibility(View.GONE);
        }

        // Обработка лайков
        boolean isLiked = post.getLikes() != null && post.getLikes().contains(currentUserId);
        holder.imb_post_likes.setImageResource(isLiked ? R.drawable.ic_like : R.drawable.ic_dislike);

        holder.imb_post_likes.setOnClickListener(v -> {
            if (currentUserId == null) return;

            List<String> likes = post.getLikes() != null ? new ArrayList<>(post.getLikes()) : new ArrayList<>();
            int newLikesCount = post.getLikesCount();

            boolean newLikeState;
            if (likes.contains(currentUserId)) {
                likes.remove(currentUserId);
                newLikesCount--;
                newLikeState = false;
            } else {
                likes.add(currentUserId);
                newLikesCount++;
                newLikeState = true;
            }

            final int finalLikesCount = Math.max(0, newLikesCount);

            holder.imb_post_likes.setImageResource(newLikeState ? R.drawable.ic_like : R.drawable.ic_dislike);
            holder.tv_post_likes.setText(String.valueOf(finalLikesCount));

            Map<String, Object> updates = new HashMap<>();
            updates.put("likes", likes);
            updates.put("likesCount", finalLikesCount);

            db.collection("Posts").document(post.getId())
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        post.setLikes(likes);
                        post.setLikesCount(finalLikesCount);
                    })
                    .addOnFailureListener(e -> {
                        holder.imb_post_likes.setImageResource(isLiked ? R.drawable.ic_like : R.drawable.ic_dislike);
                        holder.tv_post_likes.setText(String.valueOf(post.getLikesCount()));
                        Toast.makeText(context, "Ошибка обновления лайков", Toast.LENGTH_SHORT).show();
                    });
        });

        View.OnClickListener profileClickListener = v -> {
            Log.d("PostAdapter", "Profile click listener triggered for post: " + post.getId());
            if (post.getAuthorId() == null) {
                Log.e("PostAdapter", "AuthorId is null for post: " + post.getId());
                Toast.makeText(context, "Ошибка: не удалось загрузить профиль", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentUserId != null && currentUserId.equals(post.getAuthorId())) {
                Log.d("PostAdapter", "Navigating to own profile (ProfileFragment)");
                if (context instanceof FragmentActivity) {
                    FragmentActivity activity = (FragmentActivity) context;
                    ProfileFragment profileFragment = new ProfileFragment();
                    activity.getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, profileFragment)
                            .addToBackStack(null)
                            .commit();
                }
            } else {
                Log.d("PostAdapter", "Navigating to another profile (AnotherProfileFragment)");
                if (context instanceof FragmentActivity) {
                    FragmentActivity activity = (FragmentActivity) context;
                    if (activity.isFinishing() || activity.isDestroyed()) return;
                    FragmentManager fragmentManager = activity.getSupportFragmentManager();
                    AnotherProfileFragment anotherProfileFragment = AnotherProfileFragment.newInstance(post.getAuthorId());
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, anotherProfileFragment, "AnotherProfileFragment")
                            .addToBackStack(null)
                            .commit();
                }
            }
        };

        holder.imv_post_uid.setOnClickListener(profileClickListener);
        holder.tv_post_uname.setOnClickListener(profileClickListener);
    }

    @Override
    public int getItemCount() {
        return postList.size() + (showLoading ? 1 : 0);
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imv_post_uid, imv_post_content;
        TextView tv_post_uname, tv_post_city, tv_post_location, tv_post_date, tv_post_likes;
        ImageButton imb_post_likes;
        TextView tv_follow, tv_post_delete;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            imv_post_uid = itemView.findViewById(R.id.imv_post_uid);
            imv_post_content = itemView.findViewById(R.id.imv_post_content);
            tv_post_uname = itemView.findViewById(R.id.tv_post_uname);
            tv_post_city = itemView.findViewById(R.id.tv_post_city);
            tv_post_location = itemView.findViewById(R.id.tv_post_location);
            tv_post_date = itemView.findViewById(R.id.tv_post_date);
            tv_post_likes = itemView.findViewById(R.id.tv_post_likes);
            imb_post_likes = itemView.findViewById(R.id.imb_post_likes);
            tv_post_delete = itemView.findViewById(R.id.tv_post_delete);
            tv_follow = itemView.findViewById(R.id.tv_follow);
        }
    }

    public static class LoadingViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;

        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    // Переопределяем, чтобы убрать логику подписки
    public void setupFollowButton(final TextView followButton, String authorId, String currentUserId, PostViewHolder holder) {
        followButton.setVisibility(View.GONE);
    }

    private void deletePost(PostReferenceModel post) {
        db.collection("Posts").document(post.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    int position = postList.indexOf(post);
                    if (position != -1) {
                        postList.remove(position);
                        notifyItemRemoved(position);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Ошибка удаления поста", Toast.LENGTH_SHORT).show();
                });
    }

    private Fragment getCurrentFragment() {
        if (context instanceof FragmentActivity) {
            FragmentActivity activity = (FragmentActivity) context;
            return activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        }
        return null;
    }
}