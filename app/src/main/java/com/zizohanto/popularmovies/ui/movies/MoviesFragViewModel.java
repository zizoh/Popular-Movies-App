package com.zizohanto.popularmovies.ui.movies;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.zizohanto.popularmovies.data.PopularMoviesRepository;
import com.zizohanto.popularmovies.data.database.Movie;
import com.zizohanto.popularmovies.data.network.MovieNetworkDataSource;

import java.util.List;

public class MoviesFragViewModel extends ViewModel {
    private final PopularMoviesRepository mRepository;
    private LiveData<List<Movie>> mMovies;
    private String mMoviesSortType;
    private Boolean mIsNotPreferenceChange;
    private int mPageToLoad;
    private MovieNetworkDataSource.OnResponseListener mOnResponseListener;

    MoviesFragViewModel(PopularMoviesRepository repository, String moviesSortType,
                        Boolean isNotPreferenceChange, int pageToLoad,
                        MovieNetworkDataSource.OnResponseListener onResponseListener) {
        mRepository = repository;
        mMoviesSortType = moviesSortType;
        mIsNotPreferenceChange = isNotPreferenceChange;
        mPageToLoad = pageToLoad;
        mOnResponseListener = onResponseListener;
        mRepository.setFetchCriteria(mMoviesSortType, mIsNotPreferenceChange, mPageToLoad);
        mMovies = mRepository.getCurrentMovies();
    }

    public LiveData<List<Movie>> getMovies() {
        return mMovies;
    }

    public void getCurrentMovies(String moviesSortType, Boolean isNotPreferenceChange, int pageToLoad) {
        mRepository.setFetchCriteria(moviesSortType, isNotPreferenceChange, pageToLoad);
        mMovies = mRepository.getCurrentMovies();
    }
}
