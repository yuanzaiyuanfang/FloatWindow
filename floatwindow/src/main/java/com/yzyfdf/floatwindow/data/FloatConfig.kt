package com.yzyfdf.floatwindow.data

import android.view.View
import com.yzyfdf.floatwindow.interfaces.FloatCallbacks

data class FloatConfig(

    // 浮窗的xml布局文件
    var layoutId: Int? = null,
    var layoutView: View? = null,

    // 当前浮窗的tag
    var floatTag: String? = null,


    // 是否显示
    var isShow: Boolean = false,

    // 宽高是否充满父布局
    var widthMatch: Boolean = false,
    var heightMatch: Boolean = false,

    // Callbacks
    var invokeView: ((view: View) -> Unit)? = null,
    // 通过Kotlin DSL设置回调，无需复写全部方法，按需复写
    var floatCallbacks: FloatCallbacks? = null,


    //过滤规则，过滤的页面是显示还是不显示
    var showSelectorOrUnSelector: Boolean = true,

    //自身是否显示
    var showSelf: Boolean = false,

    // 不需要显示系统浮窗的页面集合，参数为类名
    val filterSet: MutableSet<String> = mutableSetOf()

)