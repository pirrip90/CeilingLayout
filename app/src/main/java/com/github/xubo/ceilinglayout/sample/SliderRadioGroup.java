package com.github.xubo.ceilinglayout.sample;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Scroller;

import java.util.HashMap;
import java.util.Map;

/**
 * Author：xubo
 * Time：2019-03-20
 * Description：带滑块的RadioGroup
 */

public class SliderRadioGroup extends RadioGroup {
    /**
     * RadioButton宽度参考策略
     */
    public enum ChildWidthRefer {
        /** RadioButton文本大小为参考值 */
        TEXT,
        /** 整个RadioButton大小为参考值 */
        MATCH
    }

    /**
     * 滑块位置策略
     */
    public enum SliderGravity {
        /** 底部剩余空间居中 */
        CENTER,
        /** 下对齐 */
        BOTTOM
    }

    /** 默认滑块颜色 */
    private static final int SLIDER_COLOR_DEFAULT = Color.parseColor("#FF6D32");
    /** 默认滑块宽度所占比重 */
    private static final float SLIDER_WIDTH_WEIGHT_DEFAULT = 0.8f;
    /** 默认底部线高 */
    private static final float BOTTOM_LINE_HEIGHT_DEFAULT = 0.0f;
    /** 默认底部线颜色 */
    private static final int BOTTOM_LINE_COLOR_DEFAULT = Color.parseColor("#EEEEEE");

    /** child最小宽度 */
    private int minChildWidth;
    /** child最大高度 */
    private int maxChildHeight;

    /** 滑块区域 */
    private RectF sliderRectF;
    /** 滑块宽度所占比重(相对child的最小宽度) */
    private float slideWidthWeight;
    /** 滑块宽度 */
    private float slideWidth;
    /** 滑块高度 */
    private float slideHeight;
    /** 滑块圆角 */
    private float slideRound;
    /** 滑块颜色 */
    private int sliderColor;
    /** 滑块底部位置 */
    private float slideBottom;
    /** 滑块底部位置边距(只有是滑块位置策略在底部时才有效) */
    private float slideBottomMagin;
    /** 底部线区域 */
    private RectF bottomLineRectF;
    /** 底部线的高度 */
    private float bottomLineHeight;
    /** 底部线的颜色 */
    private int bottomLineColor;

    /** child宽度参考策略 */
    private ChildWidthRefer childWidthRefer;
    /** 滑块位置策略 */
    private SliderGravity sliderGravity;

    /** 选中索引 */
    private int selectIndex;

    /** 所有radiobutton的左边位置 */
    Map<Integer, Integer> childLeftMap = new HashMap<Integer, Integer>();
    /** 所有radiobutton的宽度 */
    Map<Integer, Integer> childWidthMap = new HashMap<Integer, Integer>();

    private TextPaint textPaint;
    private Paint sliderPaint;
    private Paint bottomLinePaint;
    private Scroller scroller;
    private Handler handler = new Handler();


