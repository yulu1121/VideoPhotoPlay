package com.anssy.videophotoplay.utils.videocache.task;

import androidx.annotation.NonNull;

import com.anssy.videophotoplay.utils.videocache.StorageManager;
import com.anssy.videophotoplay.utils.videocache.listener.IVideoCacheTaskListener;
import com.anssy.videophotoplay.utils.videocache.model.VideoCacheInfo;
import com.anssy.videophotoplay.utils.videocache.utils.StorageUtils;
import com.anssy.videophotoplay.utils.videocache.utils.VideoProxyThreadUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class VideoCacheTask {

    protected VideoCacheInfo mCacheInfo;
    protected Map<String, String> mHeaders;
    protected IVideoCacheTaskListener mListener;
    protected ThreadPoolExecutor mTaskExecutor;

    public volatile long mCachedSize;      //当前缓存大小
    public volatile long mLastCachedSize;  //上一次缓存大小
    protected long mTotalSize;
    protected long mLastInvokeTime;
    protected float mPercent = 0.0f;
    protected float mSpeed = 0.0f;
    protected File mSaveDir;
    public boolean isPaused;
    public boolean isCompleted;

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }

    public VideoCacheTask(VideoCacheInfo cacheInfo, Map<String, String> headers) {
        mCacheInfo = cacheInfo;
        mHeaders = headers;
        if (mHeaders == null) {
            mHeaders = new HashMap<>();
        }
        mCachedSize = cacheInfo.getCachedSize();
        mTotalSize = cacheInfo.getTotalSize();
        mSaveDir = new File(cacheInfo.getSavePath());
        if (!mSaveDir.exists()) {
            mSaveDir.mkdir();
        }
    }

    public void setTaskListener(@NonNull IVideoCacheTaskListener listener) {
        mListener = listener;
    }

    public abstract void startCacheTask();

    public abstract void pauseCacheTask();

    public abstract void stopCacheTask();

    public abstract void seekToCacheTaskFromClient(float percent);        //来自客户端的seek操作

    public abstract void seekToCacheTaskFromServer(long startPosition);   //来自服务端的seek操作,针对非M3U8视频

    public abstract void seekToCacheTaskFromServer(int segIndex);         //来自服务端的seek操作,针对M3U8视频

    public abstract void resumeCacheTask();

    protected void notifyOnTaskStart() {
        mListener.onTaskStart();
    }

    protected void notifyOnTaskFailed(Exception e) {
        StorageManager.getInstance().checkCache(mSaveDir.getAbsolutePath());
        mListener.onTaskFailed(e);
    }

    protected void notifyOnTaskCompleted() {
        StorageManager.getInstance().checkCache(mSaveDir.getAbsolutePath());
        mListener.onTaskCompleted(mTotalSize);
    }

    protected boolean isTaskRunning() {
        return mTaskExecutor != null && !mTaskExecutor.isShutdown();
    }

    protected boolean isTaskShutdown() {
        return mTaskExecutor != null && mTaskExecutor.isShutdown();
    }

    /**
     * 非M3U8视频专用的接口
     * 核心逻辑在NonM3U8CacheTask.java
     * @param startPosition
     * @return
     */
    public boolean isMp4PositionSegExisted(long startPosition) { return false; }

    public boolean isMp4Completed() { return false; }

    /**
     * position之后的数据是否缓存完全
     * @param position
     * @return
     */
    public boolean isMp4CompletedFromPosition(long position) { return false; }

    /**
     * 获取mp4视频最近的缓存点
     * @param position
     * @return
     */
    public long getMp4CachedPosition(long position) { return -1L; }

    protected void setThreadPoolArgument(int corePoolSize, int maxPoolSize) {
        if (isTaskRunning()) {
            mTaskExecutor.setCorePoolSize(corePoolSize);
            mTaskExecutor.setMaximumPoolSize(maxPoolSize);
        }
    }

    protected void saveVideoInfo() {
        VideoProxyThreadUtils.submitRunnableTask(() -> StorageUtils.saveVideoCacheInfo(mCacheInfo, mSaveDir));
    }
}
