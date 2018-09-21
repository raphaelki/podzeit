package de.rkirchner.podzeit.ui.episodelist;


import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;
import de.rkirchner.podzeit.Constants;
import de.rkirchner.podzeit.R;
import de.rkirchner.podzeit.databinding.FragmentEpisodeListBinding;
import de.rkirchner.podzeit.ui.common.FormatterUtil;
import de.rkirchner.podzeit.ui.common.GlideRequestListener;
import de.rkirchner.podzeit.ui.logindialog.LoginActivity;

public class EpisodeListFragment extends DaggerFragment {

    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    FormatterUtil formatterUtil;
    @Inject
    EpisodeListClickCallback callback;
    @Inject
    EpisodeListAdapter adapter;
    private FragmentEpisodeListBinding binding;
    private EpisodeListViewModel viewModel;
    private String seriesRssUrl;
    private GlideRequestListener glideRequestListener = this::startPostponedEnterTransition;
    private boolean seriesNeedsCredentials = false;
    private PlaylistListener playlistListener = new PlaylistListener() {
        @Override
        public void onAddToPlaylist(int episodeId) {
            viewModel.addEpisodeToPlaylist(episodeId);
        }

        @Override
        public void onRemoveFromPlaylist(int episodeId) {
            viewModel.removeEpisodeFromPlaylist(episodeId);
        }

        @Override
        public void onPlayNow(int episodeId) {
            viewModel.playNow(episodeId);
        }
    };

    public EpisodeListFragment() {
        // Required empty public constructor
    }

    public static EpisodeListFragment create(String rssUrl) {
        EpisodeListFragment episodeListFragment = new EpisodeListFragment();
        Bundle arguments = new Bundle();
        arguments.putString(Constants.RSS_URL_KEY, rssUrl);
        episodeListFragment.setArguments(arguments);
        return episodeListFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter.setPlaylistListener(playlistListener);
        adapter.setHasStableIds(true);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        postponeEnterTransition();
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_episode_list, container, false);
        binding.setRequestListener(glideRequestListener);
        binding.episodeListRv.setAdapter(adapter);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(EpisodeListViewModel.class);
        if (getArguments() != null && getArguments().containsKey(Constants.RSS_URL_KEY)) {
            seriesRssUrl = getArguments().getString(Constants.RSS_URL_KEY);
            viewModel.setSeries(seriesRssUrl);
        }
        binding.episodeListSwipeToRefresh.setOnRefreshListener(() -> viewModel.triggerRefresh());
        viewModel.getRefreshStatus().observe(this, status -> {
            if (status != null) {
                switch (status) {
                    case REFRESHING:
                        binding.episodeListSwipeToRefresh.setRefreshing(true);
                        break;
                    default:
                        binding.episodeListSwipeToRefresh.setRefreshing(false);
                        break;
                }
            }
        });
        viewModel.getSeries().observe(this, series -> {
            if (series != null) {
                binding.setSeries(series);
                seriesNeedsCredentials = series.getNeedsCredentials();
            }
        });
        viewModel.getEpisodes().observe(this, adapter::swapList);
        setupToolbar();
    }

    private AppCompatActivity getCompatActivity() {
        return ((AppCompatActivity) getActivity());
    }

    private void setupToolbar() {
        getCompatActivity().setSupportActionBar(binding.episodeListToolbar);
        ActionBar actionBar = getCompatActivity().getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (seriesNeedsCredentials) inflater.inflate(R.menu.episode_list_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
            case R.id.episode_details_menu_singin:
                String authority = Uri.parse(seriesRssUrl).getAuthority();
                Intent intent = new Intent(getContext(), LoginActivity.class);
                intent.putExtra(Constants.URI_AUTHORITY_KEY, authority);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
