package com.github.videophotoplay.utils.videocache.listener;

import com.github.videophotoplay.utils.videocache.common.VideoCacheException;
import com.github.videophotoplay.utils.videocache.m3u8.M3U8;
import com.github.videophotoplay.utils.videocache.model.VideoCacheInfo;

public abstract class VideoInfoParsedListener implements IVideoInfoParsedListener {


    @Override
    public void onM3U8ParsedFinished(M3U8 m3u8, VideoCacheInfo cacheInfo) {

    }

    @Override
    public void onM3U8ParsedFailed(VideoCacheException e, VideoCacheInfo cacheInfo) {

    }

    @Override
    public void onM3U8LiveCallback(VideoCacheInfo cacheInfo) {

    }

    @Override
    public void onNonM3U8ParsedFinished(VideoCacheInfo cacheInfo) {

    }

    @Override
    public void onNonM3U8ParsedFailed(VideoCacheException e, VideoCacheInfo cacheInfo) {

    }
}
