package com.legolas.galaxion

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestOptions
import com.legolas.galaxion.R.id.itemImage
import com.legolas.galaxion.R.id.shareView
import kotlinx.android.synthetic.main.recyclerview_item_row.view.*

class RecyclerAdapter(private val photos: ArrayList<Photo>, val context: Context) : RecyclerView.Adapter<RecyclerAdapter.PhotoHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
        val inflatedView = parent.inflate(R.layout.recyclerview_item_row, false)
        return PhotoHolder(inflatedView)

    }

    private var mCallback: RecyclerViewItemListener? = null


    init {
        if (context is RecyclerViewItemListener)
            mCallback = context
    }

    override fun getItemCount(): Int = photos.size

    override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
        val itemPhoto = photos[position]
        holder.bindPhoto(itemPhoto)

    }


    inner class PhotoHolder(var view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        init {
            view.itemImage.setOnClickListener(this)
            view.shareView.setOnClickListener(this)
        }

        //4
        override fun onClick(v: View) {
            when (v.id) {
                itemImage -> mCallback?.onItemClicked(photos[adapterPosition])
                shareView -> mCallback?.onShareItemClicked(photos[adapterPosition])
            }
        }

        fun bindPhoto(photo: Photo) {
            Glide.with(context).load(photo.url)
                    .transition(withCrossFade())
                    .apply(
                            RequestOptions()
                                    .transforms(CenterCrop(), RoundedCorners(8))
                                    .override(320, 200)
                                    .priority(Priority.HIGH)
                                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    )
                    .into(view.itemImage)
            view.itemDate.text = photo.humanDate
            view.itemDescription.text = photo.explanation
        }
    }

}