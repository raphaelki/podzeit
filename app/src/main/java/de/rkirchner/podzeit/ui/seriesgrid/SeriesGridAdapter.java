package de.rkirchner.podzeit.ui.seriesgrid;

import android.support.annotation.NonNull;

import de.rkirchner.podzeit.R;
import de.rkirchner.podzeit.data.models.Series;
import de.rkirchner.podzeit.databinding.SeriesGridItemBinding;
import de.rkirchner.podzeit.ui.common.BindingViewHolder;
import de.rkirchner.podzeit.ui.common.GlideRequestListener;
import de.rkirchner.podzeit.ui.common.RecyclerViewListAdapter;

public class SeriesGridAdapter extends RecyclerViewListAdapter<SeriesGridItemBinding, Series> {

    private SeriesGridClickCallback callback;
    private GlideRequestListener glideRequestListener;

    public SeriesGridAdapter(SeriesGridClickCallback callback, GlideRequestListener glideRequestListener) {
        this.callback = callback;
        this.glideRequestListener = glideRequestListener;
    }

    @Override
    public int getLayoutId() {
        return R.layout.series_grid_item;
    }

    @Override
    public void onBindViewHolder(@NonNull BindingViewHolder<SeriesGridItemBinding> holder, int position) {
        holder.binding().setSeries(getList().get(position));
        holder.binding().setCallback(callback);
        holder.binding().setRequestListener(glideRequestListener);
        holder.binding().executePendingBindings();
    }
}
