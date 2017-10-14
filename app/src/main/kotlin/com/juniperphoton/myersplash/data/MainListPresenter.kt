package com.juniperphoton.myersplash.data

import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.cloudservice.CloudService
import com.juniperphoton.myersplash.event.ScrollToTopEvent
import com.juniperphoton.myersplash.model.UnsplashCategory
import com.juniperphoton.myersplash.model.UnsplashImage
import com.juniperphoton.myersplash.utils.Pasteur
import com.juniperphoton.myersplash.utils.ResponseObserver
import javax.inject.Inject

open class MainListPresenter : MainContract.MainPresenter {
    companion object {
        const val REFRESH_PAGING = 1
        private const val TAG = "MainListPresenter"
    }

    private var next: Int = REFRESH_PAGING
    private var refreshing: Boolean = false

    @Inject
    override lateinit var category: UnsplashCategory
    @Inject
    lateinit var mainView: MainContract.MainView
    @Inject
    lateinit var preferenceRepo: PreferenceRepo

    override var query: String? = null

    override fun stop() = Unit

    override fun start() = Unit

    override fun search(query: String) {
        Pasteur.d(TAG, "on search:$query")
        if (!mainView.isBusyRefreshing) {
            this.query = query
            refresh()
        }
    }

    override fun onReceivedScrollToTopEvent(event: ScrollToTopEvent) {
        if (event.id == category.id) {
            mainView.scrollToTop()
            if (event.refresh) {
                refresh()
            }
        }
    }

    override fun loadMore() {
        loadPhotoList(++next)
    }

    override fun reloadList() {
        loadPhotoList(next)
    }

    override fun refresh() {
        loadPhotoList(REFRESH_PAGING)
    }

    private fun setSignalOfEnd() {
        refreshing = false
        mainView.setRefreshing(false)
    }

    private fun insertRecommendedImage(t: MutableList<UnsplashImage>) {
        t.add(0, UnsplashImage.createRecommendedImage())
    }

    private fun loadPhotoList(next: Int) {
        this.next = next
        refreshing = true
        if (next == REFRESH_PAGING) {
            mainView.setRefreshing(true)
        }
        val observer = object : ResponseObserver<MutableList<UnsplashImage>>() {
            override fun onFinish() {
                setSignalOfEnd()
            }

            override fun onError(e: Throwable) {
                super.onError(e)
                e.printStackTrace()
                mainView.updateNoItemVisibility()
                mainView.setRefreshing(false)
            }

            override fun onNext(t: MutableList<UnsplashImage>) {
                if (category.id == UnsplashCategory.NEW_CATEGORY_ID
                        && next == REFRESH_PAGING
                        && preferenceRepo.getBoolean(App.instance.getString(R.string.preference_key_recommendation), true)) {
                    insertRecommendedImage(t)
                }
                mainView.refreshList(t, next)
            }
        }

        category.let {
            when (it.id) {
                UnsplashCategory.FEATURED_CATEGORY_ID ->
                    CloudService.getFeaturedPhotos(it.requestUrl!!, next, observer)
                UnsplashCategory.NEW_CATEGORY_ID ->
                    CloudService.getPhotos(it.requestUrl!!, next, observer)
                UnsplashCategory.RANDOM_CATEGORY_ID ->
                    CloudService.getRandomPhotos(it.requestUrl!!, observer)
                UnsplashCategory.SEARCH_ID ->
                    CloudService.searchPhotos(it.requestUrl!!, next, query!!, observer)
            }
        }
    }
}