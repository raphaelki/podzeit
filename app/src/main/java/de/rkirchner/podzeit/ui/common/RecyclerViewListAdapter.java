package de.rkirchner.podzeit.ui.common;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

public abstract class RecyclerViewListAdapter<T extends ViewDataBinding, U> extends RecyclerView.Adapter<BindingViewHolder<T>> {

    private List<U> list;

    public abstract @LayoutRes
    int getLayoutId();

    @NonNull
    @Override
    public BindingViewHolder<T> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        T binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                getLayoutId(), parent, false);
        return new BindingViewHolder<>(binding);
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public void swapList(List<U> newList) {
        if (list == null) {
            list = newList;
            notifyDataSetChanged();
        } else {
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallback<>(newList, list));
            list = newList;
            diffResult.dispatchUpdatesTo(this);
        }
    }

    public List<U> getList() {
        return list;
    }
}
