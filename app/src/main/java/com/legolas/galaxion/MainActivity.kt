
package com.legolas.galaxion

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.recyclerview_item_row.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.sql.Timestamp
import java.util.*


class MainActivity : AppCompatActivity(), ImageRequester.ImageRequesterResponse, RecyclerViewItemListener {

    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: RecyclerAdapter
    private val PHOTO_KEY = "PHOTO"
    private lateinit var gridLayoutManager: GridLayoutManager
    private val lastVisibleItemPosition: Int
        get() = if (recyclerView.layoutManager == linearLayoutManager) {
            linearLayoutManager.findLastVisibleItemPosition()
        } else {
            gridLayoutManager.findLastVisibleItemPosition()
        }
    private var fileName: String? = null

    private var photosList: ArrayList<Photo> = ArrayList()
    private lateinit var imageRequester: ImageRequester

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        linearLayoutManager = LinearLayoutManager(this)
        gridLayoutManager = GridLayoutManager(this, 2)

        recyclerView.layoutManager = linearLayoutManager
        adapter = RecyclerAdapter(photosList, this)
        recyclerView.adapter = adapter
        setRecyclerViewScrollListener()
        setRecyclerViewItemListener()
        imageRequester = ImageRequester(this)
    }

    override fun onStart() {
        super.onStart()

        if (photosList.size == 0) {
            requestPhoto()
        }

    }

    override fun onPostResume() {
        super.onPostResume()
        val b: Bundle? = null
        b?.putString("APP_RESUME", "true")
        mFirebaseAnalytics!!.logEvent("app_resume", b)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_change_recycler_manager) {
            changeLayoutManager()
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    private fun requestPhoto() {
        try {
            imageRequester.getPhoto()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    override fun receivedNewPhoto(newPhoto: Photo) {
        runOnUiThread {
            photosList.add(newPhoto)
            adapter.notifyItemInserted(photosList.size)
        }
    }

    private fun setRecyclerViewScrollListener() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val totalItemCount = recyclerView.layoutManager!!.itemCount
                if (!imageRequester.isLoadingData && totalItemCount == lastVisibleItemPosition + 1) {
                    requestPhoto()
                }
            }
        })
    }

    private fun setRecyclerViewItemListener() {
        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, viewHolder1: RecyclerView.ViewHolder): Boolean {
                //2
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                //3
                val position = viewHolder.adapterPosition
                photosList.removeAt(position)
                recyclerView.adapter!!.notifyItemRemoved(position)
            }
        }
        //4
        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun changeLayoutManager() {
        if (recyclerView.layoutManager == linearLayoutManager) {
            //1
            recyclerView.layoutManager = gridLayoutManager
            //2
            if (photosList.size == 1) {
                requestPhoto()
            }
        } else {
            //3
            recyclerView.layoutManager = linearLayoutManager
        }
    }

    override fun onItemClicked(photo: Photo?) {
        val showPhotoIntent = Intent(this, PhotoActivity::class.java)
        showPhotoIntent.putExtra(PHOTO_KEY, photo)
        val p1: Pair<View, String> = Pair(itemImage as View, "photo")
        val p2: Pair<View, String> = Pair(itemDescription as View, "photoText")
        val options: ActivityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this, p1)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            startActivity(showPhotoIntent, options.toBundle())
        } else
            startActivity(showPhotoIntent)
    }

    override fun onShareItemClicked(photo: Photo?) {

        fileName = "Galaxion"+System.currentTimeMillis()+".jpeg"
        if (photo != null) {
            Glide.with(this)
                    .asBitmap()
                    .load(photo.url)
                    .into(object : SimpleTarget<Bitmap>() {

                        override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                            write(fileName!!, resource)
                        }
                    })
        }

        val imageShareIntent = Intent(Intent.ACTION_SEND)
        imageShareIntent.type = "image/jpeg"
        imageShareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(Environment.getExternalStorageDirectory().absolutePath + File.separator + fileName))
        startActivity(Intent.createChooser(imageShareIntent, "Share intent"))
    }

    fun write(fileName: String, bitmap: Bitmap) {
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
        val outputStream: FileOutputStream
        val file = File(Environment.getExternalStorageDirectory().absolutePath + File.separator + fileName)
        try {
            file.createNewFile()
            outputStream = FileOutputStream(file)
            outputStream.write(bos.toByteArray())
            outputStream.close()
        } catch (error: Exception) {
            error.printStackTrace()
        }

    }

}
