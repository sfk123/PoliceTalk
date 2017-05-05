package com.sheng.android.policetalk.UDP;

import com.sheng.android.policetalk.AudioWrapper;

import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

/**
 * UDP心跳服务
 * Created by Administrator on 2017/3/11.
 */

public class UDPBeatService extends TimerTask {
    private Timer timer;
    private int counter=0;
    private boolean isRunning=false;
    private static UDPBeatService instence;
    private UDPBeatService(){
        timer = new Timer();
    }
    public static UDPBeatService getInstence(){
        if(instence==null) {
            synchronized (UDPBeatService.class) {
                if(instence==null)
                    instence = new UDPBeatService();
            }
        }
        return instence;
    }
    public void clear(){
        System.out.println("clear");
        counter=0;
    }
    public void startTask(){
        if(!isRunning) {
            isRunning = true;
            System.out.println("开始UDP心跳服务");
            timer.schedule(this, 0, 5000);//15秒执行一次
        }
    }
    public void stop(){
        if(timer!=null) {
            timer.cancel();
            timer = null;
        }
        instence=null;
    }
    @Override
    public void run() {
        counter++;
        if(counter>3){
            AudioWrapper.getInstance().beat();
        }
    }
}
