package com.sheng.android.policetalk.Player;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import xmu.swordbearer.audio.AudioCodec;

/**
 * 当点击音频消息时，播放音频
 * Created by Administrator on 2017/3/28.
 */

public class MyAudioPlayer implements Runnable{
    private String fileName;
    private AudioPlayerCallBack callBack;
    private static MyAudioPlayer instence;
    private boolean isStart=false;
    private AudioTrack audioTrack;
    private static final int MAX_BUFFER_SIZE = 1024;
    private static final int sampleRate = 8000;
    // 注意：参数配置
    private static final int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
    private static final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private Thread thread_paly;
    private Lock lock;
    private int valume=1;

    private MyAudioPlayer(){lock=new ReentrantLock();}
    public static synchronized  MyAudioPlayer getInstence(){
        if(instence==null){
            instence=new MyAudioPlayer();
        }
        return instence;
    }
    public void setVolume(int valume){
        this.valume=valume;
    }
    private boolean initAudioTrack() {
        int bufferSize = AudioRecord.getMinBufferSize(sampleRate,channelConfig, audioFormat);
        if (bufferSize < 0) {
            return false;
        }
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                channelConfig, audioFormat, bufferSize, AudioTrack.MODE_STREAM);
        // set volume:设置播放音量
//        audioTrack.setStereoVolume(3.5f, 3.5f);
//        audioTrack.
//        audioTrack.setVolume(2f);
        return true;
    }
    public boolean isStart(){
        return isStart;
    }
    public void start(final String fileName, final AudioPlayerCallBack callBack){
        if(isStart) {
            isStart=false;
            thread_paly.interrupt();
            lock.lock();
            try {
                audioTrack.stop();
                audioTrack.release();
            }catch (Exception e){
                e.printStackTrace();
            }
            this.callBack.onComplete();
            lock.unlock();
        }
        this.fileName = fileName;
        this.callBack = callBack;
        initAudioTrack();
        thread_paly=new Thread(instence);
        thread_paly.start();
    }
    @Override
    public void run() {
        lock.lock();
        File file = new File(fileName);
        InputStream in=null;
        isStart=true;
        try {
            in=new FileInputStream(file);
            int byteread = 0;
            byte[] tempbytes = new byte[100];
            byte[] decodedData;
            int decodeSize = 0;
            AudioCodec.audio_codec_init(30);
            audioTrack.play();
            while ((byteread = in.read(tempbytes)) != -1&&isStart&&!Thread.interrupted()) {
                decodedData = new byte[MAX_BUFFER_SIZE];
                decodeSize = AudioCodec.audio_decode(tempbytes.clone(), 0,
                        byteread, decodedData, 0);
//                System.out.println("decodedData:"+decodedData.length+"decodeSize:"+decodeSize);
                if(decodeSize>0)
                audioTrack.write(voiceControl(decodedData), 0, decodeSize);
                tempbytes = new byte[100];
                Thread.sleep(20);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if(in!=null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                audioTrack.stop();
                audioTrack.release();
            }catch (Exception e){
                e.printStackTrace();
            }
            if(isStart)
            callBack.onComplete();
            isStart=false;
            lock.unlock();
        }
    }
    private byte[] voiceControl(byte[] b_in){
        byte[] b_new=b_in.clone();
        for(int i=0;i<b_new.length;i++){
            byte b=b_new[i];
            b_new[i]=(byte)(b*valume);
            if(b_new[i] > 127)
                b_new[i] = 127;
            else if(b_new[i] < -128)
                b_new[i] = -128;
        }
        return b_new;
    }
}
