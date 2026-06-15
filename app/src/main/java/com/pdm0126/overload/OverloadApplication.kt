package com.pdm0126.overload

import android.app.Application
import com.pdm0126.overload.data.OverloadProvider
import kotlin.getValue

class OverloadApplication : Application() {
    val overloadProvider by lazy { OverloadProvider(this) }
}