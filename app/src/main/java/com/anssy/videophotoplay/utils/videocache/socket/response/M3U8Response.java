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
 * M3U8视频的local server端
 *
 * M3U8是有可能是直播的,怎么区分是不是直播?直播情况下使用本地代理没有意义
 *
 */
public class M3U8Response extends BaseResponse {

    private static final String TAG = "M3U8Response";

    private String mMd5;
    private File mFile;

    public M3U8Response(HttpRequest request, String videoUrl, Map<String, String> headers, long time) {
        super(request, videoUrl, headers, time);
        mMd5 = ProxyCacheUtils.computeMD5(videoUrl);
        mFile = new File(mCachePath, mMd5 + File.separator + mMd5 + StorageUtils.PROXY_M3U8_SUFFIX);
        mResponseState = ResponseState.OK;
    }

    @Override
    public void sendBody(Socket socket, OutputStream outputStream, long pending) throws Exception {
        if (TextUtils.isEmpty(mMd5)) {
            throw new VideoCacheException("Get md5 failed");
        }
        Object lock = VideoLockManager.getInstance().getLock(mMd5);
        int waitTime = WAIT_TIME;

        /**
         * 1.如果文件不存在或者proxy M3U8文件没有生成
         * 2.当前M3U8不能是直播
         */
        while(!mFile.exists() || !VideoProxyCacheManager.getInstance().isM3U8LocalProxyReady(mMd5)) {
            if (VideoProxyCacheManager.getInstance().isM3U8LiveType(mMd5)) {
                throw new VideoCacheException("M3U8 is live type");
            }
            synchronized (lock) {
                lock.wait(waitTime);
            }
        }
        RandomAccessFile randomAccessFile = null;

        try {
            randomAccessFile = new RandomAccessFile(mFile, "r");
            if (randomAccessFile == null) {
                throw new VideoCacheException("M3U8 proxy file not found, this=" + this);
            }

            int bufferedSize = StorageUtils.DEFAULT_BUFFER_SIZE;
            byte[] buffer = new byte[bufferedSize];
            long available = randomAccessFile.length();
            long offset = 0;

            while (shouldSendResponse(socket, mMd5)) {
                if (available == 0) {
                    synchronized (lock) {
                        waitTime = getDelayTime(waitTime);
                        lock.wait(waitTime);
                    }
                    available = randomAccessFile.length();
                    if (waitTime < MAX_WAIT_TIME) {
                        waitTime *= 2;
                    }
                } else {
                    randomAccessFile.seek(offset);
                    int readLength;
                    while ((readLength = randomAccessFile.read(buffer, 0, buffer.length)) != -1) {
                        offset += readLength;
                        outputStream.write(buffer, 0, readLength);
                        randomAccessFile.seek(offset);
                    }
                    LogUtils.i(TAG, "Send M3U8 video info end, this="+this);
                    break;
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            ProxyCacheUtils.close(randomAccessFile);
        }
    }
}