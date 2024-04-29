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
    private Context mContext;
    private ViewPager mViewPager;
    private List<Advance> mAdvanceList;
    private List<View> mList = new ArrayList<>();//添加的控件

    private int mCurrent = 0;// 当前图片的计时
    private int mIvTime = 5000;//图片播放时间，默认5秒
    private boolean mPause;


    private int lastPosition = -1;
    private final VideoView videoView;
   // private final PreloadManager mPreloadManager;
    public AdvancePagerAdapter(Context context, ViewPager viewPager) {
        this.mContext = context;
        //mPreloadManager = PreloadManager.getInstance(context);
        videoView = new VideoView(context);
        // 清空DNS,有时因为在APP里面要播放多种类型的视频(如:MP4,直播,直播平台保存的视频,和其他http视频), 有时会造成因为DNS的问题而报10000问题的
        //videoView.addFormatOption( "dns_cache_clear", 1);
        videoView.addOnStateChangeListener(this);
        this.mViewPager = viewPager;
    }
    private boolean isRunning = false;

    public void setData(List<Advance> advances) {

        if (advances.size() == 0) return;
        if (isRunning){
            if (mList.get(mViewPager.getCurrentItem()) instanceof AdvanceVideoView){
                ((AdvanceVideoView) mList.get(mViewPager.getCurrentItem())).setDestroy();
                ((AdvanceVideoView) mList.get(mViewPager.getCurrentItem())).currentPosition = 0;
            }
            stopTimer();
        }else {
            mViewPager.addOnPageChangeListener(this);
        }
        mViewPager.removeAllViews();
        this.mAdvanceList = advances;
        mList.clear();
        addView(advances.get(advances.size() - 1));
        if (advances.size() > 1) { //多于1个要循环
            for (Advance d : advances) { //中间的N个（index:1~N）
                addView(d);
            }
            addView(advances.get(0));
        }
        notifyDataSetChanged();
        //在外层，将mViewPager初始位置设置为1即可
        if (advances.get(0).type==MediaType.VIDEO_TYPE) {//有人反应第一个是视频不播放这边优化了一下
            if (mList.size()>1){
                ((AdvanceVideoView) mList.get(1)).setVideo();
            }else {
                ((AdvanceVideoView) mList.get(0)).setVideo();
            }
        }else {
            mIvTime = advances.get(0).getPlaySecond()*1000;
        }
        if (advances.size() > 1) { //多于1个，才循环并开启定时器
            mViewPager.setCurrentItem(1);
            startNewTimer();
        }
    }

    /**
     * 添加广告类型
     * @param advance 广告类型
     */
    private void addView(Advance advance) {
        if (advance.type==MediaType.VIDEO_TYPE) {
            AdvanceVideoView videoView = new AdvanceVideoView(mContext);
            videoView.setImage(advance.path,this.videoView);
            mList.add(videoView);
        } else {
            AdvanceImageView imageView = new AdvanceImageView(mContext);
            imageView.setImage(advance.path);
            mList.add(imageView);
        }
    }
    private int mPauseTime = 0;//防止缓存时间过长导致广告机卡死
    private final WeakHandler weakHandler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what==22){
                mPauseTime++;
                if (mPauseTime==20){
                    mPauseTime = 0;
                    VideoProxyCacheManager.getInstance().pauseCacheTask(mAdvanceList.get(mViewPager.getCurrentItem()-1).path);
                    weakHandler.removeCallbacks(mTimePauseRun);
                    mViewPager.setCurrentItem(mViewPager.getCurrentItem()+1,true);
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
        mCurrent = 0;
        lastPosition = -1;
        weakHandler.removeCallbacks(timeRunnable);
    }

    private final Runnable timeRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mPause && !(mList.get(mViewPager.getCurrentItem()) instanceof AdvanceVideoView)){
               mCurrent+=1000;
            }
            if (mCurrent >= mIvTime) {
                mViewPager.post(() -> mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true));
                mCurrent = 0;
            }
            weakHandler.postDelayed(this,1000);
        }
    };


    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (position<mList.size()){
            container.removeView(mList.get(position));
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = mList.get(position);
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
            if (mList.size() > 1) { //多于1，才会循环跳转
                if (lastPosition != -1 && lastPosition != mViewPager.getCurrentItem() && mList.get(lastPosition) instanceof AdvanceVideoView) {
                    ((AdvanceVideoView) mList.get(lastPosition)).setPause();
                }
                if (mViewPager.getCurrentItem() < 1) { //首位之前，跳转到末尾（N）
                    int position = mAdvanceList.size(); //注意这里是mList，而不是mViews
                    mViewPager.setCurrentItem(position, false);
                } else if (mViewPager.getCurrentItem() > mAdvanceList.size()) { //末位之后，跳转到首位（1）
                    mViewPager.setCurrentItem(1, false); //false:不显示跳转过程的动画
                }
                mCurrent = 0;//换页重新计算时间
                if (mList.get(mViewPager.getCurrentItem()) instanceof AdvanceVideoView) {
                    ((AdvanceVideoView) mList.get(mViewPager.getCurrentItem())).setVideo();
                }else {
                    mIvTime = mAdvanceList.get(mViewPager.getCurrentItem()-1).getPlaySecond()*1000;
                }
                lastPosition = mViewPager.getCurrentItem();
            }
        }
    }
    public void setDestroy(){
        mPause = true;
        if (mList.size() > 0 && mList.get(mViewPager.getCurrentItem()) instanceof AdvanceVideoView) {
            ((AdvanceVideoView) mList.get(mViewPager.getCurrentItem())).setDestroy();
            Log.e("调用销毁", " destroy");
        }
        weakHandler.removeCallbacksAndMessages(null);
    }
    public void setPause() {
        mPause = true;
        if (mList.size() > 0 && mList.get(mViewPager.getCurrentItem()) instanceof AdvanceVideoView) {
            ((AdvanceVideoView) mList.get(mViewPager.getCurrentItem())).setPause();
            Log.e("调用暂停", " pause");
        }
    }

    public void setResume() {
        mPause = false;
        if (mList.size() > 0 && mList.get(mViewPager.getCurrentItem()) instanceof AdvanceVideoView) {
            ((AdvanceVideoView)mList.get(mViewPager.getCurrentItem())).setRestart();
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
            if (mList.get(mViewPager.getCurrentItem())instanceof AdvanceVideoView){
                ((AdvanceVideoView) mList.get(mViewPager.getCurrentItem())).currentPosition = 0;
                if ( ((AdvanceVideoView) mList.get(mViewPager.getCurrentItem())).imageView!=null){
                    ((AdvanceVideoView) mList.get(mViewPager.getCurrentItem())).imageView.setVisibility(View.VISIBLE);
                }
            }
            if (mList.size()>1){
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
            }else {
                if (mList.get(mViewPager.getCurrentItem())instanceof AdvanceVideoView){
                    ((AdvanceVideoView) mList.get(mViewPager.getCurrentItem())).setVideo();
                }
            }
        }else if (playState==VideoView.STATE_PLAYING){
            if (mList.get(mViewPager.getCurrentItem())instanceof AdvanceVideoView){
                if ( ((AdvanceVideoView) mList.get(mViewPager.getCurrentItem())).imageView!=null){
                    ((AdvanceVideoView) mList.get(mViewPager.getCurrentItem())).imageView.setVisibility(View.GONE);
                }
            }
        }else if (playState==VideoView.STATE_ERROR){
            VideoProxyCacheManager.getInstance().pauseCacheTask(mAdvanceList.get(mViewPager.getCurrentItem()-1).path);
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
        }else if (playState==VideoView.STATE_BUFFERING){
            weakHandler.post(mTimePauseRun);
        } else if (playState==VideoView.STATE_BUFFERED) {
            mPauseTime = 0;
            weakHandler.removeCallbacks(mTimePauseRun);
        }
    }

}