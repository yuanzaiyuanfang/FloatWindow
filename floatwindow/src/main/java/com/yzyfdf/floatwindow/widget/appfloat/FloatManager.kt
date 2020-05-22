package com.yzyfdf.floatwindow.widget.appfloat

import android.content.Context
import android.view.View
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.LogUtils
import com.yzyfdf.floatwindow.data.FloatConfig


internal object FloatManager {

    private const val DEFAULT_TAG = "default"
    val floatMap = mutableMapOf<String, AppFloatManager>()

    /**
     * 创建系统浮窗，首先检查浮窗是否存在：不存在则创建，存在则回调提示
     */
    fun create(context: Context, config: FloatConfig) {
        if (checkTag(config)) {
            // 通过floatManager创建浮窗，并将floatManager添加到map中
            floatMap[config.floatTag!!] = AppFloatManager(context.applicationContext, config)
                .apply { createFloat() }
        } else {
            //允许多个，需要不同tag
            config.floatCallbacks?.builder?.createdResult?.invoke(false, "请为系统浮窗设置不同的tag", null)
            LogUtils.w("请为系统浮窗设置不同的tag", "")
        }
    }

    /**
     * 设置浮窗的显隐，用户主动调用
     */
    fun visible(isShow: Boolean, tag: String? = null) =
        floatMap[getTag(tag)]?.setVisible(if (isShow) View.VISIBLE else View.GONE)

    /**
     * 设置浮窗的显隐，根据页面切换和设置的条件筛选
     */
    fun visible(
        className: String = "",
        tag: String? = null
    ) {
        val appFloatManager = floatMap[getTag(tag)]
        appFloatManager?.apply {
            val contains = config.filterSet.contains(className)
            val selector = config.showSelectorOrUnSelector
            val showSelf = config.showSelf && className == AppUtils.getAppPackageName()
            setVisible(if (showSelf || contains == selector) View.VISIBLE else View.GONE)
        }
    }

    /**
     * 关闭浮窗，执行浮窗的退出动画
     */
    fun dismiss(tag: String? = null) = floatMap[getTag(tag)]?.exitAnim()

    /**
     * 移除当条浮窗信息，在退出完成后调用
     */
    fun remove(tag: String?) = floatMap.remove(tag)

    /**
     * 获取浮窗tag，为空则使用默认值
     */
    private fun getTag(tag: String?) = tag ?: DEFAULT_TAG

    /**
     * 获取具体的系统浮窗管理类
     */
    fun getAppFloatManager(tag: String?) = floatMap[getTag(tag)]

    /**
     * 检测浮窗的tag是否有效，不同的浮窗必须设置不同的tag
     */
    private fun checkTag(config: FloatConfig): Boolean {
        // 如果未设置tag，设置默认tag
        config.floatTag = getTag(config.floatTag)
        return !floatMap.containsKey(config.floatTag!!)
    }

}