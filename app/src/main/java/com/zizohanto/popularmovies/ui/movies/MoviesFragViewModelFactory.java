package com.zizohanto.popularmovies.ui.movies;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.zizohanto.popularmovies.data.PopularMoviesRepository;

/**
 * Factory method that to create a ViewModel with a constructor that takes a
 * {@link PopularMoviesRepository}
 */
public class MoviesFragViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final PopularMoviesRepository mRepository;
    private String mMoviesSortType;
    private Boolean mIsNotFirstPreferenceChange;
    private int mPageToLoad;

    public MoviesFragViewModelFactory(PopularMoviesRepository repository,
                                      String moviesSortType, Boolean isNotFirstPreferenceChange,
                                      int pageToLoad) {
        mRepository = repository;
        mMoviesSortType = moviesSortType;
        mIsNotFirstPreferenceChange = isNotFirstPreferenceChange;
        mPageToLoad = pageToLoad;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new MoviesFragViewModel(mRepository, mMoviesSortType, mIsNotFirstPreferenceChange,
                mPageToLoad);
    }
}
