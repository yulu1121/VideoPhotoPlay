package com.github.videophotoplay.videoplay;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.github.videophotoplay.utils.WeakHandler;
import com.github.videophotoplay.utils.videocache.VideoProxyCacheManager;

import java.util.ArrayList;
import java.util.List;

import xyz.doikki.videoplayer.player.BaseVideoView;
import xyz.doikki.videoplayer.player.VideoView;

public class AdvancePagerAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener,BaseVideoView.OnStateChangeListener {
    private Context context;
    private ViewPager viewPager;
    private List<Advance> datas;
    private List<View> list = new ArrayList<>();
 
    private int current = 0;
    private int time = 5000;
    private boolean pause;
    private Thread thread;

    private int lastPosition = -1;
    private final VideoView videoView;
   // private final PreloadManager mPreloadManager;
    public AdvancePagerAdapter(Context context, ViewPager viewPager) {
        this.context = context;
        //mPreloadManager = PreloadManager.getInstance(context);
        videoView = new VideoView(context);
        // 清空DNS,有时因为在APP里面要播放多种类型的视频(如:MP4,直播,直播平台保存的视频,和其他http视频), 有时会造成因为DNS的问题而报10000问题的
        //videoView.addFormatOption( "dns_cache_clear", 1);
        videoView.addOnStateChangeListener(this);
        this.viewPager = viewPager;
    }
    private boolean isRunning = false;

    public void setData(List<Advance> advances) {

        if (advances.size() == 0) return;
//        for (int i = 0; i < advances.size(); i++) {
//            if (advances.get(i).type.equals("1")){
//                VideoProxyCacheManager.getInstance().startRequestVideoInfo(advances.get(i).path, null, null);
//                VideoProxyCacheManager.getInstance().addCacheListener(advances.get(i).path,this);
//                VideoProxyCacheManager.getInstance().setPlayingUrlMd5(ProxyCacheUtils.computeMD5(advances.get(i).path));
//                //mPreloadManager.addPreloadTask(advances.get(i).path,i);
//            }
//        }
        if (isRunning){
            if (list.get(viewPager.getCurrentItem()) instanceof AdvanceVideoView){
                ((AdvanceVideoView) list.get(viewPager.getCurrentItem())).setDestroy();
                ((AdvanceVideoView) list.get(viewPager.getCurrentItem())).currentPosition = 0;
            }
            stopTimer();
        }else {
            viewPager.addOnPageChangeListener(this);
        }
        viewPager.removeAllViews();
        this.datas = advances;
        list.clear();
        addView(advances.get(advances.size() - 1));
        if (advances.size() > 1) { //多于1个要循环
            for (Advance d : advances) { //中间的N个（index:1~N）
                addView(d);
            }
            addView(advances.get(0));
        }
        notifyDataSetChanged();
        //在外层，将mViewPager初始位置设置为1即可
        if (advances.get(0).type.equals("1")) {//有人反应第一个是视频不播放这边优化了一下
            if (list.size()>1){
                ((AdvanceVideoView) list.get(1)).setVideo();
            }else {
                ((AdvanceVideoView) list.get(0)).setVideo();
            }
        }else {
            time = advances.get(0).getPlaySecond()*1000;
        }
        if (advances.size() > 1) { //多于1个，才循环并开启定时器
            viewPager.setCurrentItem(1);
            startNewTimer();
        }
    }
 
