package com.legolas.galaxion

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.Uri.Builder
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ImageRequester(listeningActivity: Activity) {

    interface ImageRequesterResponse {
        fun receivedNewPhoto(newPhoto: Photo)
    }

    private val calendar: Calendar = Calendar.getInstance()
    @SuppressLint("SimpleDateFormat")
    private val dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
    private val responseListener: ImageRequesterResponse
    private val context: Context
    private val client: OkHttpClient
    var isLoadingData: Boolean = false
        private set

    init {
        responseListener = listeningActivity as ImageRequesterResponse
        context = listeningActivity.applicationContext
        client = OkHttpClient()
    }

    fun getPhoto() {

        val date = dateFormat.format(calendar.time)

        val urlRequest = Builder().scheme(URL_SCHEME)
                .authority(URL_AUTHORITY)
                .appendPath(URL_PATH_1)
                .appendPath(URL_PATH_2)
                .appendQueryParameter(URL_QUERY_PARAM_DATE_KEY, date)
                .appendQueryParameter(URL_QUERY_PARAM_API_KEY, context.getString(R.string.api_key))
                .build().toString()

        val request = Request.Builder().url(urlRequest).build()
        isLoadingData = true

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                isLoadingData = false
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) = try {
                val photoJSON = JSONObject(response.body()!!.string())

                calendar.add(Calendar.DAY_OF_YEAR, -1)

                if (photoJSON.getString(MEDIA_TYPE_KEY) != MEDIA_TYPE_VIDEO_VALUE) {
                    val receivedPhoto = Photo(photoJSON)
                    responseListener.receivedNewPhoto(receivedPhoto)
                    isLoadingData = false
                } else {
                    getPhoto()
                }
            } catch (e: JSONException) {
                isLoadingData = false
                e.printStackTrace()
            }
        })
    }

    companion object {
        private const val MEDIA_TYPE_KEY = "media_type"
        private const val MEDIA_TYPE_VIDEO_VALUE = "video"
        private const val URL_SCHEME = "https"
        private const val URL_AUTHORITY = "api.nasa.gov"
        private const val URL_PATH_1 = "planetary"
        private const val URL_PATH_2 = "apod"
        private const val URL_QUERY_PARAM_DATE_KEY = "date"
        private const val URL_QUERY_PARAM_API_KEY = "api_key"
    }
}
