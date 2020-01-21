package com.fazal.imageloadinglibrary.utils

import android.util.Log
import org.w3c.dom.Document
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import org.w3c.dom.Element
import org.w3c.dom.Node


/**
 * XML Parser is responsible for parsing XML
 */
class XMLParser {

    fun getXmlFromUrl(xmlURL: String) : String {
        var xmlString: String = ""
        var urlConnection: HttpURLConnection? = null
        val url = URL(xmlURL)
        try {
            urlConnection = url.openConnection() as HttpURLConnection
            // set request method
            urlConnection.requestMethod = "GET"
            if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                val xmlResponse = StringBuilder()
                val input = BufferedReader(InputStreamReader(urlConnection.inputStream), 8192)
                var strLine: String? = null

                do {

                    strLine = input.readLine()

                    if (strLine == null)

                        break

                    xmlResponse.append(strLine)

                } while (true)

                xmlString = xmlResponse.toString()
                input.close()
            }
        } catch (e: Exception) {
            // do something
            e.printStackTrace()
        } finally {// close connection
            urlConnection?.disconnect()
        }

        return xmlString
    }

    fun getDomElement(xml: String): Document? {
        var doc: Document? = null
        val dbf = DocumentBuilderFactory.newInstance()
        try {

            val db = dbf.newDocumentBuilder()

            val `is` = InputSource()
            `is`.setCharacterStream(StringReader(xml))
            doc = db.parse(`is`)

        } catch (e: ParserConfigurationException) {
            Log.e("Error: ", e.toString())
            return null
        } catch (e: SAXException) {
            Log.e("Error: ", e.toString())
            return null
        } catch (e: IOException) {
            Log.e("Error: ", e.toString())
            return null
        }

        // return DOM
        return doc
    }

    fun getValue(item: Element, str: String): String {
        val n = item.getElementsByTagName(str)
        return this.getElementValue(n.item(0))
    }

    fun getElementValue(elem: Node?): String {
        var child: Node?
            elem?.let {
            if (it.hasChildNodes()) {
                child = it.firstChild
                while (child != null) {
                    if (child!!.nodeType == Node.TEXT_NODE) {
                        return child!!.nodeValue
                    }
                    child = child!!.nextSibling
                }
            }
        }
        return ""
    }


}