package com.anssy.videophotoplay.base;

import android.app.Application;

import com.github.videophotoplay.utils.videocache.VideoProxyCacheManager;

import java.io.File;

import me.jessyan.autosize.AutoSize;
import xyz.doikki.videoplayer.ijk.IjkPlayerFactory;
import xyz.doikki.videoplayer.player.VideoViewConfig;
import xyz.doikki.videoplayer.player.VideoViewManager;


public class BaseApplication extends Application {
    public static BaseApplication instances;

//    private HttpProxyCacheServer proxy;

    public static BaseApplication getInstances() {
        return instances;
    }




    public void onCreate() {
        super.onCreate();
        instances = this;
        AutoSize.initCompatMultiProcess(this);
        VideoViewManager.setConfig(VideoViewConfig.newBuilder()
                //使用使用IjkPlayer解码
                .setPlayerFactory(IjkPlayerFactory.create())
                .setLogEnabled(true)
                .build());
        setCaches();
    }

    private void setCaches(){
        File saveFile =  new File(getExternalFilesDir(null).toString()+File.separator+"videocache");
        if (!saveFile.exists()) {
            saveFile.mkdir();
        }
        VideoProxyCacheManager.Builder builder = new VideoProxyCacheManager.Builder().
                setFilePath(saveFile.getAbsolutePath()).    //缓存存储位置
                        setConnTimeOut(60 * 1000).                  //网络连接超时
                        setReadTimeOut(60 * 1000).                  //网络读超时
                        setExpireTime(2 * 24 * 60 * 60 * 1000).     //2天的过期时间
                        setMaxCacheSize(2L * 1024 * 1024 * 1024)
                .setIgnoreCert(true)
                .setUseOkHttp(true);    //2G的存储上限
        VideoProxyCacheManager.getInstance().initProxyConfig(builder.build());

    }
}
