package com.horsesgo

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.horsesgo.storage.MmkvMatchStore
import com.horsesgo.ui.HorsesGoApp
import com.tencent.mmkv.kmp.MMKV
import com.tencent.mmkv.kmp.initialize

fun MainViewController() = ComposeUIViewController(
    configure = { MMKV.initialize() },
) {
    val store = remember { MmkvMatchStore() }
    HorsesGoApp(store)
}
