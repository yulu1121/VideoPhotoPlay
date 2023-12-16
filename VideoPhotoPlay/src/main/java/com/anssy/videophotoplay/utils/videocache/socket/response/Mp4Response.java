package com.anssy.videophotoplay.utils.videocache.socket.response;

import android.text.TextUtils;

import com.anssy.videophotoplay.utils.videocache.VideoLockManager;
import com.anssy.videophotoplay.utils.videocache.VideoProxyCacheManager;
import com.anssy.videophotoplay.utils.videocache.common.VideoCacheException;
import com.anssy.videophotoplay.utils.videocache.socket.request.HttpRequest;
import com.anssy.videophotoplay.utils.videocache.socket.request.ResponseState;
import com.anssy.videophotoplay.utils.videocache.utils.LogUtils;
import com.anssy.videophotoplay.utils.videocache.utils.ProxyCacheUtils;
import com.anssy.videophotoplay.utils.videocache.utils.StorageUtils;

import java.io.File;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.Map;

/**
 * @author jeffmony
 * MP4视频的local server端
 */
public class Mp4Response extends BaseResponse {
    private static final String TAG = "Mp4Response";

    private File mFile;
    private String mMd5;

    public Mp4Response(HttpRequest request, String videoUrl, Map<String, String> headers, long time) throws Exception {
        super(request, videoUrl, headers, time);
        mMd5 = ProxyCacheUtils.computeMD5(videoUrl);
        mFile = new File(mCachePath, mMd5 + File.separator + mMd5 + StorageUtils.NON_M3U8_SUFFIX);
        mResponseState = ResponseState.OK;
        Object lock = VideoLockManager.getInstance().getLock(mMd5);
        int waitTime = WAIT_TIME;
        mTotalSize = VideoProxyCacheManager.getInstance().getTotalSize(mMd5);
        //等不到MP4文件大小就不返回
        while (mTotalSize <= 0) {
            synchronized (lock) {
                lock.wait(waitTime);
            }
            mTotalSize = VideoProxyCacheManager.getInstance().getTotalSize(mMd5);
        }

        String rangeStr = request.getRangeString();
        mStartPosition = getRequestStartPosition(rangeStr);
        LogUtils.i(TAG, "Range header=" + request.getRangeString() + ", start position="+mStartPosition +", instance="+this);
        if (mStartPosition != -1) {
            mResponseState = ResponseState.PARTIAL_CONTENT;
            //服务端将range起始位置设置到客户端
            VideoProxyCacheManager.getInstance().seekToCacheTaskFromServer(videoUrl, mStartPosition);
        }
    }

    /**
     * 获取range请求的起始位置
     * bytes=15372019-
     * @param rangeStr
     * @return
     */
    private long getRequestStartPosition(String rangeStr) {
        if (TextUtils.isEmpty(rangeStr)) {
            return -1L;
        }
        if (rangeStr.startsWith("bytes=")) {
            rangeStr = rangeStr.substring("bytes=".length());
            if (rangeStr.contains("-")) {
                return Long.parseLong(rangeStr.split("-")[0]);
            }
        }
        return -1L;
    }

    @Override
    public void sendBody(Socket socket, OutputStream outputStream, long pending) throws Exception {
        if (TextUtils.isEmpty(mMd5)) {
            throw new VideoCacheException("Current md5 is illegal, instance="+this);
        }
        Object lock = VideoLockManager.getInstance().getLock(mMd5);
        int waitTime = WAIT_TIME;
        LogUtils.i(TAG, "Current VideoFile exists : " + mFile.exists() + ", File length=" + mFile.length()+", instance=" + this);
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(mFile, "r");
            if (randomAccessFile == null) {
                throw new VideoCacheException("Current File is not found, instance="+this);
            }
            int bufferedSize = StorageUtils.DEFAULT_BUFFER_SIZE;
            byte[] buffer = new byte[bufferedSize];
            long offset = mStartPosition == -1L ? 0 : mStartPosition;

            long avilable = VideoProxyCacheManager.getInstance().getMp4CachedPosition(mVideoUrl, offset);

            while(shouldSendResponse(socket, mMd5)) {
                if (avilable == 0) {
                    synchronized (lock) {
                        lock.wait(waitTime = getDelayTime(waitTime));
                    }
                    avilable = VideoProxyCacheManager.getInstance().getMp4CachedPosition(mVideoUrl, offset);
                    waitTime *= 2;
                } else {
                    randomAccessFile.seek(offset);
                    int readLength;

                    long bufferLength = (avilable - offset + 1) > bufferedSize ? bufferedSize : (avilable - offset + 1);

                    while (bufferLength > 0 && (readLength = randomAccessFile.read(buffer, 0, (int) bufferLength)) != -1) {
                        offset += readLength;
                        outputStream.write(buffer, 0, readLength);
                        randomAccessFile.seek(offset);
                        bufferLength = (avilable - offset + 1) > bufferedSize ? bufferedSize : (avilable - offset + 1);
                    }

                    if (offset >= mTotalSize) {
                        LogUtils.i(TAG, "# Video file is cached in local storage. instance=" + this);
                        break;
                    }
                    if (offset < avilable) {
                        continue;
                    }
                    long lastAvailable = avilable;
                    avilable = VideoProxyCacheManager.getInstance().getMp4CachedPosition(mVideoUrl, offset);
                    waitTime = WAIT_TIME;
                    while(avilable - lastAvailable < bufferedSize && shouldSendResponse(socket, mMd5)) {
                        if (avilable >= mTotalSize - 1) {
                            LogUtils.i(TAG, "## Video file is cached in local storage. instance=" + this);
                            break;
                        }

                        synchronized (lock) {
                            lock.wait(waitTime = getDelayTime(waitTime));
                        }
                        avilable = VideoProxyCacheManager.getInstance().getMp4CachedPosition(mVideoUrl, offset);
                        waitTime *= 2;
                    }
                }
            }
            LogUtils.i(TAG, "Send video info end, instance="+this);
        } catch (Exception e) {
            LogUtils.w(TAG, "Send video info failed, exception="+e+", this="+this);
            throw e;
        } finally {
            ProxyCacheUtils.close(randomAccessFile);
        }
    }
}
