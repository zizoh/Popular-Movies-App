package com.zizohanto.popularmovies.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;

import com.zizohanto.popularmovies.AppExecutors;
import com.zizohanto.popularmovies.data.database.movie.Movie;
import com.zizohanto.popularmovies.data.database.movie.MovieDao;
import com.zizohanto.popularmovies.data.database.video.Video;
import com.zizohanto.popularmovies.data.database.video.VideoDao;
import com.zizohanto.popularmovies.data.network.MovieNetworkDataSource;
import com.zizohanto.popularmovies.utils.NetworkState;

import java.util.List;

import timber.log.Timber;

/**
 * Handles data operations in Popular movies. Acts as a mediator between {@link MovieNetworkDataSource}
 * and {@link MovieDao}
 */
public class PopularMoviesRepository {

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static PopularMoviesRepository sInstance;
    private final MovieDao mMovieDao;
    private final VideoDao mVideoDao;
    private final MovieNetworkDataSource mMovieNetworkDataSource;
    private final AppExecutors mExecutors;

    private String mMoviesSortType;
    private int mPageToLoad = 0;
    private boolean mInitialized = false;
    private boolean mIsNotPreferenceChange;

    private PopularMoviesRepository(MovieDao movieDao,
                                    VideoDao videoDao,
                                    MovieNetworkDataSource movieNetworkDataSource,
                                    AppExecutors executors) {
        mMovieDao = movieDao;
        mVideoDao = videoDao;
        mMovieNetworkDataSource = movieNetworkDataSource;
        mExecutors = executors;

        // As long as the repository exists, observe the network LiveData.
        // If that LiveData changes, update the database.
        LiveData<List<Movie>> networkData = mMovieNetworkDataSource.getMoviesData();

        networkData.observeForever(new Observer<List<Movie>>() {
            @Override
            public void onChanged(@Nullable List<Movie> newMoviesFromNetwork) {
                mExecutors.diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (mPageToLoad <= 1) {
                            // Deletes old historical data
                            PopularMoviesRepository.this.deleteOldData();
                            Timber.d("Old movies deleted");
                        }
                        mVideoDao.deleteAllVideos();
                        // Insert our new movie data into PopularMovie's database
                        mMovieDao.bulkInsert(newMoviesFromNetwork);
                        Timber.d("New values inserted");
                    }
                });
            }
        });

        LiveData<List<Video>> networkVideoData = mMovieNetworkDataSource.getVideos();

        networkVideoData.observeForever(new Observer<List<Video>>() {
            @Override
            public void onChanged(@Nullable List<Video> videos) {
                mExecutors.diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        mVideoDao.deleteAllVideos();
                        Timber.d("Old videos deleted");
                        // Insert our new movie data into PopularMovie's database
                        mVideoDao.bulkInsert(videos);
                        Timber.e("New video values inserted");
                    }
                });
            }
        });
    }

    public synchronized static PopularMoviesRepository getInstance(
            MovieDao movieDao, VideoDao videoDao, MovieNetworkDataSource movieNetworkDataSource,
            AppExecutors executors) {
        Timber.d("Getting the repository");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new PopularMoviesRepository(movieDao, videoDao, movieNetworkDataSource,
                        executors);
                Timber.d("Made new repository");
            }
        }
        return sInstance;
    }

    /**
     * Deletes old movies data
     */
    private void deleteOldData() {
        mMovieDao.deleteAllMovies();
    }

    /**
     * Starts service that fetches data
     */
    private synchronized void initializeData() {
        // Only perform initialization once per app lifetime. If initialization has already been
        // performed, nothing is done in this method.
        if (mInitialized && mIsNotPreferenceChange) {
            return;
        }
        mInitialized = true;
        mMovieNetworkDataSource.setFetchCriteria(mMoviesSortType, mPageToLoad);

        startFetchMoviesService();
    }

    /**
     * Network related operation
     */
    private void startFetchMoviesService() {
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mMovieNetworkDataSource.startFetchMoviesService();
            }
        });
    }

    private void startFetchVideosService(Integer id) {
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mMovieNetworkDataSource.startFetchVideosService(id);
            }
        });
    }

    public LiveData<NetworkState> getNetworkState() {
        return mMovieNetworkDataSource.getNetworkState();
    }

    /**
     * Database related operations
     */
    public LiveData<List<Movie>> getCurrentMovies() {
        Timber.d("Getting current movies: ");
        initializeData();
        return mMovieDao.getAllMovies();
    }

    public LiveData<Movie> getMovieByTitle(String title) {
        initializeData();
        return mMovieDao.getMovieByTitle(title);
    }

    public LiveData<List<Video>> getVideosOfMovieId(Integer id) {
        startFetchVideosService(id);
        return mVideoDao.getAllVideos();
    }

    public void setFetchCriteria(String moviesSortType, Boolean isNotPreferenceChange, int pageToLoad) {
        mMoviesSortType = moviesSortType;
        mIsNotPreferenceChange = isNotPreferenceChange;
        mPageToLoad = pageToLoad;
    }

}
