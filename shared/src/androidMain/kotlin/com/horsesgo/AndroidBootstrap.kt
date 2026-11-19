package com.horsesgo

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.horsesgo.storage.MmkvMatchStore
import com.horsesgo.ui.HorsesGoApp
import com.tencent.mmkv.kmp.MMKV
import com.tencent.mmkv.kmp.initialize

fun initializeAndroidStorage(context: Context) {
    MMKV.initialize(context.applicationContext)
}

@Composable
fun AndroidHorsesGoApp() {
    val store = remember { MmkvMatchStore() }
    HorsesGoApp(store)
}
