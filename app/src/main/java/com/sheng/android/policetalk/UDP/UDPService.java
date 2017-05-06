package com.sheng.android.policetalk.UDP;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sheng.android.policetalk.MyConfig;
import com.sheng.android.policetalk.Player.AudioDecoder;
import com.sheng.android.policetalk.Record.AudioRecorder;
import com.sheng.android.policetalk.URLConfig;
import com.sheng.android.policetalk.modal.EventModal;

import org.simple.eventbus.EventBus;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Administrator on 2017/3/10.
 */

public class UDPService {
//    private DatagramSocket socket;
    private DatagramChannel channel;
    private static UDPService instence;
    private String type;
    private InetAddress ip;
    private int port;
    private boolean isInitOK=false,isRunning=true;
    private AtomicBoolean stopTalk=new AtomicBoolean(false);
    private int currentID,groupID;
    private UDPBeatService udpBeatService;
    private Selector selector;
    private Thread run_thread;
    private AudioDecoder decoder;
    private UDPService(){
        try {
            ip= InetAddress.getByName(MyConfig.SERVER_HOST);
            udpBeatService=UDPBeatService.getInstence();
            decoder = AudioDecoder.getInstance();
            selector = Selector.open();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static UDPService getInstence(){
        if(instence==null){
            instence=new UDPService();
        }
        instence.isRunning=true;
        return instence;
    }
    private void parseData(ByteBuffer receiveBuffer,boolean hasRemain){
        if (hasRemain) {//小于100时认为不是语音
            try {
                String str_temp= Charset.forName("UTF-8").newDecoder().decode(receiveBuffer).toString();
                System.out.println("收到消息：" +str_temp);
//                        System.out.println("收到消息：" + new String(b_temp)+"----------ip:"+packet.getAddress()+"<>port:"+packet.getPort());
                JSONObject json = JSON.parseObject(str_temp);
                if (json.getString("type").equals("prepareTalk")) {
                    if (AudioRecorder.getInstance().isRecording()) {//当前正在说话
                        json.put("type", "prepare_fail");
                    } else {
                        json.put("type", "prepare_OK");
                    }
                    int targetID=json.getIntValue("target_id");
                    json.put("target_id", json.getString("clientID"));
                    json.put("clientID", targetID);
                    System.out.println("返回可以说话");
                    channel.write(ByteBuffer.wrap(json.toJSONString().getBytes()));
//                    UDPService.getInstence().sendMessage();
                } else if (json.getString("type").equals("prepare_fail")) {
                    UDPService.getInstence().stopPrepare(false);
                }else if (json.getString("type").equals("prepare_OK")) {
                    UDPService.getInstence().stopPrepare(true);
                }else if(json.getString("type").equals("init_group_back")){
                    UDPService.getInstence().initOK();
                }else if(json.getString("type").equals("stopTalk_back")){
                    UDPService.getInstence().stopOK();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else{
            System.out.println("语音数据");
            byte[] data=new byte[100];
            receiveBuffer.get(data);
            // 每接收一个UDP包，就交给解码器，等待解码
            decoder.addData(data.clone(), 100);
        }
    }
    public InetAddress getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public DatagramChannel getChannel(){
        return channel;
    }
    synchronized public void initOK(){
        if(!isInitOK) {
            System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<initOK");
            isInitOK = true;
            EventModal eventModal = new EventModal();
            eventModal.setType("UDP_init");
            EventBus.getDefault().post(eventModal, "UDP_init_back");
            UDPBeatService.getInstence().startTask();
            decoder.startDecoding();
        }
    }
    public Boolean isInitOK(){
        return isInitOK;
    }
    public void stopOK(){
        stopTalk.set(false);
    }
    public void stopTalk(){
        if(stopTalk.compareAndSet(false, true)||type.equals("init_single")) {//单聊时不需要发送停止信号
            if(type.equals("init_single")){
                System.out.println("单聊 不发送停止信息");
            }
            return;
        }
        stopTalk.set(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(stopTalk.compareAndSet(true, true)&&channel!=null) {//若没有初始化成功 则重复发送
                    JSONObject json = new JSONObject();
                    json.put("type", "stopTalk");
                    json.put("target_id", groupID);
                    json.put("clientID", currentID);
                    try {
                        byte[] data = (json.toJSONString()).getBytes();
                        System.out.println("----------------------------停止说话");
                        synchronized (channel) {
                            if (channel != null) {
                                channel.write(ByteBuffer.wrap(data));
                            } else {
                                break;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("----------------------------停止说话完成");
            }
        }).start();
    }
    public void init(final String type, final int currentID, final int group_id, final int port){
        this.currentID=currentID;
        this.groupID=group_id;
        this.type=type;
        if(type.equals("init_group"))
            this.port=port;
        else
            this.port= MyConfig.SERVER_PORT_SINGLE;

        Runnable runnable=new Runnable() {
            private boolean isStartInit=false;
            @Override
            public void run() {
                try {
                    try {
                        channel = DatagramChannel.open();
                        channel.configureBlocking(false);
                        channel.connect(new InetSocketAddress(MyConfig.SERVER_HOST,instence.port));
                        channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    ByteBuffer receiveBuffer= ByteBuffer.allocate(100);
                    while (!Thread.interrupted()&&selector.select() > 0) {
                        Iterator iterator = selector.selectedKeys().iterator();
                        while (iterator.hasNext()) {
                            SelectionKey key = null;
                            try {
                                key = (SelectionKey) iterator.next();
                                iterator.remove();
                                if (key.isReadable()) {
//                            System.out.println("读取数据");
                                    receiveBuffer.clear();
                                    DatagramChannel sc = (DatagramChannel) key.channel();
                                    sc.read(receiveBuffer);
                                    boolean hasRemain= receiveBuffer.hasRemaining();
                                    receiveBuffer.flip();
//                            System.out.println(Charset.forName("UTF-8").newDecoder().decode(receiveBuffer.asReadOnlyBuffer()).toString()+"<>"+hasRemain);
                                    parseData(receiveBuffer.asReadOnlyBuffer(),hasRemain);
                                    udpBeatService.clear();
                                }
                                if (key.isWritable()) {
                                    if(!isStartInit) {
                                        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<开始初始化");
                                        startInit();
                                        isStartInit=true;
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        if(run_thread!=null)
            run_thread.interrupt();
        run_thread=new Thread(runnable);
        run_thread.start();
    }
    public int getCurrentID(){
        return currentID;
    }
    synchronized public void startInit(){
        isInitOK=false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("startIniit");
                while(!isInitOK&&isRunning) {//若没有初始化成功 则重复发送
                    JSONObject json = new JSONObject();
                    json.put("type", type);
                    json.put("target_id", groupID);
                    json.put("clientID", currentID);
                    try {
                        channel.write(ByteBuffer.wrap(json.toJSONString().getBytes()));
                        System.out.println("----------------------------初始化UDP");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    private AtomicBoolean prepareTalk=new AtomicBoolean(false);
    public void stopPrepare(boolean status){
        prepareTalk.set(false);
        EventModal eventModal=new EventModal();
        eventModal.setType("back");
        eventModal.setData(status);
        EventBus.getDefault().post(eventModal, "prepareTalk");
    }
    public void stopPrepare(){
        prepareTalk.set(false);
    }
    public void prepareTalk(){
        prepareTalk.getAndSet(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(prepareTalk.compareAndSet(true, true)&&isRunning) {//若没有初始化成功 则重复发送
                    JSONObject json = new JSONObject();
                    json.put("type", "prepareTalk");
                    json.put("target_id", groupID);
                    json.put("clientID", currentID);
                    try {
                        byte[] data = (json.toJSONString()).getBytes();
//                        DatagramPacket dataPacket = new DatagramPacket(data, data.length, ip, port);
//                        dataPacket.setData(data);
                        System.out.println("----------------------------prepareTalk");
//                        socket.send(dataPacket);
                        synchronized (channel) {
                            channel.write(ByteBuffer.wrap(data));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    //销毁连接
    public void destory(final UDPServiceStopCallBack callBack){
        isRunning=false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                udpBeatService.stop();
                if(run_thread!=null)
                    run_thread.interrupt();
                decoder.stopDecoding();
                if(channel!=null) {
                    try {
                        channel.disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }finally {
                        try {
                            channel.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }finally {
                            channel=null;
                        }
                    }
                }
                if(callBack!=null)
                    callBack.stoped();
            }
        }).start();

        instence=null;
    }
    public void sendBeat(){
        JSONObject json = new JSONObject();
        json.put("type", "beat");
        json.put("clientID", UDPService.getInstence().getCurrentID());
        try {
            channel.write(ByteBuffer.wrap(json.toJSONString().getBytes()));
        }catch (SocketException se){
            if(se.getMessage().indexOf("Network is unreachable")!=-1){
                System.out.println("网络不可用");
                EventModal eventModal=new EventModal();
                eventModal.setType("all");
                EventBus.getDefault().post(eventModal, "close_talk");
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
