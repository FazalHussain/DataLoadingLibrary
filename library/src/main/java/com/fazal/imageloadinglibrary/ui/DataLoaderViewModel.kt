package com.fazal.imageloadinglibrary.ui

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.util.LruCache
import android.widget.ImageView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fazal.imageloadinglibrary.models.ImageRequest
import com.fazal.imageloadinglibrary.repo.DataLoadingRepo
import com.fazal.imageloadinglibrary.utils.XMLParser
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.util.*
import java.util.Collections.synchronizedMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.HashMap

/**
 * Data Loader View Model Responsible to Provide loading image, loading json array and json object
 */
class DataLoaderViewModel : ViewModel() {

    val dataLoadingRepo = DataLoadingRepo()

    val scaledBitmapLiveData : MutableLiveData<Bitmap> = MutableLiveData()

    val jsonObjectLiveData : MutableLiveData<JSONObject> = MutableLiveData()

    val jsonArrayLiveData : MutableLiveData<JSONArray> = MutableLiveData()


    val xmlNodeListLiveData : MutableLiveData<HashMap<String, Any>> = MutableLiveData()

    val xmlLiveData :MutableLiveData<Any> = MutableLiveData()

    private val maxCacheSize: Int = (Runtime.getRuntime().maxMemory() / 1024).toInt()/8
    private val memoryCache: LruCache<String, Bitmap>

    private val executorService: ExecutorService

    private val imageViewMap = synchronizedMap(WeakHashMap<ImageView, String>())
    private val handler: Handler

    init {
        memoryCache = object : LruCache<String, Bitmap>(maxCacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                // The cache size will be measured in kilobytes rather than number of items.
                return bitmap.byteCount / 1024
            }
        }

        executorService = Executors.newFixedThreadPool(5,
            dataLoadingRepo.ImageThreadFactory())
        handler = Handler()



    }

    /**
     * Setup Matrix
     */
    fun setupMatrix(application: Context) {

        val metrics = application.resources.displayMetrics
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
    }

    /**
     * Load image from the image url
     *
     * @param imageView The ImageView
     * @param imageUrl The url of an image
     */
    fun load(imageView: ImageView, imageUrl: String) {

        require(imageView != null) {
            "ImageLoader:load - ImageView should not be null."
        }

        require(imageUrl != null && imageUrl.isNotEmpty()) {
            "ImageLoader:load - Image Url should not be empty"
        }

        imageView.setImageResource(0)
        imageViewMap[imageView] = imageUrl

        val bitmap = checkImageInCache(imageUrl)
        bitmap?.let {
            loadImageIntoImageView(imageView, it, imageUrl)
        } ?: run {
            executorService.submit(PhotosLoader(ImageRequest(imageUrl, imageView)))
        }
    }

    /**
     * Load the JSON object
     *
     * @param url The url of the json
     * @param method The method of the api either POST or GET
     * @param basicAuthentication The authentication of the url (OPTIONAL PARAM)
     * @param param The param required for the URL. (OPTIONAL PARAM)
     */
    fun loadJSONObject(
        url: String, method: Int, basicAuthentication: String?,
        param: ContentValues?) {
        Thread(Runnable {
            if (param != null) {
                jsonObjectLiveData.postValue(dataLoadingRepo.getJsonObject(url, param,
                    method, basicAuthentication))
            } else {
                jsonObjectLiveData.postValue(dataLoadingRepo.getJsonObject(url,
                    method, basicAuthentication))
            }
        }).start()


    }


    /**
     * Load the JSON Array
     *
     * @param url The url of the json
     * @param method The method of the api either POST or GET
     * @param basicAuthentication The authentication of the url (OPTIONAL PARAM)
     * @param param The param required for the URL. (OPTIONAL PARAM)
     */
    fun loadJSONArray(
        url: String, method: Int, basicAuthentication: String?,
        param: ContentValues?) {
        Thread(Runnable {
            if (param != null) {
                jsonArrayLiveData.postValue(dataLoadingRepo.getJsonArray(url, param,
                    method, basicAuthentication))
            } else {
                jsonArrayLiveData.postValue(dataLoadingRepo.getJsonArray(url, method,
                    basicAuthentication))
            }
        }).start()

    }

    fun loadXMLNodeList(url: String, tagName: String) {
        Thread(Runnable {
            xmlNodeListLiveData.postValue(dataLoadingRepo.getNodeList(url, tagName))

        }).start()


    }

    /**
     * Check if image view is reused
     *
     * @param imageRequest The object of [ImageRequest]
     *
     * @return true if image-view reused otherwise false
     */
    private fun isImageViewReused(imageRequest: ImageRequest): Boolean {
        val tag = imageViewMap[imageRequest.imageView]
        return tag == null || tag != imageRequest.imgUrl
    }

    /**
     * Load image into image-view
     *
     * @param imageView The image-view where image will be loaded.
     * @param bitmap The image bitmap object.
     * @param imageUrl The url of an image.
     */
    @Synchronized
    private  fun loadImageIntoImageView(imageView: ImageView, bitmap: Bitmap?, imageUrl: String) {

        require(bitmap != null) {
            "ImageLoader:loadImageIntoImageView - Bitmap should not be null"
        }

        // Scale the bitmap according to the image width and height bounds
        val scaledBitmap = dataLoadingRepo.scaleBitmapForLoad(bitmap, imageView.width,
            imageView.height)

        // If Scaled Bitmap is not null check if image view is not reused then set the value to livedata
        scaledBitmap?.let {
            if(!isImageViewReused(ImageRequest(imageUrl, imageView)))
                scaledBitmapLiveData.value = scaledBitmap
        }
    }

    /**
     * Check image in memory cache
     *
     * @param imageUrl The url of an image.
     *
     * @return The bitmap object from the memory cache
     */
    @Synchronized
    fun checkImageInCache(imageUrl: String): Bitmap? = memoryCache.get(imageUrl)

    /**
     * Photo Loader Inner class to download image if it is not found image in memory cache
     */
    inner class PhotosLoader(private var imageRequest: ImageRequest) : Runnable {

        override fun run() {

            if(isImageViewReused(imageRequest)) return

            val bitmap = dataLoadingRepo.downloadBitmapFromURL(imageRequest.imgUrl)
            memoryCache.put(imageRequest.imgUrl, bitmap)

            if(isImageViewReused(imageRequest)) return

            val displayBitmap = DisplayBitmap(imageRequest)
            handler.post(displayBitmap)
        }
    }

    /**
     * Display the image bitmap. If the image view is not reused
     * than call [loadImageIntoImageView] other wise check image in memory cache
     */
    inner class DisplayBitmap(private var imageRequest: ImageRequest) : Runnable {
        override fun run() {
            if(!isImageViewReused(imageRequest)) loadImageIntoImageView(imageRequest.imageView,
                checkImageInCache(imageRequest.imgUrl), imageRequest.imgUrl)
        }
    }

    companion object {


        internal var screenWidth = 0
        internal var screenHeight = 0
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up all pending runnables and remove messages and
        // callbacks that have been posted to that specific Handler
        handler.removeCallbacksAndMessages(null)
    }

}