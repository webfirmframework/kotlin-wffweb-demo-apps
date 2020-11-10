package com.wffwebdemo.minimalproductionsample

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object AppSettings {
    @JvmField
    val CACHED_THREAD_POOL: ExecutorService = Executors.newCachedThreadPool()
}