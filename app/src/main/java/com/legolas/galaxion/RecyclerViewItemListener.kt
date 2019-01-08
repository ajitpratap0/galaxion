package com.legolas.galaxion



interface RecyclerViewItemListener {

    fun onItemClicked(photo: Photo?)

    fun onShareItemClicked(photo: Photo?)
}