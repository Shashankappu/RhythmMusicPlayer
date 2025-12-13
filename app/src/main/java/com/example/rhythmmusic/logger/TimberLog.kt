package com.example.rhythmmusic.logger

import timber.log.Timber

object TimberLog {
    private const val TAG = "Timber"

    fun d(message: String) {
        Timber.d(message)
    }

    fun i(message: String) {
        Timber.tag(TAG).i(message)
    }

    fun e(message: String, throwable: Throwable? = null) {
        Timber.tag(TAG).e(throwable, message)
    }
}