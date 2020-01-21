package com.fazal.imageloadinglibrary.ui

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.fazal.imageloadinglibrary.utils.JSONParser
import com.fazal.imageloadinglibrary.utils.XMLParser
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Element
import org.w3c.dom.NodeList

abstract class BaseActivity : AppCompatActivity() {

    lateinit var viewModel: DataLoaderViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this)
            .get(DataLoaderViewModel::class.java)
        viewModel.setupMatrix(this)
    }

    /**
     * Load the image into imageview
     */
    fun loadImage(iv: ImageView, imageURL: String) {

        viewModel.load(iv, imageURL)


        viewModel.scaledBitmapLiveData.observe(this, Observer {
            iv.setImageBitmap(it)
        })
    }

    /**
     * Load the JSON Object
     */
    fun loadJSONObject(url: String, method: Int, authentication: String? = null,
                       param: ContentValues? = null) {

        viewModel.loadJSONObject(url,
            method,
            authentication,
            param)


        viewModel.jsonObjectLiveData.observe(this, Observer {
            getJSONObjectResult(it)
        })
    }

    /**
     * Load the JSON Array
     */
    fun loadJSONArray(url: String, method: Int, authentication: String? = null,
                      param: ContentValues? = null) {

        viewModel.loadJSONArray(url,
            method,
            authentication,
            param)


        viewModel.jsonArrayLiveData.observe(this, Observer {
            getJSONArrayResult(it)
        })
    }

    /**
     * Load the JSON Array
     */
    fun loadXMLNodeList(url: String, tagName: String) {

        viewModel.loadXMLNodeList(url, tagName)


        viewModel.xmlNodeListLiveData.observe(this, Observer {
            getXMLNodeListResult(it)
        })
    }

    /**
     * Fetch the JSON Object Result
     */
    open fun getJSONObjectResult(jsonObject: JSONObject) { }

    /**
     * Fetch the JSOn Array Result
     */
    open fun getJSONArrayResult(jsonArray: JSONArray) { }


    /**
     * Fetch the XML Node List
     */
    open fun getXMLNodeListResult(map: HashMap<String, Any>) { }
}
