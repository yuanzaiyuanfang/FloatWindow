package com.yzyfdf.floatwindow.widget.appfloat

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.blankj.utilcode.util.LogUtils
import com.yzyfdf.floatwindow.data.FloatConfig


internal class AppFloatManager(val context: Context, var config: FloatConfig) {

    lateinit var windowManager: WindowManager
    lateinit var params: WindowManager.LayoutParams
    var frameLayout: ParentFrameLayout? = null
    lateinit var floatingView: View

    /**
     * 创建系统浮窗
     */
    fun createFloat() = try {
        initParams()
        addView()
        config.isShow = true
    } catch (e: Exception) {
        config.floatCallbacks?.builder?.createdResult?.invoke(false, "$e", null)
    }

    private fun initParams() {
        windowManager = context.getSystemService(Service.WINDOW_SERVICE) as WindowManager
        params = WindowManager.LayoutParams().apply {
            // 安卓6.0 以后，全局的Window类别，必须使用TYPE_APPLICATION_OVERLAY
            type =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE

            format = PixelFormat.RGBA_8888
            gravity = Gravity.START or Gravity.TOP
            // 设置浮窗以外的触摸事件可以传递给后面的窗口、不自动获取焦点
            flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            width =
                if (config.widthMatch) WindowManager.LayoutParams.MATCH_PARENT else WindowManager.LayoutParams.WRAP_CONTENT
            height =
                if (config.heightMatch) WindowManager.LayoutParams.MATCH_PARENT else WindowManager.LayoutParams.WRAP_CONTENT

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
    }

    /**
     * 将自定义的布局，作为xml布局的父布局，添加到windowManager中，
     * 重写自定义布局的touch事件，实现拖拽效果。
     */
    private fun addView() {
        // 创建一个frameLayout作为浮窗布局的父容器
        frameLayout = ParentFrameLayout(context)
        frameLayout?.tag = config.floatTag
        // 将浮窗布局文件添加到父容器frameLayout中，并返回该浮窗文件
        floatingView =
            LayoutInflater.from(context).inflate(config.layoutId!!, frameLayout, true)
        // 为了避免创建的时候闪一下，我们先隐藏视图，不能直接设置GONE，否则定位会出现问题
        floatingView.visibility = View.INVISIBLE
        // 将frameLayout添加到系统windowManager中
        windowManager.addView(frameLayout, params)

        // 在浮窗绘制完成的时候，设置初始坐标、执行入场动画
        frameLayout?.layoutListener = object : ParentFrameLayout.OnLayoutListener {
            override fun onLayout() {
                setGravity(frameLayout)
                enterAnim(floatingView)
            }
        }

        // 设置callbacks
        config.layoutView = floatingView
        config.invokeView?.also { it(floatingView) }
        config.floatCallbacks?.builder?.createdResult?.invoke(true, null, floatingView)
    }

    /**
     * 设置浮窗的对齐方式，支持上下左右、居中、上中、下中、左中和右中，默认左上角
     * 支持手动设置的偏移量
     */
    @SuppressLint("RtlHardcoded")
    private fun setGravity(view: View?) {
        if (view == null) return

        // 设置偏移量
        params.x += 0
        params.y += 0
        // 更新浮窗位置信息
        windowManager.updateViewLayout(view, params)
    }

    /**
     * 设置浮窗的可见性
     */
    fun setVisible(visible: Int) {
        if (frameLayout == null) return

        frameLayout?.visibility = visible
        if (visible == View.VISIBLE) {
            config.isShow = true
            if (frameLayout!!.childCount > 0) {
                config.floatCallbacks?.builder?.show?.invoke(frameLayout!!.getChildAt(0))
            }
        } else {
            config.isShow = false
            if (frameLayout!!.childCount > 0) {
                config.floatCallbacks?.builder?.hide?.invoke(frameLayout!!.getChildAt(0))
            }
        }
    }

    /**
     * 入场动画
     */
    private fun enterAnim(floatingView: View) {
        if (frameLayout == null) return

        floatingView.visibility = View.VISIBLE
        windowManager.updateViewLayout(floatingView, params)
    }

    /**
     * 退出动画
     */
    fun exitAnim() {
        if (frameLayout == null) return
        floatOver()

    }

    /**
     * 退出动画执行结束/没有退出动画，一些回调、移除、检测是否需要关闭Service等操作
     */
    private fun floatOver() = try {
        config.floatCallbacks?.builder?.dismiss?.invoke()
        FloatManager.remove(config.floatTag)
        windowManager.removeView(frameLayout)
    } catch (e: Exception) {
        LogUtils.e("浮窗关闭出现异常：", "$e")
    }

}