package com.example.actnow;

import androidx.recyclerview.widget.DiffUtil;

import com.example.actnow.Models.PostReferenceModel;

import java.util.List;

public class PostDiffCallback extends DiffUtil.Callback {
    private final List<PostReferenceModel> oldList;
    private final List<PostReferenceModel> newList;

    public PostDiffCallback(List<PostReferenceModel> oldList, List<PostReferenceModel> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getId().equals(newList.get(newItemPosition).getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}
