package com.lxj.statelayout

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.view.ViewGroup
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v4.app.Fragment
import com.lxj.statelayout.State.*


class StateLayout : FrameLayout {
    var state = Loading // default state
    var loadingView: View = inflate(context, R.layout._loading_layout_loading, null)
    var emptyView: View = inflate(context, R.layout._loading_layout_empty, null)
    var errorView: View = inflate(context, R.layout._loading_layout_error, null)
    var contentView: View? = null
    var hasLoadingOverlay: Boolean = false
    var loadingOverlayColor = Color.parseColor("#DCFFFFFF")
    var animDuration = 250L

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        with(emptyView) {
            visibility = View.INVISIBLE
            alpha = 0f
        }
        with(errorView) {
            visibility = View.INVISIBLE
            alpha = 0f
        }
    }

    fun wrap(view: View?): StateLayout {
        if (view == null) {
            throw IllegalArgumentException("view can not be null")
        }
        // 1.remove self
        val parent = view.parent as ViewGroup
        val lp = view.layoutParams
        val index = parent.indexOfChild(view)
        parent.removeView(view)

        // 2.wrap a new parent
        view.visibility = View.INVISIBLE
        view.alpha = 0f

        addView(view, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        prepareStateView()
        contentView = view
        // 3.add to parent
        parent.addView(this, index, lp)
        return this
    }

    fun wrap(activity: Activity): StateLayout = wrap((activity.findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0))

    fun wrap(fragment: Fragment): StateLayout = wrap(fragment.view)

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (contentView == null) {
            throw IllegalArgumentException("contentView can not be null!")
        }
    }

    private fun prepareStateView() {
        addView(emptyView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(errorView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(loadingView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        bringChildToFront(loadingView)
        if (hasLoadingOverlay) {
            loadingView.setBackgroundColor(loadingOverlayColor)
        }
    }

    private fun switchLayout(s: State = Loading) {
        state = s
        when (state) {
            Loading -> show(loadingView)
            Empty -> show(emptyView)
            Error -> show(errorView)
            Content -> show(contentView)
        }
    }

    fun showLoading() {
        switchLayout(Loading)
    }

    fun showContent() {
        switchLayout(Content)
    }

    fun showEmpty() {
        switchLayout(Empty)
    }

    fun showError() {
        switchLayout(Error)
    }

    private fun show(v: View?) {
        if (v == null) {
            throw IllegalArgumentException("contentView can not be null!")
        }
        for (i in 0..childCount) {
            val child = getChildAt(i)
            if (child == v) {
                showAnim(child)
            } else {
                //hide others
                if (hasLoadingOverlay && child == contentView && state == Loading) {
                    continue
                }
                hideAnim(child)
            }
        }
    }

    private fun showAnim(v: View?) {
        if (v == null || v.visibility == View.VISIBLE) return
        v.animate().alpha(1f).setDuration(animDuration)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
                        v.visibility = View.VISIBLE
                    }
                })
                .start()
    }

    private fun hideAnim(v: View?) {
        if (v == null || v.visibility == View.INVISIBLE) return
        v.animate().alpha(0f).setDuration(animDuration)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        v.visibility = View.INVISIBLE
                    }
                })
                .start()
    }

}