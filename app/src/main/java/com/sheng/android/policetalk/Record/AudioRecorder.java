package com.sheng.android.policetalk.Record;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sheng.android.policetalk.UDP.UDPService;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Administrator on 2017/3/9.
 */

public class AudioRecorder implements Runnable {
    String LOG = "Recorder ";

    private Boolean isRecording = false;
    private AudioRecord audioRecord;

    public static final int voice_update=10021;
    private static final int audioSource = MediaRecorder.AudioSource.DEFAULT;
    private static final int sampleRate = 8000;
    private static final int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    private static final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_FRAME_SIZE =960;
    private int audioBufSize = 0;

    //
    private byte[] samples;// 缓冲区
    private int bufferRead = 0;// 从recorder中读取的samples的大小

    private int bufferSize = 0;// samples的大小

    private static AudioRecorder instance;
    private Thread thread;
    private Lock lock;
    private boolean isStop=false;
    private Handler handler;
    private AudioRecorder(){
        lock=new ReentrantLock();
    }
    public static AudioRecorder getInstance(){
        if(instance==null){
            synchronized (AudioRecorder.class){
                if(instance==null)
                instance=new AudioRecorder();
            }
        }
        return instance;
    }
    public void setHandler(Handler handler){
        this.handler=handler;
    }
    // 开始录制
    public void startRecording() {
        boolean isUnlock=false;
        try {
            instance.isStop=false;
            lock.lock();
                bufferSize = BUFFER_FRAME_SIZE;

                audioBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig,
                        audioFormat);
//            Log.e(LOG, "audioBufSize----------------" + audioBufSize);
                if (audioBufSize == AudioRecord.ERROR_BAD_VALUE) {
                    Log.e(LOG, "audioBufSize error");
                    return;
                }
                samples = new byte[audioBufSize];
                // 初始化recorder
                if (null == audioRecord) {
                    audioRecord = new AudioRecord(audioSource, sampleRate,
                            channelConfig, audioFormat, audioBufSize);
                }
            System.out.println("》》》》》》》"+instance.isStop+"<<<<<<<<<<<<<<<<<<<<<");
            if(instance.isStop) {
                instance.isStop=false;
                System.out.println("》》》》》》》》》》》》》》》》》isStop:"+instance.isStop);
                UDPService.getInstence().stopTalk();
                return;
            }
            lock.unlock();
            isUnlock=true;
                isRecording=true;
                System.out.println("启动线程------------------------------》" + isRecording);
                thread = new Thread(instance);
                thread.start();

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(!isUnlock)
            lock.unlock();
        }
    }

    // 停止录制
    public void stopRecording() {
        try {
            lock.lock();
            System.out.println("stopRecording");
            instance.isStop=true;
            isRecording = false;
            if (thread != null) {
                thread.interrupt();
            }
        }catch (Exception e){

        }finally {
            lock.unlock();
        }
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void release(){
        thread.interrupted();
    }
    // run
    public void run() {
        // 录制前，先启动解码器
        AudioEncoder encoder = AudioEncoder.getInstance();
        encoder.startEncoding();
        audioRecord.startRecording();
        Message message;
        int count=0;//m每5次发送一次音量显示
        while (isRecording&&!Thread.interrupted()) {
            bufferRead = audioRecord.read(samples, 0, bufferSize);
            if (bufferRead>0) {
                // 将数据添加给解码器
                encoder.addData(samples, bufferRead);
                long v = 0;
                // 将 buffer 内容取出，进行平方和运算
                for (int i = 0; i < samples.length; i++) {
                    v += samples[i] * samples[i];
                }
                double mean = v / (double) bufferRead;
                if(handler!=null&&count==5){
                    message=new Message();
                    message.what=voice_update;
                    message.obj=mean;
                    handler.sendMessage(message);
                    count=0;
                }
                count++;
            }else{
                System.out.println("bufferRead:"+bufferRead);
            }

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                isRecording=false;
                e.printStackTrace();
            }
        }
        System.out.println(LOG + "录制结束");
//        MediaRecorderManager.getInstance().release();
        audioRecord.stop();
        encoder.stopEncoding();
        thread=null;
        instance.isStop=false;
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<isStop:"+isStop);
    }

}
