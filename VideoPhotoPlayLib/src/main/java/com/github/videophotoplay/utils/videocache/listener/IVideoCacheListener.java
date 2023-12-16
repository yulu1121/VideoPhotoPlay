package com.github.videophotoplay.utils.videocache.listener;

import com.github.videophotoplay.utils.videocache.model.VideoCacheInfo;

public interface IVideoCacheListener {

    void onCacheStart(VideoCacheInfo cacheInfo);

    void onCacheProgress(VideoCacheInfo cacheInfo);

    void onCacheError(VideoCacheInfo cacheInfo, int errorCode);

    void onCacheForbidden(VideoCacheInfo cacheInfo);

    void onCacheFinished(VideoCacheInfo cacheInfo);
}
