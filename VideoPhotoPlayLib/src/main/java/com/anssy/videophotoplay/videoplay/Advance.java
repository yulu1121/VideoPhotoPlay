package com.anssy.videophotoplay.videoplay;

public class Advance {
 
    public String path;//路径
    public String type;//类型 1、视频 2、图片
    private int playSecond;//图片播放时长
    public Advance(String path, String type,int playSecond) {
        this.path = path;
        this.type = type;
        this.playSecond = playSecond;
    }

    public int getPlaySecond() {
        return playSecond;
    }

    public void setPlaySecond(int playSecond) {
        this.playSecond = playSecond;
    }
}