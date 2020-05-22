package com.yzyfdf.floatwindow

import android.content.Context
import android.view.View
import com.blankj.utilcode.util.LogUtils
import com.yzyfdf.floatwindow.data.FloatConfig
import com.yzyfdf.floatwindow.interfaces.FloatCallbacks
import com.yzyfdf.floatwindow.permission.FloatPermissionUtils
import com.yzyfdf.floatwindow.widget.appfloat.FloatManager


class FloatWindow {

    companion object {

        @JvmStatic
        fun with(context: Context): Builder {
            return Builder(context)
        }

        // *************************** 以下系统浮窗的相关方法 ***************************
        /**
         * 关闭系统级浮窗
         */
        @JvmStatic
        @JvmOverloads
        fun dismissAppFloat(tag: String? = null) = FloatManager.dismiss(tag)

        /**
         * 隐藏系统浮窗
         */
        @JvmStatic
        @JvmOverloads
        fun hideAppFloat(tag: String? = null) = FloatManager.visible(false, tag)

        /**
         * 显示系统浮窗
         */
        @JvmStatic
        @JvmOverloads
        fun showAppFloat(tag: String? = null) = FloatManager.visible(true, tag)

        /**
         * 根据包名，判断是否需要显示
         */
        fun changeShow(className: String, tag: String? = null) {
            FloatManager.visible(className, tag)
        }

        /**
         * 获取系统浮窗是否显示，通过浮窗的config，获取显示状态
         */
        @JvmStatic
        @JvmOverloads
        fun appFloatIsShow(tag: String? = null) = getConfig(tag) != null && getConfig(tag)!!.isShow

        /**
         * 获取系统浮窗中，我们传入的View
         */
        @JvmStatic
        @JvmOverloads
        fun getAppFloatView(tag: String? = null): View? = getConfig(tag)?.layoutView

        /**
         * 增加过滤
         */
        @JvmStatic
        @JvmOverloads
        fun addFilters(tag: String? = null, vararg clazz: String) =
            getConfig(tag)?.filterSet?.addAll(clazz)

        /**
         * 移除过滤
         */
        @JvmStatic
        @JvmOverloads
        fun removeFilters(tag: String? = null, vararg clazz: String) =
            clazz.forEach { c -> getConfig(tag).let { it?.filterSet?.remove(c) } }

        /**
         * 清空过滤
         */
        @JvmStatic
        @JvmOverloads
        fun clearFilters(tag: String? = null) = getConfig(tag)?.filterSet?.clear()

        /**
         * 获取系统浮窗的config
         */
        private fun getConfig(tag: String?) = FloatManager.getAppFloatManager(tag)?.config
    }


    /**
     * 浮窗的属性构建类，支持链式调用
     */
    class Builder(private val context: Context) {

        // 创建浮窗数据类，方便管理配置
        private val config = FloatConfig()

        @JvmOverloads
        fun setLayout(layoutId: Int, invokeView: ((view: View) -> Unit)? = null) = this.apply {
            config.layoutId = layoutId
            config.invokeView = invokeView
        }

        fun setTag(floatTag: String?) = this.apply { config.floatTag = floatTag }

        /**
         * 针对kotlin 用户，传入带FloatCallbacks.Builder 返回值的 lambda，可按需回调
         * 为了避免方法重载时 出现编译错误的情况，更改了方法名
         */
        fun registerCallback(builder: FloatCallbacks.Builder.() -> Unit): Builder = this.apply {
            config.floatCallbacks = FloatCallbacks().apply { registerListener(builder) }
        }

        fun setMatchParent(widthMatch: Boolean = false, heightMatch: Boolean = false) = this.apply {
            config.widthMatch = widthMatch
            config.heightMatch = heightMatch
        }

        /**
         * 设置显示过滤页面还是显示未过滤页面
         * @param showSelector true显示过滤页面，false显示未过滤页面
         */
        fun setShowSOrUnS(showSelector: Boolean) = this.apply {
            config.showSelectorOrUnSelector = showSelector
        }

        fun setShowSelf(showSelf: Boolean) = this.apply {
            config.showSelf = showSelf
        }

        /**
         * 设置需要过滤的页面，仅对系统浮窗有效
         */
        fun setFilter(vararg clazz: String) = this.apply {
            clazz.forEach {
                config.filterSet.add(it)
            }
        }

        /**
         * 创建浮窗，包括Activity浮窗和系统浮窗，如若系统浮窗无权限，先进行权限申请
         */
        fun show() {
            if (config.layoutId != null) {
                // 系统浮窗需要先进行权限审核，有权限则创建app浮窗
                if (FloatPermissionUtils.checkPermission(context)) {
                    createAppFloat()
                } else {
                    LogUtils.w("没有悬浮窗权限", "")
                }
            } else {
                config.floatCallbacks?.builder?.createdResult?.invoke(false, "未设置浮窗布局文件", null)
                LogUtils.w("未设置浮窗布局文件", "")
            }
        }


        /**
         * 通过Service创建系统浮窗
         */
        private fun createAppFloat() = FloatManager.create(context, config)

    }

}