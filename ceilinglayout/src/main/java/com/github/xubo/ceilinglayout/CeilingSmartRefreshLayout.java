package com.github.xubo.ceilinglayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;

/**
 * Author：xubo
 * Time：2019-03-20
 * Description：SmartRefreshLayout的兼容扩展
 */
public class CeilingSmartRefreshLayout extends SmartRefreshLayout {
    public CeilingSmartRefreshLayout(Context context) {
        super(context);
    }

    public CeilingSmartRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CeilingSmartRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public View getRefreshContentView() {
        return mRefreshContent.getView();
    }
}
