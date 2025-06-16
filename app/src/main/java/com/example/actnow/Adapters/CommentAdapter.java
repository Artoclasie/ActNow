package com.example.actnow.Adapters;

import android.content.Context;
import android.graphics.drawable.TransitionDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
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

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final Context context;
    private final List<CommentModel> commentList;
    private final FirebaseUser currentUser;
    private final OnCommentInteractionListener listener;

    public interface OnCommentInteractionListener {
        void onLikeComment(String commentId, boolean isLiked, CommentModel comment, CommentViewHolder holder);
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.postcomment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentModel comment = commentList.get(position);
        Log.d("CommentAdapter", "Binding comment at position " + position + ": content=" + comment.getContent() + ", authorName=" + comment.getAuthorName());
        holder.bind(comment);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            CommentModel comment = commentList.get(position);
            holder.updateLikeUI(comment);
        } else {
            onBindViewHolder(holder, position);
        }
    }

    @Override
    public int getItemCount() {
        Log.d("CommentAdapter", "Item count: " + commentList.size());
        return commentList.size();
    }

    public void updateComments(List<CommentModel> newComments) {
        commentList.clear();
        commentList.addAll(newComments);
        Log.d("CommentAdapter", "Updated with " + commentList.size() + " comments");
        notifyDataSetChanged();
    }

    public void showLoading(boolean show) {
        // Логика индикатора загрузки, если нужна
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        private final CardView userAvatarCard;
        private final ImageView userAvatar;
        private final TextView usernameText;
        private final TextView commentText;
        private final TextView timeText;
        public final TextView likesCountText;
        public final ImageButton likeButton;
        private final TextView deleteText;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            userAvatarCard = itemView.findViewById(R.id.cv_comment_uid);
            userAvatar = itemView.findViewById(R.id.imv_comment_uid);
            usernameText = itemView.findViewById(R.id.tv_comment_uname);
            commentText = itemView.findViewById(R.id.et_post_comment);
            timeText = itemView.findViewById(R.id.tv_comment_date);
            likesCountText = itemView.findViewById(R.id.tv_comment_likes);
            likeButton = itemView.findViewById(R.id.imb_comment_likes);
            deleteText = itemView.findViewById(R.id.tv_comment_delete);
        }

        public void bind(CommentModel comment) {
            Log.d("CommentViewHolder", "Binding comment: " + comment.getContent());
            if (comment.getProfileImage() != null && !comment.getProfileImage().isEmpty()) {
                Glide.with(context)
                        .load(comment.getProfileImage())
                        .placeholder(R.drawable.default_profile_picture)
                        .error(R.drawable.default_profile_picture)
                        .into(userAvatar);
            } else {
                userAvatar.setImageResource(R.drawable.default_profile_picture);
            }

            usernameText.setText(comment.getAuthorName() != null ? comment.getAuthorName() : "Аноним");
            commentText.setText(comment.getContent() != null ? comment.getContent() : "Нет текста");
            timeText.setText(formatTimestamp(comment.getCreatedAtLong()));

            if (currentUser != null && currentUser.getUid().equals(comment.getAuthorId())) {
                deleteText.setVisibility(View.VISIBLE);
            } else {
                deleteText.setVisibility(View.GONE);
            }

            updateLikeUI(comment);

            likeButton.setOnClickListener(v -> handleLikeClick(comment, this));
            deleteText.setOnClickListener(v -> {
                if (currentUser != null && currentUser.getUid().equals(comment.getAuthorId())) {
                    listener.onDeleteComment(comment);
                }
            });
        }

        public void updateLikeUI(CommentModel comment) {
            int likesCount = comment.getLikesCount();
            likesCountText.setText(String.valueOf(likesCount));

            boolean isLiked = currentUser != null && comment.isLikedBy(currentUser.getUid());
            TransitionDrawable transition = (TransitionDrawable) ContextCompat.getDrawable(context, isLiked ? R.drawable.transition_like : R.drawable.transition_dislike);
            if (transition != null) {
                likeButton.setImageDrawable(transition);
                transition.startTransition(200);
            } else {
                likeButton.setImageResource(isLiked ? R.drawable.ic_like : R.drawable.ic_dislike);
            }
            likeButton.setColorFilter(null);
        }

        private void handleLikeClick(CommentModel comment, CommentViewHolder holder) {
            if (currentUser == null) {
                Toast.makeText(context, "Войдите, чтобы ставить лайки", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentUserId = currentUser.getUid();
            int newLikesCount = comment.getLikesCount();
            boolean isLiked = comment.isLikedBy(currentUserId);
            boolean newLikeState;

            if (isLiked) {
                comment.removeLike(currentUserId);
                newLikesCount--;
                newLikeState = false;
            } else {
                comment.addLike(currentUserId);
                newLikesCount++;
                newLikeState = true;
            }

            final int finalLikesCount = Math.max(0, newLikesCount);

            holder.likesCountText.setText(String.valueOf(finalLikesCount));
            TransitionDrawable transition = (TransitionDrawable) ContextCompat.getDrawable(context, newLikeState ? R.drawable.transition_like : R.drawable.transition_dislike);
            if (transition != null) {
                holder.likeButton.setImageDrawable(transition);
                transition.startTransition(200);
            } else {
                holder.likeButton.setImageResource(newLikeState ? R.drawable.ic_like : R.drawable.ic_dislike);
            }

            notifyItemChanged(getAdapterPosition(), "like_update");
            listener.onLikeComment(comment.getCommentId(), newLikeState, comment, holder);
        }

        private String formatTimestamp(long timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }
}