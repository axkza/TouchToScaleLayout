package com.example.touchtoscaleview.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 可缩放的Layout
 * **/
class TouchToScaleLayout(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    // 缩放view的初始left/top
    private var originalXY: IntArray? = null

    // 触摸时 双指中间的点 / 双指距离
    private var originalTwoPointerCenter: Point? = null
    private var originalDistance: Int = 0

    // 移动时 双指距离 缩放比例
    private var moveDistance: Int = 0
    private var moveScale: Float = 0.0f;

    // 双指移动距离的增量比（用于计算缩放比、背景颜色）
    private var moveDistanceIncrement: Float = 0.0f

    // 缩放的View
    private var scaleableView: View? = null

    // 缩放的View原LayoutParams
    private var viewLayoutParams: ViewGroup.LayoutParams? = null

    // 缩放的View,在dialog中的LayoutParams
    private var dialogFrameLayoutParams: FrameLayout.LayoutParams? = null

    // 用于缩放的dialog
    private var dialog: ScaleDialog? = null

    // 缩放的动画状态
    private var isDismissAnimating: Boolean = false

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return true
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (childCount == 0 && scaleableView == null) return super.dispatchTouchEvent(ev)
        when (ev?.actionMasked) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                requestDisallowInterceptTouchEvent(false)
                if (scaleableView != null) {
                    if (!isDismissAnimating) {
                        dismissWithAnimator()
                    }
                    return true
                }
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                if (ev!!.pointerCount == 2) {
                    requestDisallowInterceptTouchEvent(true)
                    if (scaleableView == null && childCount > 0) {
                        scaleableView = getChildAt(0)
                        originalXY = IntArray(2)
                        scaleableView?.getLocationOnScreen(originalXY)

                        dialog = ScaleDialog(context)
                        dialog?.show()

                        viewLayoutParams = scaleableView!!.layoutParams
                        dialogFrameLayoutParams =
                            LayoutParams(scaleableView!!.width, scaleableView!!.height).apply {
                                leftMargin = originalXY!![0]
                                topMargin = originalXY!![1]
                            }

                        postDelayed({
                            if (scaleableView != null && scaleableView?.parent == this && !isDismissAnimating) {
                                removeView(scaleableView)
                                dialog?.addView(scaleableView!!, dialogFrameLayoutParams)
                            }
                        }, 80)
                    }

                    originalDistance = getDistance(ev)
                    if (originalTwoPointerCenter == null) {
                        originalTwoPointerCenter = Point()
                    }
                    originalTwoPointerCenter?.x = getTwoPointerCenterX(ev)
                    originalTwoPointerCenter?.y = getTwoPointerCenterY(ev)
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (scaleableView != null && scaleableView?.parent != this) {
                    if (ev.pointerCount == 2) {
                        // 双指距离和距离比例
                        moveDistance = getDistance(ev)
                        moveDistanceIncrement =
                            (moveDistance.toFloat() - originalDistance.toFloat()) / originalDistance.toFloat()

                        // 关键点：
                        // 1.设置pivotX和pivotY为view左上角，相比View中心点更容易计算缩放后的位移
                        // 2.位移计算公式 （触摸屏幕时的坐标 * 缩放比 = 缩放后的坐标，当前两指中心点 - 缩放后的坐标 + 触摸屏幕时的leftMargin和topMargin = left和top最终需要的位移）
                        //   leftMargin = 当前两指中心点的x坐标 - 首次触摸屏幕时两指中心点的x坐标 乘以 缩放比 + 首次触摸时的原始leftMargin
                        //   topMargin同上，将x换成y

                        // 缩放
                        moveScale = 1 + moveDistanceIncrement
                        moveScale = max(0.5f, moveScale)
                        moveScale = min(5.0f, moveScale)
                        scaleableView?.run {
                            pivotX = 0f
                            pivotY = 0f
                            scaleX = moveScale
                            scaleY = moveScale
                        }

                        // 位移
                        if (originalTwoPointerCenter != null && originalXY != null) {
                            updateOffset(
                                (getTwoPointerCenterX(ev) - originalTwoPointerCenter!!.x * moveScale) + originalXY!![0].toFloat(),
                                (getTwoPointerCenterY(ev) - originalTwoPointerCenter!!.y * moveScale) + originalXY!![1].toFloat()
                            )
                        }

                        // 透明背景
                        dialog?.setShadowAlpha(max(min(0.8f, moveDistanceIncrement / 1.5f), 0f))
                        return true
                    }
                }

            }
        }
        return super.dispatchTouchEvent(ev)
    }

    // 获取两指的中心点x坐标
    private fun getTwoPointerCenterX(ev: MotionEvent): Int {
        return ((ev.getX(0) + ev.getX(1)) / 2).toInt()
    }

    // 获取两指的中心点y坐标
    private fun getTwoPointerCenterY(ev: MotionEvent): Int {
        return ((ev.getY(0) + ev.getY(1)) / 2).toInt()
    }

    // 获取两指的距离
    private fun getDistance(ev: MotionEvent): Int {
        return sqrt(
            (ev.getX(1).toDouble() - ev.x.toDouble()).pow(2.0) + (ev.getY(1)
                .toDouble() - ev.y.toDouble()).pow(2.0)
        ).toInt()
    }

    // 更新位移
    private fun updateOffset(left: Float, top: Float) {
        dialogFrameLayoutParams?.run {
            leftMargin = left.toInt()
            topMargin = top.toInt()
        }
        scaleableView?.layoutParams = dialogFrameLayoutParams
    }

    // 回弹动画
    private fun dismissWithAnimator() {
        if (scaleableView == null || originalXY == null) return
        if (scaleableView!!.parent == this) {
            dismissDialog()
            return
        }
        isDismissAnimating = true
        val scaleStart = scaleableView!!.scaleY
        val leftMarginStart = dialogFrameLayoutParams!!.leftMargin
        val topMarginStart = dialogFrameLayoutParams!!.topMargin
        val alphaStart = dialog?.getShadowAlpha() ?: 0f

        val scaleEnd = 1f
        val leftMarginEnd = originalXY!![0]
        val topMarginEnd = originalXY!![1]
        val alphaEnd = 0f

        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.duration = 200
        valueAnimator.addUpdateListener {
            scaleableView?.scaleX = ((scaleEnd - scaleStart) * it.animatedFraction) + scaleStart
            scaleableView?.scaleY = ((scaleEnd - scaleStart) * it.animatedFraction) + scaleStart
            updateOffset(
                ((leftMarginEnd - leftMarginStart) * it.animatedFraction) + leftMarginStart,
                ((topMarginEnd - topMarginStart) * it.animatedFraction) + topMarginStart
            )
            dialog?.setShadowAlpha(
                max(
                    min(
                        0.8f, ((alphaEnd - alphaStart) * it.animatedFraction) + alphaStart
                    ), 0f
                )
            )
            if (it.animatedFraction == 1.0f) {
                dismissDialogAndRemoveView()
                valueAnimator.removeAllUpdateListeners()
            }
        }
        valueAnimator.start()
    }

    // 关闭弹窗、view复原（在回弹动画结束之后）
    private fun dismissDialogAndRemoveView() {
        if (scaleableView != null && scaleableView!!.parent != null) {
            val parent = scaleableView!!.parent as ViewGroup
            if (parent != this) {
                parent.removeView(scaleableView)
                addView(scaleableView!!, viewLayoutParams)
            }
            // 先复原view后间隔一段时间再dialog.dismiss(), 避免removeView/addView过程中闪一下
            postDelayed({ dismissDialog() }, 100)
        } else {
            dismissDialog()
        }
    }

    // 关闭弹窗
    private fun dismissDialog() {
        dialog?.dismiss()
        dialog = null
        scaleableView = null
        isDismissAnimating = false
    }
}