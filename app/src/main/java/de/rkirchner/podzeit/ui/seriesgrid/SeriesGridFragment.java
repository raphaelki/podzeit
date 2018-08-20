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

    public SeriesGridFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_series_grid, container, false);
        gridAdapter = new SeriesGridAdapter(seriesGridClickCallback);
        binding.seriesGridRv.setAdapter(gridAdapter);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SeriesGridViewModel.class);
        viewModel.getSeriesList().observe(this, seriesList -> gridAdapter.swapList(seriesList));
    }
}