    public SliderRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SliderRadioGroup, 0, 0);
        slideRound = typedArray.getDimension(R.styleable.SliderRadioGroup_slider_round, Utils.dpTopx(context, 1.0f));
        slideHeight = typedArray.getDimension(R.styleable.SliderRadioGroup_slider_height, Utils.dpTopx(context, 2.0f));
        sliderColor = typedArray.getColor(R.styleable.SliderRadioGroup_slider_color, SLIDER_COLOR_DEFAULT);
        slideWidthWeight = typedArray.getFloat(R.styleable.SliderRadioGroup_slider_width_weight, SLIDER_WIDTH_WEIGHT_DEFAULT);
        if (slideWidthWeight > 1.0 || slideWidthWeight < 0.0f) {
            slideWidthWeight = SLIDER_WIDTH_WEIGHT_DEFAULT;
        }
        bottomLineHeight = typedArray.getDimension(R.styleable.SliderRadioGroup_bottom_line_height, BOTTOM_LINE_HEIGHT_DEFAULT);
        bottomLineColor = typedArray.getColor(R.styleable.SliderRadioGroup_bottom_line_color, BOTTOM_LINE_COLOR_DEFAULT);
        typedArray.recycle();
        init(context);
    }

    private void init(Context context) {
        sliderRectF = new RectF();
        bottomLineRectF = new RectF();

        childWidthRefer = ChildWidthRefer.TEXT;
        sliderGravity = SliderGravity.BOTTOM;

        textPaint = new TextPaint();
        sliderPaint = new Paint();
        sliderPaint.setAntiAlias(true);
        sliderPaint.setColor(sliderColor);
        bottomLinePaint = new Paint();
        bottomLinePaint.setAntiAlias(true);
        bottomLinePaint.setColor(bottomLineColor);

        selectIndex = 0;
        scroller = new Scroller(context, new DecelerateInterpolator());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            if (childView instanceof RadioButton) {
                RadioButton radioButton = (RadioButton) childView;
                if (radioButton.isChecked()) {
                    selectIndex = i;
                    break;
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int childCount = getChildCount();
        if (childCount == 0) {
            return;
        }
        childLeftMap.clear();
        childWidthMap.clear();
        int currentIndexChildLeft = getPaddingLeft();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            int childWidth = childView.getMeasuredWidth();
            int childHeight = 0;
            childWidthMap.put(i, childWidth);
            RadioGroup.LayoutParams childParams = (LayoutParams) childView.getLayoutParams();
            childLeftMap.put(i, currentIndexChildLeft + childParams.leftMargin);
            currentIndexChildLeft += (childParams.leftMargin + childWidth + childParams.rightMargin);
            if (childView instanceof RadioButton) {
                RadioButton radioButton = (RadioButton) childView;
                textPaint.setTextSize(radioButton.getTextSize());
                childHeight = Utils.getTextHeightForInt(textPaint);
                if (childWidthRefer == ChildWidthRefer.TEXT) {
                    childWidth = Utils.getTextWidth(radioButton.getText().toString(), textPaint);
                }
            }
            if (minChildWidth == 0) {
                minChildWidth = childWidth;
            }
            minChildWidth = Math.min(childWidth, minChildWidth);
            maxChildHeight = Math.max(childHeight, maxChildHeight);
        }
        slideWidth = minChildWidth * slideWidthWeight;

        float remainingHeight = (getMeasuredHeight() - maxChildHeight) / 2f;
        if (sliderGravity == SliderGravity.CENTER) {
            if (remainingHeight > slideHeight + bottomLineHeight) {
                slideBottom = getMeasuredHeight() - (remainingHeight - slideHeight - bottomLineHeight) / 2 - bottomLineHeight;
            } else {
                slideBottom = -1;
            }
        } else {
            if (remainingHeight > slideHeight + bottomLineHeight + slideBottomMagin) {
                slideBottom = getMeasuredHeight() - bottomLineHeight - slideBottomMagin;
            } else {
                slideBottom = -1;
            }
        }
        if (slideBottom > 0) {
            int childLeft = childLeftMap.get(selectIndex);
            int childWidth = childWidthMap.get(selectIndex);
            float slideLeft = childLeft + (childWidth - slideWidth) / 2.0f;
            float slideRight = slideLeft + slideWidth;
            float slideTop = slideBottom - slideHeight;
            sliderRectF.set(slideLeft, slideTop, slideRight, slideBottom);
        } else {
            sliderRectF.set(0, 0, 0, 0);
        }
        bottomLineRectF.set(0, getMeasuredHeight() - bottomLineHeight, getMeasuredWidth(), getMeasuredHeight());
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        canvas.drawRoundRect(sliderRectF, slideRound, slideRound, sliderPaint);
        if (bottomLineHeight > 0) {
            canvas.drawRect(bottomLineRectF, bottomLinePaint);
        }
    }

    /**
     * ViewPage联动
     * @param selectIndex
     * @param movePercentage
     */
    public void move(int selectIndex, float movePercentage) {
        this.selectIndex = selectIndex;
        if (slideHeight > 0 && slideBottom > 0 && childLeftMap.size() > 0 && childWidthMap.size() > 0) {
            float slideLeft = 0;

            int childLeft = childLeftMap.get(selectIndex);
            int childWidth = childWidthMap.get(selectIndex);
            float startX = childLeft + (childWidth - slideWidth) / 2.0f;

            float stopLeftX = 0;
            if (selectIndex > 0) {
                int leftChildLeft = childLeftMap.get(selectIndex - 1);
                int leftChildWidth = childWidthMap.get(selectIndex - 1);
                stopLeftX = leftChildLeft + (leftChildWidth - slideWidth) / 2.0f;
            }
            float stopRightX = 0;
            if (selectIndex < childLeftMap.size() - 1) {
                int rightChildLeft = childLeftMap.get(selectIndex + 1);
                int rightChildWidth = childWidthMap.get(selectIndex + 1);
                stopRightX = rightChildLeft + (rightChildWidth - slideWidth) / 2.0f;
            }

            if (movePercentage > 0) {  //往右滑动
                slideLeft = startX + Math.abs(stopRightX - startX) * movePercentage;
            } else {  //往左滑动
                slideLeft = startX + Math.abs(stopLeftX - startX) * movePercentage;
            }

            float slideRight = slideLeft + slideWidth;
            float slideTop = slideBottom - slideHeight;
            sliderRectF.set(slideLeft, slideTop, slideRight, slideBottom);
        }
        invalidate();
    }

    /**
     * 选择索引
     * @param index
     */
    public void checkIndex(int index) {
        if (selectIndex < 0 || selectIndex > getChildCount() - 1) {
            return;
        }
        if (selectIndex != index) {
            if (scroller.computeScrollOffset()) {
                scroller.abortAnimation();
                float slideLeft = scroller.getFinalX();
                float slideRight = slideLeft + slideWidth;
                float slideTop = slideBottom - slideHeight;
                sliderRectF.set(slideLeft, slideTop, slideRight, slideBottom);
                invalidate();
            }
            int startChildLeft = childLeftMap.get(selectIndex);
            int startChildWidth = childWidthMap.get(selectIndex);
            int startX = (int) (startChildLeft + (startChildWidth - slideWidth) / 2);
            int stopChildLeft = childLeftMap.get(index);
            int stopChildWidth = childWidthMap.get(index);
            int stopX = (int) (stopChildLeft + (stopChildWidth - slideWidth) / 2);
            int dx = stopX - startX;
            scroller.startScroll(startX, 0, dx, 0, 200);
            this.selectIndex = index;
            refresh();
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (scroller.computeScrollOffset()) {
                float slideLeft = scroller.getCurrX();
                float slideRight = slideLeft + slideWidth;
                float slideTop = slideBottom - slideHeight;
                sliderRectF.set(slideLeft, slideTop, slideRight, slideBottom);
                invalidate();
                refresh();
            }
        }
    };

    private void refresh() {
        handler.post(runnable);
    }

    /**
     * 设置child宽度参考策略
     * @param childWidthRefer
     */
    public void setChildWidthRefer(ChildWidthRefer childWidthRefer) {
        this.childWidthRefer = childWidthRefer;
    }

    /**
     * 设置滑块位置策略
     * @param sliderGravity
     */
    public void setSliderGravity(SliderGravity sliderGravity) {
        this.sliderGravity = sliderGravity;
    }

    /**
     * 滑块位置策略在底部时距离底部的边距(居中策略将无效)
     * @param slideBottomMagin
     */
    public void setSlideBottomMagin(float slideBottomMagin) {
        this.slideBottomMagin = slideBottomMagin;
    }
}
