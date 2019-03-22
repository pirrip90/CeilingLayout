package com.github.xubo.ceilinglayout.sample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Author：xubo
 * Time：2019-03-21
 * Description：
 */
public class Tab1Fragment extends Fragment {
    RecyclerView tab2_rv;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab1, null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView(getView());
        final MyAdapter myAdapter = new MyAdapter(getContext());
        tab2_rv.setAdapter(myAdapter);
        tab2_rv.setLayoutManager(new LinearLayoutManager(getContext()));
        final int dividerHeight = (int) Utils.dpTopx(getContext(), 0.5f);
        final Paint dividerPaint = new Paint();
        dividerPaint.setColor(Color.parseColor("#EEEEEE"));
        tab2_rv.addItemDecoration(new RecyclerView.ItemDecoration() {

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                int position = parent.getChildAdapterPosition(view);
                if (position == myAdapter.getItemCount() - 1) {
                    outRect.set(0, 0, 0, 0);
                } else {
                    outRect.set(0, 0, 0, dividerHeight);
                }
            }

            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                super.onDraw(c, parent, state);
                int childSize = parent.getChildCount();
                for (int i = 0; i < childSize; i++) {
                    View child = parent.getChildAt(i);
                    c.drawRect(0, child.getBottom(), child.getRight(), child.getBottom() + dividerHeight, dividerPaint);
                }
            }
        });
    }

    private void initView(View view) {
        tab2_rv = view.findViewById(R.id.tab2_rv);
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.Holder> {
        LayoutInflater inflater;

        public MyAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = inflater.inflate(R.layout.fragment_tab1_item, viewGroup, false);
            return new MyAdapter.Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int i) {
            holder.tab2_item_tv.setText("item索引" + i);
        }

        @Override
        public int getItemCount() {
            return 30;
        }

        public class Holder extends RecyclerView.ViewHolder {
            public TextView tab2_item_tv;

            public Holder(@NonNull View itemView) {
                super(itemView);
                tab2_item_tv = itemView.findViewById(R.id.tab2_item_tv);
            }
        }
    }
}
