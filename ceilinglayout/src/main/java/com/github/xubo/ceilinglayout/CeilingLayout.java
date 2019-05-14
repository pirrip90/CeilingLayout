package com.github.xubo.ceilinglayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.OverScroller;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;

/**
 * Author：xubo
 * Time：2019-04-25
 * Description：吸顶联滑Layout
 */
public class CeilingLayout extends LinearLayout implements NestedScrollingParent, NestedScrollingChild {
    private static final int INVALID_POINTER = -1;

    private int ceilingChildIndex;
    private int ceilingHeight;
    private int ceilingOffset;
    private View nestedTarget;
    private CeilingListener ceilingListener;
    private ScrollListener scrollListener;

    private NestedScrollingParentHelper parentHelper;
    private NestedScrollingChildHelper childHelper;
    private NestedScroller nestedScroller;
    private OverScroller scroller;
    private float minimumFlingVelocity;
    private float maximumFlingVelocity;
    private VelocityTracker velocityTracker;
    private int touchSlop;
    private int scrollRange;
    private boolean isBeingDragged;
    private int activePointerId;
    private int lastTouchY;
    private int lastScrollerY;
    private int nestedYOffset;
    private final int[] scrollOffset = new int[2];
    private final int[] scrollConsumed = new int[2];

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
        touchSlop = viewConfiguration.getScaledTouchSlop();
        scroller = new OverScroller(context);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        for (ViewParent parent = getParent(); parent != null; parent = parent.getParent()) {
            if (parent instanceof NestedScrollingParent) {
                setNestedScrollingEnabled(true);
                break;
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //最小控制高
        int minMeasuredHeight = MeasureSpec.getSize(heightMeasureSpec);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 1, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
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
                    if (childView.getVisibility() == View.GONE) {
                        continue;
                    }
                    LayoutParams params = (LayoutParams) childView.getLayoutParams();
                    ceilingHeight += (childView.getMeasuredHeight() + params.topMargin + params.bottomMargin);
                }
                scrollRange = ceilingHeight - ceilingOffset;
                if (scrollRange < 0) {
                    throw new IllegalStateException("CeilingLayout偏移高度不能大于吸顶高度");
                }
                View ceilingChildView = getChildAt(ceilingChildIndex);
                LayoutParams ceilingChildParams = (LayoutParams) ceilingChildView.getLayoutParams();
                int measuredHeight = minMeasuredHeight;
                if (ceilingHeight + ceilingChildView.getMeasuredHeight() + ceilingChildParams.topMargin + ceilingChildParams.bottomMargin > minMeasuredHeight) {  //最小控制高无法排下
                    measuredHeight = ceilingHeight + ceilingChildView.getMeasuredHeight() + ceilingChildParams.topMargin + ceilingChildParams.bottomMargin;
                }
                setMeasuredDimension(measuredWidth, measuredHeight);

                //最后子View重新分配高度
                int lastChildViewHeight = getMeasuredHeight() - ceilingChildView.getMeasuredHeight() - ceilingChildParams.topMargin - ceilingChildParams.bottomMargin - ceilingOffset;
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
            if (t == scrollRange) {
                ceilingListener.scroll(true, 1);
            } else {
                float scale = (float) t / scrollRange;
                ceilingListener.scroll(false, scale);
            }
        }
        if (scrollListener != null) {
            scrollListener.onScroll(t);
        }
    }

    @Override
    protected int computeVerticalScrollRange() {
        return getMeasuredHeight() + scrollRange;
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
            ViewCompat.postInvalidateOnAnimation(this);
        }
        if (scroller.computeScrollOffset()) {
            final int y = scroller.getCurrY();
            int dy = y - lastScrollerY;
            if (dy != 0) {
                final int oldScrollY = getScrollY();
                overScroll(dy, oldScrollY, scrollRange);
                final int scrolledDeltaY = getScrollY() - oldScrollY;
                final int unconsumedY = dy - scrolledDeltaY;
                dispatchNestedScroll(0, scrolledDeltaY, 0, unconsumedY, null);
            }
            lastScrollerY = y;
            ViewCompat.postInvalidateOnAnimation(this);
        } else {
            if (hasNestedScrollingParent()) {
                stopNestedScroll();
            }
            lastScrollerY = 0;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        if ((action == MotionEvent.ACTION_MOVE) && (isBeingDragged)) {
            return true;
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                lastTouchY = (int) ev.getY();
                activePointerId = ev.getPointerId(0);
                initOrResetVelocityTracker();
                velocityTracker.addMovement(ev);
                scroller.computeScrollOffset();
                isBeingDragged = !scroller.isFinished();
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                if (activePointerId == INVALID_POINTER) {
                    break;
                }
                int pointerIndex = ev.findPointerIndex(activePointerId);
                if (pointerIndex == -1) {
                    break;
                }
                int y = (int) ev.getY(pointerIndex);
                int diffY = Math.abs(y - lastTouchY);
                if (diffY > touchSlop
                        && (getNestedScrollAxes() & ViewCompat.SCROLL_AXIS_VERTICAL) == 0) {
                    isBeingDragged = true;
                    lastTouchY = y;
                    initVelocityTrackerIfNotExists();
                    velocityTracker.addMovement(ev);
                    nestedYOffset = 0;
                    //阻止父View窃取事件
                    ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }
            }
            break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                isBeingDragged = false;
                activePointerId = INVALID_POINTER;
                recycleVelocityTracker();
//                if (scroller.springBack(getScrollX(), getScrollY(), 0, 0, 0, scrollRange)) {
//                    ViewCompat.postInvalidateOnAnimation(this);
//                }
                stopNestedScroll();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                final int pointerIndex = ev.getActionIndex();
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == activePointerId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    lastTouchY = (int) ev.getY(newPointerIndex);
                    activePointerId = ev.getPointerId(newPointerIndex);
                    if (velocityTracker != null) {
                        velocityTracker.clear();
                    }
                }
                break;
        }
        return isBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        initVelocityTrackerIfNotExists();
        MotionEvent motionEvent = MotionEvent.obtain(event);
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            nestedYOffset = 0;
        }
        motionEvent.offsetLocation(0, nestedYOffset);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                if ((isBeingDragged = !scroller.isFinished())) {
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }
                if (!scroller.isFinished()) {
                    scroller.abortAnimation();
                }

                lastTouchY = (int) event.getY();
                activePointerId = event.getPointerId(0);
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                int activePointerIndex = event.findPointerIndex(activePointerId);
                if (activePointerIndex == -1) {
                    break;
                }
                final int y = (int) event.getY(activePointerIndex);
                int diffY = lastTouchY - y;
                if (dispatchNestedPreScroll(0, diffY, scrollConsumed, scrollOffset)) {
                    diffY -= scrollConsumed[1];
                    motionEvent.offsetLocation(0, scrollOffset[1]);
                    nestedYOffset += scrollOffset[1];
                }
                if (!isBeingDragged && Math.abs(diffY) > touchSlop) {
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    isBeingDragged = true;
                    if (diffY > 0) {
                        diffY -= touchSlop;
                    } else {
                        diffY += touchSlop;
                    }
                }
                if (isBeingDragged) {
                    lastTouchY = y - scrollOffset[1];
                    final int oldY = getScrollY();
                    if (overScroll(diffY, getScrollY(), scrollRange) && !hasNestedScrollingParent()) {
                        velocityTracker.clear();
                    }
                    final int scrolledDeltaY = getScrollY() - oldY;
                    final int unconsumedY = diffY - scrolledDeltaY;
                    if (dispatchNestedScroll(0, scrolledDeltaY, 0, unconsumedY, scrollOffset)) {
                        lastTouchY -= scrollOffset[1];
                        motionEvent.offsetLocation(0, scrollOffset[1]);
                        nestedYOffset += scrollOffset[1];
                    }
                }
            }
            break;
            case MotionEvent.ACTION_UP: {
                velocityTracker.computeCurrentVelocity(1000, maximumFlingVelocity);
                int velocityY = (int) velocityTracker.getYVelocity(activePointerId);
                if ((Math.abs(velocityY) > minimumFlingVelocity)) {
                    flingWithNestedDispatch(-velocityY);
                }
//                else if (scroller.springBack(getScrollX(), getScrollY(), 0, 0, 0, scrollRange)) {
//                    ViewCompat.postInvalidateOnAnimation(this);
//                }
                activePointerId = INVALID_POINTER;
                isBeingDragged = false;
                recycleVelocityTracker();
                stopNestedScroll();
            }
            break;
            case MotionEvent.ACTION_CANCEL: {
//                if (isBeingDragged && scroller.springBack(getScrollX(), getScrollY(), 0, 0, 0, scrollRange)) {
//                    ViewCompat.postInvalidateOnAnimation(this);
//                }
                activePointerId = INVALID_POINTER;
                isBeingDragged = false;
                recycleVelocityTracker();
                stopNestedScroll();
            }
            break;
            case MotionEvent.ACTION_POINTER_DOWN: {
                final int index = event.getActionIndex();
                lastTouchY = (int) event.getY(index);
                activePointerId = event.getPointerId(index);
            }
            break;
            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = event.getActionIndex();
                final int pointerId = event.getPointerId(pointerIndex);
                if (pointerId == activePointerId) {
                    int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    lastTouchY = (int) event.getY(newPointerIndex);
                    activePointerId = event.getPointerId(newPointerIndex);
                    if (velocityTracker != null) {
                        velocityTracker.clear();
                    }
                }
                lastTouchY = (int) event.getY(event.findPointerIndex(activePointerId));
            }
            break;
        }
        if (velocityTracker != null) {
            velocityTracker.addMovement(motionEvent);
        }
        motionEvent.recycle();
        return true;
    }

    private void flingWithNestedDispatch(int velocityY) {
        final int scrollY = getScrollY();
        final boolean canFling = (scrollY > 0 || velocityY > 0)
                && (scrollY < scrollY || velocityY < 0);
        if (!dispatchNestedPreFling(0, velocityY)) {
            dispatchNestedFling(0, velocityY, canFling);
            fling(velocityY);
        }
    }

    private void fling(int velocityY) {
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
        scroller.fling(0, getScrollY(), // start
                0, velocityY, // velocities
                0, 0, // x
                Integer.MIN_VALUE, Integer.MAX_VALUE, // y
                0, 0); // overscroll
        lastScrollerY = getScrollY();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    private boolean overScroll(int diffY, int scrollY, int scrollRange) {
        int newScrollY = diffY + scrollY;
        boolean clampedY = false;
        if (newScrollY > scrollRange) {
            newScrollY = scrollRange;
            clampedY = true;
        } else if (newScrollY < 0) {
            newScrollY = 0;
            clampedY = true;
        }
//        if (clampedY && !hasNestedScrollingParent()) {
//            scroller.springBack(0, newScrollY, 0, 0, 0, scrollY);
//        }
        scrollTo(0, newScrollY);
        return clampedY;
    }

    private void initOrResetVelocityTracker() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        } else {
            velocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            nestedScroller.forceFinished(true);
            scroller.forceFinished(true);
            ViewCompat.postInvalidateOnAnimation(this);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        childHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return childHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return childHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        childHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return childHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow) {
        return childHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed, @Nullable int[] offsetInWindow) {
        return childHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return childHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return childHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public int getNestedScrollAxes() {
        return parentHelper.getNestedScrollAxes();
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return nestedScrollAxes == SCROLL_AXIS_VERTICAL ? true : false;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        parentHelper.onNestedScrollAccepted(child, target, axes);
        childHelper.startNestedScroll(axes);
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
        if (dy > 0 && getScrollY() < scrollRange) {  //向上滑动
            if (getScrollY() + dy > scrollRange) {
                dy = scrollRange - getScrollY();
            }
            scrollBy(0, dy);
            consumed[1] = dy;
        } else if (dy < 0 && getScrollY() > 0) {  //向下滑动
            if (target instanceof NestedScrollingChild) {
                int childScrollY = getVerticalScrollOffset(target);
                if (childScrollY <= 0) {
                    if (getScrollY() + dy < 0) {
                        dy = -getScrollY();
                    }
                    scrollBy(0, dy);
                    consumed[1] = dy;
                }
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

    private boolean scrollSelfIsConsumed(View target, float velocityY) {
        if (Math.abs(velocityY) >= minimumFlingVelocity) {
            //滑动速度
            int yVelocity = (int) (Math.abs(velocityY) <= maximumFlingVelocity ? velocityY : (velocityY > 0 ? maximumFlingVelocity : -maximumFlingVelocity));
            //联动子View的偏移量
            int childScrollOffset = getVerticalScrollOffset(target);
            //自身偏移量
            int scrollOffset = computeVerticalScrollOffset();
            //自身最大偏移量
            int maxScrollOffset = scrollRange;
            //自身可滑动剩余量
            int haveScrollOffset = maxScrollOffset - scrollOffset;
            if ((yVelocity > 0 && haveScrollOffset > 0) || (yVelocity < 0 && haveScrollOffset < maxScrollOffset)) {
                nestedScroller.fling(yVelocity, childScrollOffset, scrollOffset, maxScrollOffset);
                ViewCompat.postInvalidateOnAnimation(this);
                if (nestedScroller.isUpScroll() && nestedScroller.getFinalY() <= nestedScroller.getScrollFianl()) {  //向上滑动如果惯性偏移量小于本身可滑动的最大偏移量,则禁止联动子View滑动
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取联动目标View滚动距离
     * @param target
     * @return
     */
    private int getVerticalScrollOffset(View target) {
        if (nestedTarget == null) {
            nestedTarget = getNestedTarget(target);
            if (nestedTarget != null) {
                nestedTarget.setTag(target);
            }
        } else {
            View tag = (View) nestedTarget.getTag();
            if (!tag.equals(target)) {
                nestedTarget = getNestedTarget(target);
                if (nestedTarget != null) {
                    nestedTarget.setTag(target);
                }
            }
        }
        if (nestedTarget == null) {
            return 0;
        }
        int scrollY = nestedTarget.getScrollY();
        if (nestedTarget instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) nestedTarget;
            scrollY = recyclerView.computeVerticalScrollOffset();
        }
        return scrollY;
    }

    /**
     * 获取联动目标View
     * @return
     */
    private View getNestedTarget(View target) {
        if (target instanceof SmartRefreshLayout) {
            SmartRefreshLayout smartRefreshLayout = (SmartRefreshLayout) target;
            for (int i = 0; i < smartRefreshLayout.getChildCount(); i++) {
                View child = smartRefreshLayout.getChildAt(i);
                if (child instanceof NestedScrollingChild) {
                    View nestedTarget = getNestedTarget(child);
                    if (nestedTarget != null) {
                        return nestedTarget;
                    }
                }
            }
            return target;
        } else if (target instanceof NestedScrollingChild) {
            return target;
        } else if (target instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) target;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                View nestedTarget = getNestedTarget(child);
                if (nestedTarget != null) {
                    return nestedTarget;
                }
            }
        }
        return null;
    }

    /**
     * 设置吸引子View的偏移量
     * @param ceilingOffset
     */
    public void setCeilingOffset(int ceilingOffset) {
        this.ceilingOffset = ceilingOffset;
        requestLayout();
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
     * 设置滚动监听
     * @param scrollListener
     */
    public void setScrollListener(ScrollListener scrollListener) {
        this.scrollListener = scrollListener;
    }

    /**
     * 惯性联动Scroller
     */
    public class NestedScroller extends OverScroller {
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

    public interface ScrollListener {
        /**
         * 滚动
         * @param scrollY 滚动距离
         */
        void onScroll(int scrollY);
    }
}
