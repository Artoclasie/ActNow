package com.example.actnow.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.actnow.Models.CommentModel;
import com.example.actnow.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final Context context;
    private final List<CommentModel> commentList;
    private final FirebaseUser currentUser;
    private final OnCommentInteractionListener listener;

    public interface OnCommentInteractionListener {
        void onLikeComment(String commentId, boolean isLiked);
        void onDeleteComment(CommentModel comment);
    }

    public CommentAdapter(Context context, List<CommentModel> commentList, OnCommentInteractionListener listener) {
        this.context = context;
        this.commentList = commentList;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.listener = listener;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.postcomment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentModel comment = commentList.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public void updateComments(List<CommentModel> newComments) {
        commentList.clear();
        commentList.addAll(newComments);
        notifyDataSetChanged();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        private final CardView userAvatarCard;
        private final ImageView userAvatar;
        private final TextView usernameText;
        private final TextView commentText;
        private final TextView timeText;
        private final TextView likesCountText;
        private final ImageButton likeButton;
        private final TextView deleteText; // Изменили на TextView вместо ImageButton

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            userAvatarCard = itemView.findViewById(R.id.cv_comment_uid);
            userAvatar = itemView.findViewById(R.id.imv_comment_uid);
            usernameText = itemView.findViewById(R.id.tv_comment_uname);
            commentText = itemView.findViewById(R.id.et_post_comment); // Изменили на TextView
            timeText = itemView.findViewById(R.id.tv_comment_date);
            likesCountText = itemView.findViewById(R.id.tv_comment_likes);
            likeButton = itemView.findViewById(R.id.imb_comment_likes);
            deleteText = itemView.findViewById(R.id.tv_comment_delete); // Изменили ID
        }

        public void bind(CommentModel comment) {
            // Load user avatar
            if (comment.getImageUrls() != null && !comment.getImageUrls().isEmpty()) {
                Glide.with(context)
                        .load(comment.getImageUrls().get(0)) // Assuming first image is the avatar
                        .placeholder(R.drawable.default_profile_picture)
                        .error(R.drawable.default_profile_picture)
                        .into(userAvatar);
            } else {
                userAvatar.setImageResource(R.drawable.default_profile_picture);
            }

            // Set username
            usernameText.setText(comment.getAuthorName());

            // Set comment text
            commentText.setText(comment.getContent());

            // Set formatted time
            timeText.setText(formatTimestamp(comment.getCreatedAtLong()));

            // Show delete option only for current user's comments
            if (currentUser != null && currentUser.getUid().equals(comment.getAuthorId())) {
                deleteText.setVisibility(View.VISIBLE);
            } else {
                deleteText.setVisibility(View.GONE);
            }

            // Handle likes
            updateLikeUI(comment);

            // Set click listeners
            likeButton.setOnClickListener(v -> handleLikeClick(comment));
            deleteText.setOnClickListener(v -> {
                if (currentUser != null && currentUser.getUid().equals(comment.getAuthorId())) {
                    listener.onDeleteComment(comment);
                }
            });
        }

        private void updateLikeUI(CommentModel comment) {
            Map<String, Boolean> likes = comment.getLikes();
            int likesCount = likes != null ? likes.size() : 0;

            likesCountText.setText(String.valueOf(likesCount));

            if (currentUser != null && likes != null && likes.containsKey(currentUser.getUid())) {
                likeButton.setImageResource(R.drawable.like_filled);
                likeButton.setColorFilter(context.getResources().getColor(R.color.text));
            } else {
                likeButton.setImageResource(R.drawable.ic_dislike); // Изменили на ic_dislike из XML
                likeButton.setColorFilter(context.getResources().getColor(R.color.text));
            }
        }

        private void handleLikeClick(CommentModel comment) {
            if (currentUser == null) {
                Toast.makeText(context, "Please sign in to like comments", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isLiked = comment.getLikes() != null &&
                    comment.getLikes().containsKey(currentUser.getUid());
            listener.onLikeComment(comment.getCommentId(), !isLiked);
        }

        private String formatTimestamp(long timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }
}