package com.yzyfdf.floatwindow.permission

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.blankj.utilcode.util.LogUtils
import com.yzyfdf.floatwindow.interfaces.OnPermissionResult


internal class PermissionFragment : Fragment() {

    companion object {
        private var onPermissionResult: OnPermissionResult? = null

        fun requestPermission(activity: FragmentActivity, onPermissionResult: OnPermissionResult) {
            this.onPermissionResult = onPermissionResult
            activity.supportFragmentManager
                .beginTransaction()
                .add(PermissionFragment(), activity.localClassName)
                .commitAllowingStateLoss()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // 权限申请
        FloatPermissionUtils.requestPermission(this)
        LogUtils.i("PermissionFragment", "requestPermission")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FloatPermissionUtils.requestCode) {
            // 需要延迟执行，不然即使授权，仍有部分机型获取不到权限
            Handler(Looper.getMainLooper()).postDelayed({
                val activity = activity ?: return@postDelayed
                val check = FloatPermissionUtils.checkPermission(activity)
                LogUtils.i("PermissionFragment onActivityResult:", " $check")
                // 回调权限结果
                onPermissionResult?.permissionResult(check)
                // 将Fragment移除
                fragmentManager!!.beginTransaction().remove(this).commitAllowingStateLoss()
            }, 500)
        }
    }

}