package com.example.actnow.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class PostReferenceModel implements Parcelable {
    private String id;
    private String uid; // UID автора
    private String title;
    private String location;
    private String timestamp; // Время создания поста
    private String date; // Дата проведения (новое поле)
    private String content;
    private String imageUrl;
    private String username;
    private String profileImageUrl;
    private String city;
    private ArrayList<String> likes; // Существующее поле для лайков
    private int likesCount;
    private ArrayList<String> volunteers; // Поле для волонтёров

    // Конструктор по умолчанию
    public PostReferenceModel() {
        this.likes = new ArrayList<>();
        this.volunteers = new ArrayList<>(); // Инициализация пустым списком
    }

    // Конструктор с параметрами
    public PostReferenceModel(String id, String uid, String title, String location, String timestamp,
                              String date, String content, String imageUrl, String username,
                              String profileImageUrl, String city, ArrayList<String> likes,
                              int likesCount, ArrayList<String> volunteers) {
        this.id = id;
        this.uid = uid;
        this.title = title;
        this.location = location;
        this.timestamp = timestamp;
        this.date = date; // Новое поле
        this.content = content;
        this.imageUrl = imageUrl;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.city = city;
        this.likes = likes != null ? likes : new ArrayList<>();
        this.likesCount = likesCount;
        this.volunteers = volunteers != null ? volunteers : new ArrayList<>();
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getDate() { return date; } // Новый геттер
    public void setDate(String date) { this.date = date; } // Новый сеттер

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

    public ArrayList<String> getLikes() { return likes; }
    public void setLikes(ArrayList<String> likes) { this.likes = likes; }

    public int getLikesCount() { return likesCount; }
    public void setLikesCount(int likesCount) { this.likesCount = likesCount; }

    public ArrayList<String> getVolunteers() { return volunteers; }
    public void setVolunteers(ArrayList<String> volunteers) { this.volunteers = volunteers; }

    // Реализация Parcelable
    protected PostReferenceModel(Parcel in) {
        id = in.readString();
        uid = in.readString();
        title = in.readString();
        location = in.readString();
        timestamp = in.readString();
        date = in.readString(); // Чтение нового поля date
        content = in.readString();
        imageUrl = in.readString();
        username = in.readString();
        profileImageUrl = in.readString();
        city = in.readString();
        likes = in.createStringArrayList();
        likesCount = in.readInt();
        volunteers = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(uid);
        dest.writeString(title);
        dest.writeString(location);
        dest.writeString(timestamp);
        dest.writeString(date); // Запись нового поля date
        dest.writeString(content);
        dest.writeString(imageUrl);
        dest.writeString(username);
        dest.writeString(profileImageUrl);
        dest.writeString(city);
        dest.writeStringList(likes);
        dest.writeInt(likesCount);
        dest.writeStringList(volunteers);
    }

    @Override
    public int describeContents() {
        return 0;
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
}