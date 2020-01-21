package com.fazal.imageloadingdemo

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.fazal.imageloadinglibrary.ui.BaseActivity
import com.fazal.imageloadinglibrary.utils.JSONParser
import com.fazal.imageloadinglibrary.utils.XMLParser
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Element
import org.w3c.dom.NodeList

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadImage(iv, "https://homepages.cae.wisc.edu/~ece533/images/airplane.png")

        loadJSONObject("https://jsonplaceholder.typicode.com/todos/1", JSONParser.GET)

        loadJSONArray("https://jsonplaceholder.typicode.com/posts", JSONParser.GET)

        loadXMLNodeList("https://api.androidhive.info/pizza/?format=xml", "item")

    }

    override fun getJSONObjectResult(jsonObject: JSONObject) {
        Toast.makeText(this, jsonObject.getString("title"), Toast.LENGTH_LONG).show()
    }

    override fun getJSONArrayResult(jsonArray: JSONArray) {
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            Log.d("title" , jsonObject.getString("title"))
        }
    }


    override fun getXMLNodeListResult(map: HashMap<String, Any>) {
        val parser = map["parser"] as XMLParser
        val elementList = map["elementList"] as List<Element>

        for (i in 0 until elementList.size) {
            Log.d("name", parser.getValue(elementList[i], "name"))
        }
    }
}
