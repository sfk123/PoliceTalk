package com.sheng.android.policetalk.Player;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.util.Log;

import com.sheng.android.policetalk.AudioData;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/9.
 */

public class AudioPlayer implements Runnable  {
    String LOG = "AudioPlayer ";
    private static AudioPlayer player;

    private List<AudioData> dataList = null;
    private AudioData playData;
    private boolean isPlaying = false;

    private AudioTrack audioTrack;
    private int valume=1;

    private static final int sampleRate = 8000;
    // 注意：参数配置
    private static final int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
    private static final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    public AudioPlayer() {
        dataList = Collections.synchronizedList(new LinkedList<AudioData>());
    }

    public void setVolume(int valume){
        this.valume=valume;
    }
    public void addData(byte[] rawData, int size) {
        AudioData decodedData = new AudioData();
        decodedData.setSize(size);
        try {
            byte[] tempData = new byte[size];
            System.arraycopy(rawData, 0, tempData, 0, size);
            decodedData.setRealData(tempData);
            dataList.add(decodedData);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /*
     * init Player parameters
     */
    private boolean initAudioTrack() {
        int bufferSize = AudioRecord.getMinBufferSize(sampleRate,channelConfig, audioFormat);
        if (bufferSize < 0) {
            Log.e(LOG, LOG + "initialize error!");
            return false;
        }
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                channelConfig, audioFormat, bufferSize, AudioTrack.MODE_STREAM);
        // set volume:设置播放音量
        audioTrack.setStereoVolume(2.0f, 2.0f);
//        audioTrack.setVolume(2.0f);
        audioTrack.play();
        return true;
    }

    private void playFromList() {
        while (dataList.size() > 0 && isPlaying) {
            playData = dataList.remove(0);
            audioTrack.write(voiceControl(playData.getRealData()), 0, playData.getSize());
        }
    }
    private byte[] voiceControl(byte[] b_in){
        for(int i=0;i<b_in.length;i++){
            byte b=b_in[i];
            b_in[i]=(byte)(b*valume);
            if(b_in[i] > 127)
                b_in[i] = 127;
            else if(b_in[i] < -128)
                b_in[i] = -128;
        }
        return b_in;
    }
    void calc1(byte[] lin,int off,int len) {
        int i,j;
        for (i = 0; i < len; i++) {
            j = lin[i+off];
            lin[i+off] = (byte)(j>>2);
        }
    }
    public void startPlaying() {
        if (isPlaying) {
            return;
        }
        new Thread(this).start();
    }

    public void run() {
        this.isPlaying = true;
        System.out.println("开始播放器");
        if (!initAudioTrack()) {
            Log.e(LOG, LOG + "initialized player error!");
            return;
        }
        while (isPlaying) {
            if (dataList.size() > 0) {
                playFromList();
            } else {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                }
            }
        }
        if (this.audioTrack != null) {
            if (this.audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                this.audioTrack.stop();
                this.audioTrack.release();
            }
        }
        Log.d(LOG, LOG + "end playing");
    }

    public void stopPlaying() {
        System.out.println("stopPlaying<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        this.isPlaying = false;
    }
}
