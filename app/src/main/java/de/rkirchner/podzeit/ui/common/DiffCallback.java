package de.rkirchner.podzeit.ui.common;

import android.support.v7.util.DiffUtil;

import java.util.List;

public class DiffCallback<T> extends DiffUtil.Callback {

    private List<T> newList;
    private List<T> oldList;

    public DiffCallback(List<T> newList, List<T> oldList) {
        this.newList = newList;
        this.oldList = oldList;
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
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}
