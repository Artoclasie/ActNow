package com.example.actnow.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostReferenceModel implements Parcelable {
    private String id;
    private String authorId;
    private String content;
    private String imageUrl;
    private String username;
    private String profileImageUrl;
    private String city;
    private List<String> likes;
    private int likesCount;
    private Timestamp createdAt;
    private String date;

    public PostReferenceModel() {
        this.likes = new ArrayList<>();
        this.likesCount = 0;
        this.date = "";
    }

    protected PostReferenceModel(Parcel in) {
        id = in.readString();
        authorId = in.readString();
        content = in.readString();
        imageUrl = in.readString();
        username = in.readString();
        profileImageUrl = in.readString();
        city = in.readString();
        likes = in.createStringArrayList();
        likesCount = in.readInt();
        createdAt = in.readParcelable(Timestamp.class.getClassLoader());
        date = in.readString();
    }

    public static final Creator<PostReferenceModel> CREATOR = new Creator<PostReferenceModel>() {
        @Override
        public PostReferenceModel createFromParcel(Parcel in) {
            return new PostReferenceModel(in);
        }

        @Override
        public PostReferenceModel[] newArray(int size) {
            return new PostReferenceModel[size];
        }
    };

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("authorId", authorId);
        map.put("content", content);
        map.put("imageUrl", imageUrl);
        map.put("likes", likes);
        map.put("likesCount", likesCount);
        map.put("createdAt", createdAt);
        map.put("date", date);
        return map;
    }

    // Геттеры и сеттеры
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public List<String> getLikes() { return likes; }
    public void setLikes(List<String> likes) { this.likes = likes; }
    public int getLikesCount() { return likesCount; }
    public void setLikesCount(int likesCount) { this.likesCount = likesCount; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(authorId);
        dest.writeString(content);
        dest.writeString(imageUrl);
        dest.writeString(username);
        dest.writeString(profileImageUrl);
        dest.writeString(city);
        dest.writeStringList(likes);
        dest.writeInt(likesCount);
        dest.writeParcelable(createdAt, flags);
        dest.writeString(date);
    }
}