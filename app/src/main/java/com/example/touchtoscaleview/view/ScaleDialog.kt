package com.example.touchtoscaleview.view

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.touchtoscaleview.R

/**
 * 用于缩放的dialog
 * 样式可根据需求而定
 * **/
class ScaleDialog(context: Context) :
    Dialog(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen) {
    private var contentView: ViewGroup? = null
    private var shadowView: View? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_scale)
        contentView = findViewById(R.id.content_layout)
        shadowView = findViewById(R.id.shadow_view)

        // 沉浸式dialog适配 核心代码
        val window = window
        window!!.decorView.systemUiVisibility = systemUiVisibility //获取视口全屏大小
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS) //这个flag表示window负责绘制状态栏的背景当设置了这个flag,系统状态栏会变透明,同时这个相应的区域会被填满 getStatusBarColor() and getNavigationBarColor()的颜色
        window.statusBarColor = Color.TRANSPARENT //设置bar为透明色
        val layoutParams = window.attributes //获取dialog布局的参数
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT //宽全屏
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT //高全屏
        window.navigationBarColor = Color.TRANSPARENT //设置导航栏颜
        window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_PANEL) //内容扩展到导航栏
        if (Build.VERSION.SDK_INT >= 28) {
            layoutParams.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        window.attributes = layoutParams

        setCanceledOnTouchOutside(false)
        setCancelable(false)
    }

    fun addView(zoomableView: View, lp: FrameLayout.LayoutParams?) {
        contentView?.addView(zoomableView, lp)
    }

    fun setShadowAlpha(alpha: Float) {
        shadowView?.alpha = alpha
    }

    fun getShadowAlpha(): Float {
        return shadowView?.alpha ?: 0f
    }

    protected val systemUiVisibility: Int
        protected get() = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
}