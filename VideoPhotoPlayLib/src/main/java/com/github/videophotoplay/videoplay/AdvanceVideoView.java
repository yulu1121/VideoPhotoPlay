package com.github.videophotoplay.videoplay;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.anssy.videophotoplay.R;
import com.github.videophotoplay.utils.glide.GlideApp;
import com.github.videophotoplay.utils.videocache.VideoProxyCacheManager;
import com.github.videophotoplay.utils.videocache.listener.IVideoCacheListener;
import com.github.videophotoplay.utils.videocache.model.VideoCacheInfo;
import com.github.videophotoplay.utils.videocache.utils.ProxyCacheUtils;
import com.bumptech.glide.request.RequestOptions;

import xyz.doikki.videoplayer.player.VideoView;

public class AdvanceVideoView extends RelativeLayout implements IVideoCacheListener {
    public ImageView imageView;
    private VideoView videoView;
    private RelativeLayout videoRela;
    private String path;
    public int currentPosition;


    public AdvanceVideoView(Context context) {
        super(context);
        initView();
    }
 
    public AdvanceVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }
 
    public AdvanceVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    private void initView() {
        videoRela = new RelativeLayout(getContext());
        addView(videoRela, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        imageView = new ImageView(getContext());
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        addView(imageView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }
 
    public void setImage(String path,VideoView  videoView) {
        this.path = path;
        this.videoView = videoView;
        GlideApp.with(getContext()) .setDefaultRequestOptions(
                new RequestOptions()
                        .frame(0)
        ).load(path).into(imageView);
    }
    /**
     * 将View从父控件中移除
     */
    public static void removeViewFormParent(View v) {
        if (v == null) return;
        ViewParent parent = v.getParent();
        if (parent instanceof RelativeLayout) {
            ((RelativeLayout) parent).removeView(v);
        }
    }


    public void setVideo() {

        removeViewFormParent(videoView);
        videoView.release();
        videoView.setPlayerBackgroundColor(Color.parseColor("#0F1B2F"));
        videoView.setScreenScaleType(VideoView.SCREEN_SCALE_MATCH_PARENT);
        if (VideoProxyCacheManager.getInstance().isTaskPause(path)){
            VideoProxyCacheManager.getInstance().resumeCacheTask(path);
        }else {
            VideoProxyCacheManager.getInstance().startRequestVideoInfo(Uri.parse(path).toString());
        }
        VideoProxyCacheManager.getInstance().addCacheListener(path,this);
        VideoProxyCacheManager.getInstance().setPlayingUrlMd5(ProxyCacheUtils.computeMD5(path));
        String playUrl = ProxyCacheUtils.getProxyUrl(Uri.parse(path).toString(), null, null);
//        if (VideoProxyCacheManager.getInstance().isTaskPause(path)){
//            VideoProxyCacheManager.getInstance().resumeCacheTask(path);
//        }
      //  String playUrl = mPreloadManager.getPlayUrl(path);
        videoView.setUrl(playUrl);
        LayoutParams layoutParams = new LayoutParams(-1, -1);
        //设置videoview占满父view播放
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        videoRela.addView(videoView, layoutParams);
        videoView.start();
    }

    public void setDestroy(){
        if (videoView!=null){
            videoView.release();
        }
        VideoProxyCacheManager.getInstance().removeCacheListener(path);
    }

    public void setPause() {
        if (videoView != null) {
            videoView.pause();
        }
    }
 
    public void setRestart() {
        if (videoView != null) {
            videoView.resume();
        }
    }



    @Override
    public void onCacheStart(VideoCacheInfo cacheInfo) {
    }

    @Override
    public void onCacheProgress(VideoCacheInfo cacheInfo) {
        Log.e("xxx","xx"+cacheInfo.getPercent());
    }

    @Override
    public void onCacheError(VideoCacheInfo cacheInfo, int errorCode) {
        Log.e("xxx","cacheError:"+errorCode);
      //  VideoProxyCacheManager.getInstance().pauseCacheTask(cacheInfo.getVideoUrl());   //停止视频缓存任务

    }

    @Override
    public void onCacheForbidden(VideoCacheInfo cacheInfo) {

    }

    @Override
    public void onCacheFinished(VideoCacheInfo cacheInfo) {
        Log.e("xxx","xx"+cacheInfo.getSavePath());

    }

}