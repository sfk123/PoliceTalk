package com.sheng.android.policetalk.Record;

import com.alibaba.fastjson.JSONObject;
import com.sheng.android.policetalk.AudioData;
import com.sheng.android.policetalk.MyConfig;
import com.sheng.android.policetalk.UDP.UDPBeatService;
import com.sheng.android.policetalk.UDP.UDPService;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/9.
 */

public class AudioSender implements Runnable {
    String LOG = "AudioSender ";

    private boolean isSendering = false;
    private List<AudioData> dataList;

    private DatagramChannel channel;
    private InetAddress ip;
    private int port;
    private UDPBeatService udpBeatService;
    public AudioSender() {
        this.channel= UDPService.getInstence().getChannel();
        dataList = Collections.synchronizedList(new LinkedList<AudioData>());
        udpBeatService=UDPBeatService.getInstence();
        ip=UDPService.getInstence().getIp();
        port=UDPService.getInstence().getPort();
    }
    // 添加数据
    public void addData(byte[] data, int size) {
        AudioData encodedData = new AudioData();
        encodedData.setSize(size);
        byte[] tempData = new byte[size];
        try {
            System.arraycopy(data, 0, tempData, 0, size);
            encodedData.setRealData(tempData);
            dataList.add(encodedData);
        }catch (Exception e){

        }
    }

    // 发送数据
    private void sendData(byte[] data, int size) {
        udpBeatService.clear();
        try {
//            dataPacket = new DatagramPacket(data, size, ip, port);
//            dataPacket.setData(data);
//            socket.send(dataPacket);
            channel.write(ByteBuffer.wrap(data));
//            System.out.println("发送数据-》ip:"+ip+"<>port:"+port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 开始发送
    public void startSending() {
        System.out.println(LOG + "发送线程启动");
        this.isSendering = true;
        new Thread(this).start();
    }

    // 停止发送
    public void stopSending() {
        this.isSendering = false;
    }

    // run
    public void run() {
        while (isSendering) {
            try {
                if (dataList.size() > 0) {
                    AudioData encodedData = dataList.remove(0);
                    sendData(encodedData.getRealData(), encodedData.getSize());
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        System.out.println(LOG + "发送结束");
    }
}
