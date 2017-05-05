package com.sheng.android.policetalk;

/**
 * Created by Administrator on 2017/3/9.
 */

public class AudioData {
    int size;
    byte[] realData;
    //long timestamp;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public byte[] getRealData() {
        return realData;
    }

    public void setRealData(byte[] realData) {
        this.realData = realData;
    }
}
