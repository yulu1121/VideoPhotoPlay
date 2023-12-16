package com.anssy.videophotoplay;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import com.anssy.videophotoplay.videoplay.Advance;
import com.anssy.videophotoplay.videoplay.AdvanceView;
import com.jaeger.library.StatusBarUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private AdvanceView mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StatusBarUtil.setTranslucentForImageView(this,0,null);
        initView();
        initData();
        //模拟刷新数据,自行放开
         new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                data.clear();
                //https://t7.baidu.com/it/u=1956604245,3662848045&fm=193&f=GIF
                Advance advance = new Advance(toURLString("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4").trim(),"1",0);
                data.add(advance);
                mViewPager.setData(data);
            }
        },20000);
    }

    private final List<Advance> data = new ArrayList<>();
    private void initData(){
        data.clear();
        //https://t7.baidu.com/it/u=1956604245,3662848045&fm=193&f=GIF
        Advance advance1= new Advance(toURLString("https://t7.baidu.com/it/u=1956604245,3662848045&fm=193&f=GIF").trim(),"2",10);
        data.add(advance1);
        Advance advance = new Advance(toURLString("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4").trim(),"1",0);
        data.add(advance);
//        Advance advance2 = new Advance("http://vjs.zencdn.net/v/oceans.mp4","1",0);
//        data.add(advance2);
        Advance advance3 = new Advance(toURLString("https://t7.baidu.com/it/u=1415984692,3889465312&fm=193&f=GIF").trim(),"2",5);
        data.add(advance3);
        mViewPager.setData(data);
    }
    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        mViewPager = findViewById(R.id.home_vp);
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

    }


    /**
     * @param str 解决中文路径
     * @return
     */
    public static String toURLString(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char charAt = str.charAt(i);
            if (charAt > 255) {
                try {
                    sb.append(URLEncoder.encode(String.valueOf(charAt), "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                sb.append(charAt);
            }
        }
        return sb.toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViewPager.setResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mViewPager.setPause();
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewPager.setDestroy();
    }
}