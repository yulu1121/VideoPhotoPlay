package com.github.videophotoplay.videoplay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;

public class AdvanceView extends RelativeLayout {
    private ViewPager viewPager;
    private List<View> views = new ArrayList<>();
    public AdvancePagerAdapter adapter;
 
    public AdvanceView(Context context) {
        super(context);
        initView();
    }
 
    public AdvanceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }
 
    public AdvanceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
 
    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        viewPager = new ViewPager(getContext());
        viewPager.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        adapter = new AdvancePagerAdapter(getContext(),viewPager);
        viewPager.setAdapter(adapter);
        addView(viewPager, new LayoutParams(-1, -1));
    }
 
    public void setData(List<Advance> advances) {
        adapter.setData(advances);
    }
    public void setDestroy(){
        adapter.setDestroy();
    }
    public void setPause(){
        adapter.setPause();
    }
    public void setResume(){
        adapter.setResume();
    }
}