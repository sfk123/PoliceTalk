package com.sheng.android.policetalk.Record;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sheng.android.policetalk.AudioData;
import com.sheng.android.policetalk.MyConfig;
import com.sheng.android.policetalk.UDP.UDPService;
import com.sheng.android.policetalk.modal.EventModal;
import com.sheng.android.policetalk.service.WebSocketService;

import org.simple.eventbus.EventBus;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import xmu.swordbearer.audio.AudioCodec;

/**
 * Created by Administrator on 2017/3/9.
 */

public class AudioEncoder implements Runnable {
    String LOG = "AudioEncoder";

    private static AudioEncoder encoder;
    private AtomicBoolean isEncoding = new AtomicBoolean(false);
    private AudioSender sender;

    private List<AudioData> dataList = null;// 存放数据
    private FileOutputStream file_output;
    private File dir;
    private long startTime;
    private Handler handler;
    public static AudioEncoder getInstance() {
        if (encoder == null) {
            synchronized (AudioEncoder.class) {
                if (encoder == null)
                    encoder = new AudioEncoder();
            }
        }
        return encoder;
    }

    private AudioEncoder() {
        dataList = Collections.synchronizedList(new LinkedList<AudioData>());
        dir = Environment.getExternalStorageDirectory();
        if(dir!=null){
            File file = new File(dir.getAbsolutePath()+"/PTT");
            if (file.exists()){
                file.mkdir();
            }
        }
    }

    public void addData(byte[] data, int size) {
        AudioData rawData = new AudioData();
        rawData.setSize(size);
        byte[] tempData = new byte[size];
        System.arraycopy(data, 0, tempData, 0, size);
        rawData.setRealData(tempData);
        dataList.add(rawData);
    }

    // 开始编码
    public void startEncoding(Handler handler) {
        if (isEncoding.compareAndSet(true,true)) {
            Log.e(LOG, "编码器已经启动，不能再次启动");
            return;
        }
        this.handler=handler;
        new Thread(this).start();
    }
    public void prepareOK(){
        startTime=new Date().getTime();
    }
    // 结束
    public void stopEncoding() {
        this.isEncoding.set(false);
    }
    public void stopSender(){
        sender.stopSending();
        sender=null;
    }
    public void run() {
        sender = new AudioSender();
        sender.startSending();

        int encodeSize = 0;
        byte[] encodedData = new byte[256];
        File file=null;
        // 初始化编码器
        AudioCodec.audio_codec_init(30);
        if(dir!=null) {
            file = new File(dir.getAbsolutePath()+"/PTT",UUID.randomUUID().toString() + ".amr");
            try {
                file_output = new FileOutputStream(file,true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }else{
            System.out.println("没有内存卡，不能保存文件");
        }
        System.out.println(LOG + "解码线程启动");
        Message message=new Message();
        message.what= WebSocketService.prepareRecordOK;
        handler.sendMessage(message);
        isEncoding.set(true);
        while (isEncoding.compareAndSet(true,true)) {
            try {
                if (dataList.size() == 0) {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
//            System.out.println("编码");
                synchronized (isEncoding) {
                    if (isEncoding.compareAndSet(true, true)) {
                        AudioData rawData = dataList.remove(0);

                        encodedData = new byte[rawData.getSize()];
                        //
                        try {
                            encodeSize = AudioCodec.audio_encode(rawData.getRealData(), 0,
                                    rawData.getSize(), encodedData, 0);
                        } catch (Exception e) {
                            Log.e("AudioEncoder", "-->:" + e.getMessage());
                        }
//                System.out.println("encodeSize:"+encodeSize);
                        if (encodeSize > 0) {
                            if (sender != null)
                                sender.addData(encodedData, encodeSize);
                            else {
                                System.out.println("sender is null");
                            }
                            if (file_output != null) {
                                try {
                                    file_output.write(encodedData.clone(), 0, encodeSize);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            // 清空数据
                            encodedData = new byte[encodedData.length];
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        try {
//            bufferedOutputStream.flush();
            file_output.close();
//            bufferedOutputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        file_output = null;
        if(file!=null) {
            EventModal eventModal = new EventModal();
            long time=new Date().getTime() - startTime;
            if(time>600) {
                eventModal.setType(String.valueOf(time));
                eventModal.setData(file.getAbsolutePath());
                EventBus.getDefault().post(eventModal, "RecorderBack");
            }else{
                file.delete();
            }
        }
        startTime=0;
        System.out.println(LOG + "编码结束");
        sender.stopSending();
    }
}