    private void addView(Advance advance) {
        if (advance.type.equals("1")) {
            AdvanceVideoView videoView = new AdvanceVideoView(context);
            videoView.setImage(advance.path,this.videoView);
            list.add(videoView);
        } else {
            AdvanceImageView imageView = new AdvanceImageView(context);
            imageView.setImage(advance.path);
            list.add(imageView);
        }
    }
    private int index = 0;//防止缓存时间过长导致广告机卡死
    private final WeakHandler weakHandler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what==22){
                index++;
                if (index==20){
                    index = 0;
                    VideoProxyCacheManager.getInstance().pauseCacheTask(datas.get(viewPager.getCurrentItem()-1).path);
                    weakHandler.removeCallbacks(mTimePauseRun);
                    viewPager.setCurrentItem(viewPager.getCurrentItem()+1,true);
                }
            }
            return true;
        }
    });
    public void startNewTimer(){
        isRunning = true;
        weakHandler.post(timeRunnable);
    }

    private void stopTimer(){
        isRunning = false;
        current = 0;
        lastPosition = -1;
        weakHandler.removeCallbacks(timeRunnable);
    }

    private final Runnable timeRunnable = new Runnable() {
        @Override
        public void run() {
            if (!pause && !(list.get(viewPager.getCurrentItem()) instanceof AdvanceVideoView)){
                current+=1000;
            }
            if (current >= time) {
                viewPager.post(() -> viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true));
                current = 0;
            }
            weakHandler.postDelayed(this,1000);
        }
    };

    private void startTimer() {
        if (thread != null && !thread.isInterrupted()) {
            thread.interrupt();
            thread = null;
        }
        thread = new Thread(() -> {
            while (thread != null && !thread.isInterrupted()) {
                try {
                    Thread.sleep(1000);
                    if (!pause && !(list.get(viewPager.getCurrentItem()) instanceof AdvanceVideoView))
                        current += 1000;
                    if (current >= time) {
                        viewPager.post(() -> viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true));
                        current = 0;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
 
    @Override
    public int getCount() {
        return list.size();
    }
 
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
 
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (position<list.size()){
            container.removeView(list.get(position));
        }
    }
 
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = list.get(position);
        container.addView(view);
        return view;
    }
 
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
 
    //
//    // 实现ViewPager.OnPageChangeListener接口
    @Override
    public void onPageSelected(int position) {

    }
 
    @Override
    public void onPageScrolled(int position, float positionOffset,
                               int positionOffsetPixels) {
        // 什么都不干
    }


    @Override
    public void onPageScrollStateChanged(int state) {
        // 由于viewpager的预加载机制onPageSelected这里面加载videoview 放的跟玩一样  等操作完成后再播放videoview就香了  很丝滑
        if (state == 0) {
            if (list.size() > 1) { //多于1，才会循环跳转
                if (lastPosition != -1 && lastPosition != viewPager.getCurrentItem() && list.get(lastPosition) instanceof AdvanceVideoView) {
                    ((AdvanceVideoView) list.get(lastPosition)).setPause();
                }
                if (viewPager.getCurrentItem() < 1) { //首位之前，跳转到末尾（N）
                    int position = datas.size(); //注意这里是mList，而不是mViews
                    viewPager.setCurrentItem(position, false);
                } else if (viewPager.getCurrentItem() > datas.size()) { //末位之后，跳转到首位（1）
                    viewPager.setCurrentItem(1, false); //false:不显示跳转过程的动画
                }
                current = 0;//换页重新计算时间
                if (list.get(viewPager.getCurrentItem()) instanceof AdvanceVideoView) {
                    ((AdvanceVideoView) list.get(viewPager.getCurrentItem())).setVideo();
                }else {
                    time = datas.get(viewPager.getCurrentItem()-1).getPlaySecond()*1000;
                }
                lastPosition = viewPager.getCurrentItem();
            }
        }
    }
    public void setDestroy(){
        pause = true;
        if (list.size() > 0 && list.get(viewPager.getCurrentItem()) instanceof AdvanceVideoView) {
            ((AdvanceVideoView) list.get(viewPager.getCurrentItem())).setDestroy();
            Log.e("调用销毁", " destroy");
        }
        weakHandler.removeCallbacksAndMessages(null);
    }
    public void setPause() {
        pause = true;
        if (list.size() > 0 && list.get(viewPager.getCurrentItem()) instanceof AdvanceVideoView) {
            ((AdvanceVideoView) list.get(viewPager.getCurrentItem())).setPause();
            Log.e("调用暂停", " pause");
        }
    }
 
    public void setResume() {
        pause = false;
        if (list.size() > 0 && list.get(viewPager.getCurrentItem()) instanceof AdvanceVideoView) {
            ((AdvanceVideoView) list.get(viewPager.getCurrentItem())).setRestart();
            Log.e("调用start", " start");
        }
    }

    @Override
    public void onPlayerStateChanged(int playerState) {

    }

    private final Runnable mTimePauseRun = new Runnable() {
        @Override
        public void run() {
             weakHandler.sendEmptyMessage(22);
             weakHandler.postDelayed(this,1000);
        }
    };

    @Override
    public void onPlayStateChanged(int playState) {
        if (playState== VideoView.STATE_PLAYBACK_COMPLETED){
            if (list.get(viewPager.getCurrentItem())instanceof AdvanceVideoView){
                ((AdvanceVideoView) list.get(viewPager.getCurrentItem())).currentPosition = 0;
                if ( ((AdvanceVideoView) list.get(viewPager.getCurrentItem())).imageView!=null){
                    ((AdvanceVideoView) list.get(viewPager.getCurrentItem())).imageView.setVisibility(View.VISIBLE);
                }
            }
            if (list.size()>1){
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
            }else {
                if (list.get(viewPager.getCurrentItem())instanceof AdvanceVideoView){
                    ((AdvanceVideoView) list.get(viewPager.getCurrentItem())).setVideo();
                }
            }
        }else if (playState==VideoView.STATE_PLAYING){
            if (list.get(viewPager.getCurrentItem())instanceof AdvanceVideoView){
                if ( ((AdvanceVideoView) list.get(viewPager.getCurrentItem())).imageView!=null){
                    ((AdvanceVideoView) list.get(viewPager.getCurrentItem())).imageView.setVisibility(View.GONE);
                }
            }
        }else if (playState==VideoView.STATE_ERROR){
            VideoProxyCacheManager.getInstance().pauseCacheTask(datas.get(viewPager.getCurrentItem()-1).path);
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
        }else if (playState==VideoView.STATE_BUFFERING){
            weakHandler.post(mTimePauseRun);
        } else if (playState==VideoView.STATE_BUFFERED) {
            index = 0;
            weakHandler.removeCallbacks(mTimePauseRun);
        }
    }

}