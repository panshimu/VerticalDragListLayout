package com.miaozi.verticaldraglistlayout;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.core.widget.ListViewCompat;
import androidx.customview.widget.ViewDragHelper;

public class VerticalDragListLayout extends FrameLayout {
    private ViewDragHelper mViewDragHelper;
    //上层View
    private View mDragContentView;
    //下层View 高度
    private int mInnerViewHeight;
    //按下的Y座标
    private float mDownY;
    //菜单是否打开
    private boolean mInnerViewIsOpen;
    public VerticalDragListLayout(Context context) {
        this(context,null);
    }

    public VerticalDragListLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public VerticalDragListLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mViewDragHelper = ViewDragHelper.create(this,mViewDragHelperCallback);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    /**
     * XML加载完毕
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int childCount = getChildCount();
        if(childCount != 2){
            throw new RuntimeException("VerticalDragListLayout 只能包含两个子布局 ！！！！");
        }
        mDragContentView = getChildAt(1);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(changed){
            mInnerViewHeight = getChildAt(0).getMeasuredHeight();
        }
    }

    //现象：如果contentView是ListView listView可以滑动 但是 拖动效果没有了 所以要解决事件冲突
    //because ACTION_DOWN was not received for this pointer before ACTION_MOVE
    //出现以上的原因：
    //流程分析：VerticalDragListLayout.onInterceptTouchEvent.DOWN -> ListView.OnTouch()->
    //VerticalDragListLayout.onInterceptTouchEvent.MOVE - > VerticalDragListLayout.OnTouchEvent.MOVE
    //看到上面的流程可以看到 ListView的DOWN事件被拦截了 因为重写了 onTouchEvent return true了
        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            //如果 菜单 是打开的 则全部拦截
            if(mInnerViewIsOpen){
                return true;
            }
            //向下滑动的时候 不要给ListView做处理
            //父类拦截子类
            //requestDisallowInterceptTouchEvent 请求父类不要拦截子类的事件 也就是不需要修改mGroupFlags的值
            switch (ev.getAction()){
                case MotionEvent.ACTION_DOWN:
                    mDownY = ev.getY();
                    //所以我们要在这里 给DragHelper一个完整的事件
                    mViewDragHelper.processTouchEvent(ev);
                    break;
                case MotionEvent.ACTION_MOVE:
                    float moveY = ev.getY();
                    Log.d("TAG","moveY="+moveY+"  mDownY="+mDownY);
                    if(moveY - mDownY > 0 && !canChildScrollUp()){
                        //向下滑动 && 滚动到顶部 拦截 不让ListView做处理
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return super.onInterceptTouchEvent(ev);
        }
    /*
    判断是否还能不能向上滚动 源码 来自于 SwipeRefreshLayout
     */
    private boolean canChildScrollUp() {
        if (mDragContentView instanceof ListView) {
            return canScrollList((ListView) mDragContentView, -1);
        }
        return false;
    }
    private boolean canScrollList(ListView listView, int direction) {
        if (Build.VERSION.SDK_INT >= 19) {
            // Call the framework version directly
            return listView.canScrollList(direction);
        } else {
            // provide backport on earlier versions
            final int childCount = listView.getChildCount();
            if (childCount == 0) {
                return false;
            }

            final int firstPosition = listView.getFirstVisiblePosition();
            if (direction > 0) {
                final int lastBottom = listView.getChildAt(childCount - 1).getBottom();
                final int lastPosition = firstPosition + childCount;
                return lastPosition < listView.getCount()
                        || (lastBottom > listView.getHeight() - listView.getListPaddingBottom());
            } else {
                final int firstTop = listView.getChildAt(0).getTop();
                return firstPosition > 0 || firstTop < listView.getListPaddingTop();
            }
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if(mViewDragHelper.continueSettling(true)){
            invalidate();
        }
    }

    //1.拖动我们的子view
    private ViewDragHelper.Callback mViewDragHelperCallback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            //在这里可以指定子 view 是否可以拖动
            //只能是前面的布局才能滑动
            return mDragContentView == child;
        }

        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            //垂直拖动的滑动距离
            if(top < 0){
                top = 0;
            }

            if(top > mInnerViewHeight){
                top = mInnerViewHeight;
            }

            return top;
        }

        /**
         * 拖动松开的时候
         * @param releasedChild
         * @param xvel
         * @param yvel
         */
        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            if(releasedChild == mDragContentView) {
                if (mDragContentView.getTop() > mInnerViewHeight / 2) {
                    //滚动到菜单的高度
                    openInnerView();
                } else {
                    //恢复
                    closeInnerView();
                }
                invalidate();
            }
        }
    };
    private void openInnerView(){
        mInnerViewIsOpen = true;
        mViewDragHelper.settleCapturedViewAt(0, mInnerViewHeight);
    }
    private void closeInnerView(){
        mViewDragHelper.settleCapturedViewAt(0, 0);
        mInnerViewIsOpen = false;
    }
}
