package com.example.rhythmmusic.model

import android.os.Bundle

data class QueueItem(
    val mediaId: String,
    val title: String,
    val artist: String,
    val url: String
){
    fun toBundle(): Bundle {
        return Bundle().apply {
            putString("MEDIA_ID", mediaId)
            putString("TITLE", title)
            putString("ARTIST", artist)
            putString("URL", url)
        }
    }

    companion object {
        fun fromBundle(bundle: Bundle): QueueItem {
            return QueueItem(
                mediaId = bundle.getString("MEDIA_ID") ?: "",
                title = bundle.getString("TITLE") ?: "",
                artist = bundle.getString("ARTIST") ?: "",
                url = bundle.getString("URL") ?: ""
            )
        }
    }
}
