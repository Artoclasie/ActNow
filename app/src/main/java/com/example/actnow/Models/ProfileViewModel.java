package com.example.actnow.Models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.actnow.Models.PostReferenceModel;
import com.example.actnow.Models.UserProfile;

import java.util.List;

public class ProfileViewModel extends ViewModel {
    private final MutableLiveData<UserProfile> userProfile = new MutableLiveData<>();
    private final MutableLiveData<List<PostReferenceModel>> posts = new MutableLiveData<>();

    public void setUserProfile(UserProfile profile) {
        userProfile.setValue(profile);
    }

    public LiveData<UserProfile> getUserProfile() {
        return userProfile;
    }

    public void setPosts(List<PostReferenceModel> postList) {
        posts.setValue(postList);
    }

    public LiveData<List<PostReferenceModel>> getPosts() {
        return posts;
    }
}