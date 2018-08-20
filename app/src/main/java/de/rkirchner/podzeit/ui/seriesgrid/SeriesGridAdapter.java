package de.rkirchner.podzeit.ui.seriesgrid;

import android.support.annotation.NonNull;

import de.rkirchner.podzeit.R;
import de.rkirchner.podzeit.data.models.Series;
import de.rkirchner.podzeit.databinding.SeriesGridItemBinding;
import de.rkirchner.podzeit.ui.common.BindingViewHolder;
import de.rkirchner.podzeit.ui.common.RecyclerViewListAdapter;

public class SeriesGridAdapter extends RecyclerViewListAdapter<SeriesGridItemBinding, Series> {

    private SeriesGridClickCallback callback;

    public SeriesGridAdapter(SeriesGridClickCallback callback) {
        this.callback = callback;
    }

    @Override
    public int getLayoutId() {
        return R.layout.series_grid_item;
    }

    @Override
    public void onBindViewHolder(@NonNull BindingViewHolder<SeriesGridItemBinding> holder, int position) {
        holder.binding().setSeries(getList().get(position));
        holder.binding().setCallback(callback);
        holder.binding().executePendingBindings();
    }
}
