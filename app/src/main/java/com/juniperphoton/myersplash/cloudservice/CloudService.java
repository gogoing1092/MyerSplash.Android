package com.juniperphoton.myersplash.cloudservice;

import com.juniperphoton.myersplash.model.SearchResult;
import com.juniperphoton.myersplash.model.UnsplashCategory;
import com.juniperphoton.myersplash.model.UnsplashImage;
import com.juniperphoton.myersplash.model.UnsplashImageFeatured;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class CloudService {
    private static String AppKey = "403d9934ce4bb8dbef44765692144e8c6fac6d2698950cb40b07397d6c6635fe";

    public static String baseUrl = "https://api.unsplash.com/";
    public static String photoUrl = "https://api.unsplash.com/photos?";
    public static String featuredPhotosUrl = "https://api.unsplash.com/collections/featured?";

    private static final int DEFAULT_TIMEOUT = 10;

    private Retrofit retrofit;
    private CategoryService categoryService;
    private PhotoService photoService;
    private OkHttpClient.Builder builder;

    private CloudService() {
        builder = new OkHttpClient.Builder();
        builder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

        retrofit = new Retrofit.Builder()
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(baseUrl)
                .build();

        categoryService = retrofit.create(CategoryService.class);
        photoService = retrofit.create(PhotoService.class);
    }

    private static class SingletonHolder {
        private static final CloudService INSTANCE = new CloudService();
    }

    public static CloudService getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void getCategories(Subscriber<List<UnsplashCategory>> subscriber) {
        Observable<List<UnsplashCategory>> observable = categoryService.getCategories(AppKey);
        observable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public void getPhotos(Subscriber<List<UnsplashImage>> subscriber, String url, int page) {
        Observable<List<UnsplashImage>> observable = photoService.getPhotos(url, page, 10, AppKey);
        subsribe(observable, subscriber);
    }

    public void getFeaturedPhotos(Subscriber<List<UnsplashImage>> subscriber, String url, int page) {
        Observable<List<UnsplashImageFeatured>> observableF = photoService.getFeaturedPhotos(url, page, 10, AppKey);
        Observable<List<UnsplashImage>> observable = observableF.map(new Func1<List<UnsplashImageFeatured>, List<UnsplashImage>>() {
            @Override
            public List<UnsplashImage> call(List<UnsplashImageFeatured> images) {
                ArrayList<UnsplashImage> contentImages = new ArrayList<>();
                for (UnsplashImageFeatured img : images) {
                    contentImages.add(img.getImage());
                }
                return contentImages;
            }
        });
        subsribe(observable, subscriber);
    }

    public void searchPhotos(Subscriber<List<UnsplashImage>> subscriber, String url, int page, String query) {
        Observable<SearchResult> observableF = photoService.searchPhotosByQuery(url, page, 10, query, AppKey);
        Observable<List<UnsplashImage>> observable = observableF.map(new Func1<SearchResult, List<UnsplashImage>>() {
            @Override
            public List<UnsplashImage> call(SearchResult searchResults) {
                return searchResults.getList();
            }
        });
        subsribe(observable, subscriber);
    }

    private void subsribe(Observable<List<UnsplashImage>> observable, Subscriber<List<UnsplashImage>> subscriber) {
        observable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public void downloadPhoto(Subscriber<ResponseBody> subscriber, String url) {
        Retrofit retrofit = new Retrofit.Builder()
                .client(builder.build())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(baseUrl)
                .build();

        DownloadService downloadService = retrofit.create(DownloadService.class);
        Observable<ResponseBody> observable = downloadService.downloadFileWithDynamicUrlSync(url);
        observable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(subscriber);
    }

}
