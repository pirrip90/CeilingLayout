package com.github.xubo.ceilinglayout.sample;

import android.graphics.Color;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class MainActivity extends AppCompatActivity {
    FragmentManager fragmentManager;
    ImageView main_image_iv;
    SliderRadioGroup main_srg;
    RadioButton main_tab1_rb;
    RadioButton main_tab2_rb;
    ViewPager main_content_vp;

    MyPagerAdapter myPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Utils.setColor(this, Color.parseColor("#303F9F"));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        fragmentManager = getSupportFragmentManager();

        //id
        main_image_iv = findViewById(R.id.main_image_iv);
        main_srg = findViewById(R.id.main_srg);
        main_tab1_rb = findViewById(R.id.main_tab1_rb);
        main_tab2_rb = findViewById(R.id.main_tab2_rb);
        main_content_vp = findViewById(R.id.main_content_vp);

        main_image_iv.getLayoutParams().height = Utils.getScreenWidth(this) * 540 / 1920;

        myPagerAdapter = new MyPagerAdapter(fragmentManager);
        main_content_vp.setAdapter(myPagerAdapter);
        main_content_vp.setOffscreenPageLimit(2);
        main_content_vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                main_srg.move(position, positionOffset);
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        main_tab1_rb.setChecked(true);
                        break;
                    case 1:
                        main_tab2_rb.setChecked(true);
                        break;
                    default:
                        main_tab1_rb.setChecked(true);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        main_srg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.main_tab1_rb:
                        main_content_vp.setCurrentItem(0);
                        break;
                    case R.id.main_tab2_rb:
                        main_content_vp.setCurrentItem(1);
                        break;
                    default:
                        main_content_vp.setCurrentItem(0);
                        break;
                }
            }
        });
    }
}
