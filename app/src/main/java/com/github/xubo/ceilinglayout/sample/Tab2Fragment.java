package com.github.xubo.ceilinglayout.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Author：xubo
 * Time：2019-03-21
 * Description：
 */
public class Tab2Fragment extends Fragment {
    ImageView tab2_image;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab2, null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView(getView());
        tab2_image.getLayoutParams().height = Utils.getScreenWidth(getContext()) * 3624 / 640;
    }

    private void initView(View view) {
        tab2_image = view.findViewById(R.id.tab2_image);
    }

}
