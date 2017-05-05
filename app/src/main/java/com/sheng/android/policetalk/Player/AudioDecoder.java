package com.sheng.android.policetalk.Player;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sheng.android.policetalk.AudioData;
import com.sheng.android.policetalk.Record.AudioRecorder;
import com.sheng.android.policetalk.UDP.UDPService;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import xmu.swordbearer.audio.AudioCodec;

/**
 * Created by Administrator on 2017/3/9.
 */

public class AudioDecoder implements Runnable {
    String LOG = "CODEC Decoder ";
    private static AudioDecoder decoder;

    private static final int MAX_BUFFER_SIZE = 1024;

    private byte[] decodedData = new byte[1024];// data of decoded
    private boolean isDecoding = false;
    private List<AudioData> dataList = null;
    private AudioPlayer player;
    private int valume;

    public static AudioDecoder getInstance() {
        if (decoder == null) {
            decoder = new AudioDecoder();
        }
        return decoder;
    }

    private AudioDecoder() {
        this.dataList = Collections
                .synchronizedList(new LinkedList<AudioData>());
    }

    /*
     * add Data to be decoded
     *
     * @ data:the data recieved from server
     *
     * @ size:data size
     */
    public void addData(byte[] data, int size) {
        AudioData adata = new AudioData();
        adata.setSize(size);
        byte[] tempData = new byte[size];
        System.arraycopy(data, 0, tempData, 0, size);
        adata.setRealData(tempData);
        dataList.add(adata);
//        System.out.println(LOG + "add data once");

    }

    /*
     * start decode AMR data
     */
    public void startDecoding() {
        System.out.println(LOG + "start decoder");
        if (isDecoding) {
            return;
        }
        new Thread(this).start();
    }
    public void setVolume(int valume){
        this.valume=valume;
    }
    public void run() {
        // start player first
        player = new AudioPlayer();
        player.setVolume(valume);
        player.startPlaying();
        //
        this.isDecoding = true;
        // init ILBC parameter:30 ,20, 15
        AudioCodec.audio_codec_init(30);

        Log.d(LOG, LOG + "initialized decoder");
        int decodeSize = 0;
        while (isDecoding) {
            while (dataList.size() > 0) {
                AudioData encodedData = dataList.remove(0);
                decodedData = new byte[MAX_BUFFER_SIZE];
                byte[] data = encodedData.getRealData();
                //
                decodeSize = AudioCodec.audio_decode(data, 0,
                        encodedData.getSize(), decodedData, 0);
                if (decodeSize > 0) {
                    // add decoded audio to player
                    player.addData(decodedData, decodeSize);
                    // clear data
                    decodedData = new byte[decodedData.length];
                }
            }
        }
        System.out.println(LOG + "stop decoder");
        // stop playback audio
        player.stopPlaying();
    }

    public void stopDecoding() {
        this.isDecoding = false;
    }
}
