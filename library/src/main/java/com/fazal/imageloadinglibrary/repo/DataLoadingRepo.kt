package com.fazal.imageloadinglibrary.repo

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Process
import com.fazal.imageloadinglibrary.ui.DataLoaderViewModel
import com.fazal.imageloadinglibrary.utils.JSONParser
import com.fazal.imageloadinglibrary.utils.XMLParser
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URL
import java.util.concurrent.ThreadFactory

/**
 * Image Utility Repo Class
 */
class DataLoadingRepo() {

    val jsonParser = JSONParser()
    val xmlParser = XMLParser()

    fun scaleBitmapForLoad(bitmap: Bitmap, width: Int, height: Int): Bitmap? {

        if(width == 0 || height == 0) return bitmap

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val inputStream = BufferedInputStream(ByteArrayInputStream(stream.toByteArray()))

        // Scale Bitmap to required ImageView Size
        return scaleBitmap(inputStream,  width, height)
    }


    /**
     * Scale the bitmap to the specific height and width of an image-view
     *
     * @param inputStream
     * @param width The width of an image-view
     * @param height The height of an image-view
     *
     * @return The [Bitmap]
     */
    fun scaleBitmap(inputStream: BufferedInputStream, width: Int, height: Int) : Bitmap? {
        return BitmapFactory.Options().run {
            inputStream.mark(inputStream.available())

            inJustDecodeBounds = true
            BitmapFactory.decodeStream(inputStream, null, this)

            inSampleSize = calculateInSampleSize(this, width, height)

            inJustDecodeBounds = false
            inputStream.reset()
            BitmapFactory.decodeStream(inputStream, null,  this)
        }
    }

    // From Developer Site
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {

        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) inSampleSize *= 2
        }

        return inSampleSize
    }

    // Thread Factory to set Thread priority to Background
     inner class ImageThreadFactory : ThreadFactory {
        override fun newThread(runnable: Runnable): Thread {
            return Thread(runnable).apply {
                name = "ImageLoader Thread"
                priority = Process.THREAD_PRIORITY_BACKGROUND
            }
        }
    }

    fun downloadBitmapFromURL(imageUrl: String): Bitmap? {
        val url = URL(imageUrl)
        val inputStream = BufferedInputStream(url.openConnection().getInputStream())

        // Scale Bitmap to Screen Size to store in Cache
        return scaleBitmap(inputStream, DataLoaderViewModel.screenWidth,
            DataLoaderViewModel.screenHeight)
    }

    fun getJsonObject(url: String, method: Int, authentication: String?) : JSONObject {
        return jsonParser.getJSONFromUrl(url, method, authentication)
    }


    fun getJsonObject(url: String, param: ContentValues, method: Int, authentication: String?) :JSONObject {
        return jsonParser.getJSONFromUrl(url, method, param, authentication)
    }

    fun getJsonArray(url: String, method: Int, authentication: String?) : JSONArray {
        return jsonParser.getJSONArrayFromUrl(url, method, authentication)
    }

    fun getJsonArray(url: String, param: ContentValues, method: Int, authentication: String?) : JSONArray {
        return jsonParser.getJSONArrayFromUrl(url, method, param, authentication)
    }

    fun getNodeList(url: String, tagName: String): HashMap<String, Any> {
        val parser = XMLParser()
        val xml = parser.getXmlFromUrl(url) // getting XML
        val doc = parser.getDomElement(xml) // getting DOM element

        val map = HashMap<String, Any>()
        val elementList = mutableListOf<Element>()
        val nodeList = doc?.getElementsByTagName(tagName)

        for (i in 0 until nodeList?.length!!) {
            val element = nodeList.item(i) as Element
            elementList.add(element)
        }

        map["parser"] = parser
        map["elementList"] = elementList
        return map
    }



}