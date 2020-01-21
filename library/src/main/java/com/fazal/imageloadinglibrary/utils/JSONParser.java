package com.fazal.imageloadinglibrary.utils;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

/**
 * JSONParser class to parse JSON
 * 
 * @author Antarix
 * 
 */
public class JSONParser {

	// Method type
	public final static int GET = 1;
	public final static int POST = 2;
	public final static int PUT = 3;
	public final static int DELETE = 4;


	/**
	 * Get response string
	 * 
	 * @param url
	 * @param method
	 * @return
	 */
	public String getResponseString(String url, int method, String basicAuth) {
		return makeServiceCall(url, method, null, basicAuth);
	}

	/**
	 * Get response string
	 * 
	 * @param url
	 * @param method
	 * @param params
	 * @return
	 */
	public String getResponseString(String url, int method,
			ContentValues params, String basicAuth) {
		return makeServiceCall(url, method, params, basicAuth);
	}

	/**
	 * Get JSON
	 * 
	 * @param url
	 * @param method
	 * @return
	 */
	public JSONObject getJSONFromUrl(String url, int method, String basicAuth) {
		return getJSONFromUrl(url, method, null, basicAuth);
	}

	/**
	 * Get JSON
	 * 
	 * @param url
	 * @param method
	 * @return
	 */
	public JSONObject getJSONFromUrl(String url, int method,
			ContentValues params, String basicAuth) {

		JSONObject json = null;
		// try parse the string to a JSON object
		try {
			String jsonString = makeServiceCall(url, method, params, basicAuth);
			if (jsonString != null) {
				json = new JSONObject(jsonString);
			}
			return json;
		} catch (JSONException e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
			return json;
		}

	}

	/**
	 * Get JSONArray
	 * 
	 * @param url
	 * @param method
	 * @return
	 */
	public JSONArray getJSONArrayFromUrl(String url, int method,
			String basicAuth) {
		return getJSONArrayFromUrl(url, method, null, basicAuth);
	}

	/**
	 * Get JSONArray
	 * 
	 * @param url
	 * @param method
	 * @return
	 */
	public JSONArray getJSONArrayFromUrl(String url, int method,
			ContentValues params, String basicAuth) {

		JSONArray jsonArray = null;
		// try parse the string to a JSON object
		try {
			String jsonString = makeServiceCall(url, method, params, basicAuth);
			if (jsonString != null) {
				Log.e("JsonString", jsonString);
				jsonArray = new JSONArray(jsonString);
			}
			return jsonArray;
		} catch (JSONException e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
			return jsonArray;
		}

	}

	/**
	 * Making service call
	 * 
	 * @url - url to make request
	 * @method - http request method
	 * @params - http request params
	 * */
	private String makeServiceCall(String address, int method,ContentValues params, String basicAuth) {


		String result = null;
		
		
			// http client
			
			URL url = null;

			boolean isSecureCall = address.startsWith("https");

			HttpURLConnection urlConnection = null;

			// String userCredentials = "username:password";
			// String basicAuth = "Basic " + new String(new
			// Base64().encode(userCredentials.getBytes()));

			OutputStream out;
			InputStream in;
			try {
				
				url = new URL(address);

				if (isSecureCall){
					urlConnection = (HttpsURLConnection)url.openConnection();
				}else{
					urlConnection = (HttpURLConnection) url.openConnection();
				}
				urlConnection.setConnectTimeout(15000);
				urlConnection.setReadTimeout(15000);
				urlConnection.setAllowUserInteraction(false);
				urlConnection.setInstanceFollowRedirects(true);


				// Checking http request method type
				if (method == POST) {
					urlConnection.setRequestMethod("POST");
					urlConnection.setDoOutput(true);
					urlConnection.setDoInput(true);
				} else if (method == GET) {
					urlConnection.setRequestMethod("GET");
				} else if (method == PUT) {
					urlConnection.setRequestMethod("PUT");
					urlConnection.setDoOutput(true);
				} else if (method == DELETE) {
					urlConnection.setRequestMethod("DELETE");
					urlConnection.setDoOutput(true);
				}


				urlConnection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
				//urlConnection.setRequestProperty( "Content-Type", "application/form-data");
				//urlConnection.setRequestProperty( "Content-Type", "x-www-form-urlencoded");

				urlConnection.setRequestProperty( "charset", "utf-8");
				urlConnection.setRequestProperty("Accept","application/json");

				if (basicAuth != null && basicAuth.length() > 0) {
					urlConnection.setRequestProperty("Authorization", basicAuth);
				}

				if(method != GET){
					if (params != null) {
						StringBuilder postData = new StringBuilder();

						for (String key : params.keySet()) {
							if (postData.length() != 0) postData.append('&');
							postData.append(URLEncoder.encode(key, "UTF-8"));
							postData.append('=');
							postData.append(URLEncoder.encode(String.valueOf(params.get(key)), "UTF-8"));
						}
						byte[] outData = postData.toString().getBytes("UTF-8");

						//byte[] outData = params.toString().getBytes("UTF-8");
						urlConnection.setRequestProperty( "Content-Length", Integer.toString(outData.length));
						out = new BufferedOutputStream(
								urlConnection.getOutputStream());
						out.write(outData);
						out.close();

					}
				}


				urlConnection.connect();

				in = new BufferedInputStream(urlConnection.getInputStream());
				result = inputStreamToString(in);
				Log.e("ServerResponse", result);
			} catch (Exception e) {
				Log.e("MakeServiceCall", "Error : " + e.toString());
			} finally {
				if (urlConnection != null){
					urlConnection.disconnect();
				}
			}

			return result;
	}

	private static String inputStreamToString(InputStream in) {
		String result = "";
		if (in == null) {
			return result;
		}
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
	        StringBuilder out = new StringBuilder();
	        String line;
	        while ((line = reader.readLine()) != null) {
	            out.append(line);
	        }
	        result = out.toString();
	        reader.close();
	        
	        return result;
		} catch (Exception e) {
			// TODO: handle exception
			Log.e("InputStream", "Error : " + e.toString());
			return result;
		}
		
	}
}