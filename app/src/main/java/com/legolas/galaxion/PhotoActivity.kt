package com.legolas.galaxion

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_photo.*

class PhotoActivity : AppCompatActivity() {

    private var selectedPhoto: Photo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_photo)

        selectedPhoto = intent.getSerializableExtra(PHOTO_KEY) as Photo
        Glide.with(this).load(selectedPhoto?.url)
                .transition(DrawableTransitionOptions.withCrossFade())
                .apply(
                        RequestOptions()
                                .transforms(CenterCrop(), RoundedCorners(20))
                                .priority(Priority.HIGH)
                                .override(1280, 960)
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                .into(photoImageView)
        photoDescription?.text = selectedPhoto?.explanation
    }

    companion object {
        private const val PHOTO_KEY = "PHOTO"
    }
}
