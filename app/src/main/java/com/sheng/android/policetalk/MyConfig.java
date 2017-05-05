package com.sheng.android.policetalk;

/**
 * Created by Administrator on 2017/3/9.
 */

public class MyConfig {
    public static String SERVER_HOST = "120.25.247.85";// 服务器的IP
//    public static String SERVER_HOST = "192.168.88.110";// 服务器的IP
    public static final int SERVER_PORT_MESSAGE = 5055;// 服务器的监听消息端口
    public static final int SERVER_PORT_SINGLE= 5051;// 服务器的监听音频端口

    public static final int AUDIO_STATUS_RECORDING = 0;//手机端的状态：录音 or 播放
    public static final int AUDIO_STATUS_LISTENING = 1;

    public static void setServerHost(String ip) {
        System.out.println("修改后的服务器网址为  " + ip);
        SERVER_HOST = ip;
    }
}
