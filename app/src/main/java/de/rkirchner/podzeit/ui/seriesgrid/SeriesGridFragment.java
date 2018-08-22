package de.rkirchner.podzeit.ui.seriesgrid;


import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;
import de.rkirchner.podzeit.R;
import de.rkirchner.podzeit.databinding.FragmentSeriesGridBinding;
import de.rkirchner.podzeit.ui.common.GlideRequestListener;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class SeriesGridFragment extends DaggerFragment {

    @Inject
    SeriesGridClickCallback seriesGridClickCallback;
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    private SeriesGridViewModel viewModel;
    private FragmentSeriesGridBinding binding;
    private SeriesGridAdapter gridAdapter;
    private GlideRequestListener glideRequestListener = () -> startPostponedEnterTransition();

    public SeriesGridFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate");
        gridAdapter = new SeriesGridAdapter(seriesGridClickCallback, glideRequestListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        postponeEnterTransition();
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_series_grid, container, false);
        binding.seriesGridRv.setAdapter(gridAdapter);
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.d("onDestroy");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SeriesGridViewModel.class);
        viewModel.getSeriesList().observe(this, seriesList -> gridAdapter.swapList(seriesList));
    }
}
