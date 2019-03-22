package com.github.xubo.ceilinglayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.Scroller;

import com.github.xubo.statuslayout.StatusLayout;

/**
 * Author：xubo
 * Time：2019-03-20
 * Description：吸顶Layout
 */
public class CeilingLayout extends LinearLayout implements NestedScrollingParent {
    private int ceilingChildIndex;
    private int ceilingHeight;

    private NestedScrollingParentHelper parentHelper;
    private NestedScrollingChildHelper childHelper;
    private NestedScroller nestedScroller;
    private float minimumFlingVelocity;
    private float maximumFlingVelocity;

    private CeilingListener ceilingListener;

    public CeilingLayout(Context context) {
        this(context, null);
    }

    public CeilingLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CeilingLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CeilingLayout);
        ceilingChildIndex = typedArray.getInt(R.styleable.CeilingLayout_ceiling_childIndex, -1);
        typedArray.recycle();
        init(context);
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        parentHelper = new NestedScrollingParentHelper(this);
        childHelper = new NestedScrollingChildHelper(this);
        nestedScroller = new NestedScroller(context);
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        minimumFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        maximumFlingVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int childCount = getChildCount();
        if (ceilingChildIndex < 0 || ceilingChildIndex >= childCount) {
            throw new IllegalStateException("吸顶子View位置索引错误,CeilingLayout没有索引为" + ceilingChildIndex + "的子View");
        } else if (ceilingChildIndex == 0) {
            throw new IllegalStateException("吸顶子View位置索引不能为0,最顶层子View吸顶无任何意义");
        } else if (ceilingChildIndex != -1) {
            if (ceilingChildIndex + 2 == childCount) {
                ceilingHeight = 0;
                for (int index = 0; index < ceilingChildIndex; index++) {
                    View childView = getChildAt(index);
                    LayoutParams params = (LayoutParams) childView.getLayoutParams();
                    ceilingHeight += (childView.getMeasuredHeight() + params.topMargin + params.bottomMargin);
                }
                View ceilingChildView = getChildAt(ceilingChildIndex);
                LayoutParams params = (LinearLayout.LayoutParams) ceilingChildView.getLayoutParams();
                int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
                int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
                int lastChildViewHeight = measuredHeight - ceilingChildView.getMeasuredHeight() - params.topMargin - params.bottomMargin;

                //最后子View重新分配高度
                View lastChildView = getChildAt(ceilingChildIndex + 1);
                int lastChildWidthMeasureSpec = MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY);
                int lastChildHeightMeasureSpec = MeasureSpec.makeMeasureSpec(lastChildViewHeight, MeasureSpec.EXACTLY);
                lastChildView.measure(lastChildWidthMeasureSpec, lastChildHeightMeasureSpec);
            } else {
                throw new IllegalStateException("在CeilingLayout里,吸顶子View下面只能配置一个子View");
            }
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (ceilingListener != null && ceilingChildIndex != -1) {
            if (t == ceilingHeight) {
                ceilingListener.scroll(true, 1);
            } else {
                float scale = (float) t / ceilingHeight;
                ceilingListener.scroll(false, scale);
            }
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (nestedScroller.computeScrollOffset()) {
            //当前scroller滑动位置
            int currY = nestedScroller.getCurrY();
            //本身可滑动的最终位置
            int scrollFianl = nestedScroller.getScrollFianl();
            //开始滑动本身的位置
            int scrollSelf = nestedScroller.getScrollSelf();
            //是否向上滑动
            boolean isUpScroll = nestedScroller.isUpScroll();
            if (isUpScroll && currY >= scrollSelf) {  //向上滑动, 滑动位置从小到大
                if (currY >= scrollFianl) {  //大于本身滑动最终距离滑动到最终位置
                    scrollTo(0, scrollFianl);
                } else {
                    scrollTo(0, currY);
                }
            } else if (!isUpScroll && currY <= scrollSelf) { //向下滑动, 滑动位置从大到小
                if (currY <= scrollFianl) {  //小于本身滑动最终距离滑动到最终位置
                    scrollTo(0, scrollFianl);
                } else {
                    scrollTo(0, currY);
                }
            }
            invalidate();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            nestedScroller.forceFinished(true);
            invalidate();
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return nestedScrollAxes == SCROLL_AXIS_VERTICAL ? true : false;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        parentHelper.onNestedScrollAccepted(child, target, axes);
    }

    @Override
    public void onStopNestedScroll(View child) {
        parentHelper.onStopNestedScroll(child);
        childHelper.stopNestedScroll();
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        childHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, null);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        if (dy > 0 && getScrollY() < ceilingHeight) {  //向上滑动
            if (getScrollY() + dy > ceilingHeight) {
                dy = ceilingHeight - getScrollY();
            }
            scrollBy(0, dy);
            consumed[1] = dy;
        } else if (dy < 0 && getScrollY() > 0) {  //向下滑动
            int childScrollY = target.getScrollY();
            if (target instanceof RecyclerView) {
                RecyclerView recyclerView = (android.support.v7.widget.RecyclerView) target;
                childScrollY = recyclerView.computeVerticalScrollOffset();
            } else if (target instanceof NestedScrollView) {
                NestedScrollView nestedScrollView = (NestedScrollView) target;
                childScrollY = nestedScrollView.getScrollY();
            } else if (target instanceof CeilingSmartRefreshLayout) {
                CeilingSmartRefreshLayout refreshLayout = (CeilingSmartRefreshLayout) target;
                View contentView = refreshLayout.getRefreshContentView();
                childScrollY = contentView.getScrollY();
                if (contentView instanceof RecyclerView) {
                    RecyclerView recyclerView = (android.support.v7.widget.RecyclerView) contentView;
                    childScrollY = recyclerView.computeVerticalScrollOffset();
                } else if (target instanceof NestedScrollView) {
                    NestedScrollView nestedScrollView = (NestedScrollView) contentView;
                    childScrollY = nestedScrollView.getScrollY();
                } else if (contentView instanceof StatusLayout) {
                    StatusLayout statusLayout = (StatusLayout) contentView;
                    RecyclerView recyclerView = null;
                    NestedScrollView nestedScrollView = null;
                    for (int i = 0; i < statusLayout.getChildCount(); i++) {
                        View childView = statusLayout.getChildAt(i);
                        if (childView instanceof RecyclerView) {
                            recyclerView = (android.support.v7.widget.RecyclerView) childView;
                            break;
                        }
                        if (childView instanceof NestedScrollView) {
                            nestedScrollView = (NestedScrollView) childView;
                            break;
                        }
                    }
                    if (recyclerView != null) {
                        childScrollY = recyclerView.computeVerticalScrollOffset();
                    } else if (nestedScrollView != null) {
                        childScrollY = nestedScrollView.getScrollY();
                    }
                }
            }
            if (childScrollY <= 0) {
                if (getScrollY() + dy < 0) {
                    dy = -getScrollY();
                }
                scrollBy(0, dy);
                consumed[1] = dy;
            }
        }
        childHelper.dispatchNestedPreScroll(dx, dy, consumed, null);
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return childHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        if (scrollSelfIsConsumed(target, velocityY)) {
            return true;
        } else {
            return childHelper.dispatchNestedPreFling(velocityX, velocityY);
        }
    }

    @Override
    public int getNestedScrollAxes() {
        return parentHelper.getNestedScrollAxes();
    }

    private boolean scrollSelfIsConsumed(View target, float velocityY) {
        if (Math.abs(velocityY) >= minimumFlingVelocity) {
            //滑动速度
            int yVelocity = (int) (Math.abs(velocityY) <= maximumFlingVelocity ? velocityY : (velocityY > 0 ? maximumFlingVelocity : -maximumFlingVelocity));
            //联动子View的偏移量
            int childScrollOffset = target.getScrollY();
            if (target instanceof RecyclerView) {
                RecyclerView recyclerView = (android.support.v7.widget.RecyclerView) target;
                childScrollOffset = recyclerView.computeVerticalScrollOffset();
            } else if (target instanceof CeilingSmartRefreshLayout) {
                CeilingSmartRefreshLayout refreshLayout = (CeilingSmartRefreshLayout) target;
                View contentView = refreshLayout.getRefreshContentView();
                childScrollOffset = contentView.getScrollY();
                if (contentView instanceof RecyclerView) {
                    RecyclerView recyclerView = (android.support.v7.widget.RecyclerView) contentView;
                    childScrollOffset = recyclerView.computeVerticalScrollOffset();
                } else if (contentView instanceof StatusLayout) {
                    StatusLayout statusLayout = (StatusLayout) contentView;
                    RecyclerView recyclerView = null;
                    for (int i = 0; i < statusLayout.getChildCount(); i++) {
                        View childView = statusLayout.getChildAt(i);
                        if (childView instanceof RecyclerView) {
                            recyclerView = (android.support.v7.widget.RecyclerView) childView;
                            break;
                        }
                    }
                    if (recyclerView != null) {
                        childScrollOffset = recyclerView.computeVerticalScrollOffset();
                    }
                }
            }
            //自身偏移量
            int scrollOffset = computeVerticalScrollOffset();
            //自身最大偏移量
            int maxScrollOffset = ceilingHeight;
            //自身可滑动剩余量
            int haveScrollOffset = maxScrollOffset - scrollOffset;
            if ((yVelocity > 0 && haveScrollOffset > 0) || (yVelocity < 0 && haveScrollOffset < maxScrollOffset)) {
                nestedScroller.fling(yVelocity, childScrollOffset, scrollOffset, maxScrollOffset);
                invalidate();
                if (nestedScroller.isUpScroll() && nestedScroller.getFinalY() <= nestedScroller.getScrollFianl()) {  //向上滑动如果惯性偏移量小于本身可滑动的最大偏移量,则禁止联动子View滑动
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 设置吸顶子View的位置索引
     * @param ceilingChildIndex
     */
    public void setCeilingChildIndex(int ceilingChildIndex) {
        this.ceilingChildIndex = ceilingChildIndex;
        requestLayout();
    }

    /**
     * 设置吸顶滚动监听
     * @param ceilingListener
     */
    public void setCeilingListener(CeilingListener ceilingListener) {
        this.ceilingListener = ceilingListener;
    }

    /**
     * 惯性联动Scroller
     */
    public class NestedScroller extends Scroller {
        //可滑动本身位置的位置
        private int scrollSelf;
        //本身惯性滑动最终位置
        private int scrollFianl;
        //受否向上滑动
        private boolean isUpScroll;

        public NestedScroller(Context context) {
            super(context);
        }

        /**
         * 惯性滑动
         * @param velocityY 滑动速度
         * @param childScrollOffset 联动子View滑动偏移量
         * @param scrollOffset 本身滑动偏移量
         * @param maxScrollOffset 本身可滑动最大偏移量
         */
        public void fling(int velocityY, int childScrollOffset, int scrollOffset, int maxScrollOffset) {
            if (velocityY > 0) {  //向上滑动
                this.scrollSelf = 0;
                this.scrollFianl = maxScrollOffset;
                this.isUpScroll = true;
                fling(0, scrollOffset, 0, velocityY, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
            } else {  //向上滑动
                this.scrollSelf = scrollOffset;
                this.scrollFianl = 0;
                this.isUpScroll = false;
                fling(0, childScrollOffset + scrollOffset, 0, velocityY, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
            }
        }

        public int getScrollSelf() {
            return scrollSelf;
        }

        public int getScrollFianl() {
            return scrollFianl;
        }

        public boolean isUpScroll() {
            return isUpScroll;
        }
    }

    public interface CeilingListener {
        /**
         * 吸顶滚动
         * @param isCeiling 是否吸顶
         * @param scale 吸顶滚动的百分比(未滑动为0, 吸顶为1)
         */
        void scroll(boolean isCeiling, float scale);
    }
}
