package com.sheng.android.policetalk.WebSocket;

import com.alibaba.fastjson.JSON;
import com.sheng.android.policetalk.UDP.UDPBeatService;
import com.sheng.android.policetalk.URLConfig;
import com.sheng.android.policetalk.modal.EventModal;
import com.sheng.android.policetalk.modal.SocketModel;
import com.sheng.android.policetalk.modal.User;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.simple.eventbus.EventBus;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2017/3/9.
 */

public class WebSocketConnection {
    public WebSocketClient mWebSocketClient	;
    private String address ;
    private int userID;
    private Timer timer;
    private int delay=0;
    private BeatService beatService;
    private static WebSocketConnection instence;
    public static WebSocketConnection getInstence(){
        if(instence==null)
            instence=new WebSocketConnection();
        return  instence;
    }
    private WebSocketConnection(){
        address="ws://"+ URLConfig.host+"/webSocketServer?token=87dadaf37cb06cdc675a8030babab980";
        timer=new Timer();
    }
    private void initSocketClient(final int id) throws URISyntaxException {
        userID=id;
        if(mWebSocketClient == null) {
//            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            System.out.println("连接地址："+address+"&user_id="+id);
            mWebSocketClient = new WebSocketClient(new URI(address+"&user_id="+id)) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {//连接成功
                    SocketModel data=new SocketModel();
                    data.setType("init");
                    data.setClientID(id);
                    sendMessage(data);
                    isClose=false;
                    beatService=new BeatService(instence,userID);
                    new Thread(beatService).start();
                }
                @Override
                public void onMessage(String s) {//服务端消息
                    System.out.println("------------------------------received:" + s);
                    SocketModel data=JSON.parseObject(s,SocketModel.class);
                    if(data.getType().equals("beat")){
                        UDPBeatService.getInstence().clear();
                    }else if(data.getType().equals("member_talk")){
                        EventModal eventModal=new EventModal();
                        eventModal.setData(data);
                        EventBus.getDefault().post(eventModal, "member_talk");
                    }else if(data.getType().equals("prepareTalk_fail")){
                        EventBus.getDefault().post(new EventModal(), "prepareTalk_fail");
                    }else if(data.getType().equals("stop_talk")){//正在说话的一端 突然掉线，停止说话
                        EventModal eventModal=new EventModal();
                        eventModal.setData(data);
                        EventBus.getDefault().post(eventModal, "websocket_stop_talk");
                    }
                    else if(data.getType().equals("init_back")){//websocket初始化返回
                        EventModal eventModal=new EventModal();
                        eventModal.setType("connect");
                        eventModal.setData(data.getData());
                        EventBus.getDefault().post(eventModal, "connect");
                    }else if(data.getType().equals("online")){//用户上线
                        EventModal eventModal=new EventModal();
                        eventModal.setType("online");
                        eventModal.setData(data.getData());//上线用户id
                        EventBus.getDefault().post(eventModal, "online");
                    }else if(data.getType().equals("offline")){//用户离线线
                        EventModal eventModal=new EventModal();
                        eventModal.setData(data.getClientID());//上线用户id
                        EventBus.getDefault().post(eventModal, "offline");
                    }else if(data.getType().equals("group_voice")){//收到语音数据
                        System.out.println("语音消息地址："+data.getData().toString());
                        EventModal eventModal= new EventModal();
                        eventModal.setData(data);
                        EventBus.getDefault().post(eventModal, "get_voice");
                    }else if(data.getType().equals("single_voice")){//收到语音数据
                        System.out.println("语音消息地址："+data.getData().toString());
                        EventModal eventModal= new EventModal();
                        eventModal.setData(data);
                        EventBus.getDefault().post(eventModal, "get_voice");
                    }else if(data.getType().equals("addNewUser")){//添加新用户
                        User u=JSON.parseObject(data.getData().toString(),User.class);
                        EventModal eventModal=new EventModal();
                        eventModal.setData(u);//新用户
                        EventBus.getDefault().post(eventModal, "addNewUser");
                    }else if(data.getType().equals("deleteUser")){//后台删除用户
                        EventModal eventModal=new EventModal();
                        eventModal.setData(data.getClientID());//新用户
                        EventBus.getDefault().post(eventModal, "deleteUser");
                    }else if(data.getType().equals("addNewGroup")){//后台添加群组
                        EventModal eventModal=new EventModal();
                        eventModal.setData(data);
                        EventBus.getDefault().post(eventModal, "addNewGroup");
                    }else if(data.getType().equals("addUserToGroup")){//后台为群组添加用户
                        EventModal eventModal=new EventModal();
                        eventModal.setData(data);
                        EventBus.getDefault().post(eventModal, "addUserToGroup");
                    }else if(data.getType().equals("removeUserOfGroup")){//后台移除群组成员
                        EventModal eventModal=new EventModal();
                        eventModal.setData(data);
                        EventBus.getDefault().post(eventModal, "removeUserOfGroup");
                    }else if(data.getType().equals("getUserIsOnline")){
                        EventModal eventModal=new EventModal();
                        eventModal.setData(data);
                        EventBus.getDefault().post(eventModal, "getUserIsOnline");
                    }else if(data.getType().equals("deleteGroup")){
                        EventModal eventModal=new EventModal();
                        eventModal.setData(data);
                        EventBus.getDefault().post(eventModal, "deleteGroup");
                    }else if(data.getType().equals("UDP_init_single_parse")){//单聊初始化UDP返回（对方和自己聊）
                        EventModal eventModal=new EventModal();
                        eventModal.setType("UDP_init_single_parse");
                        eventModal.setData(data);
                        EventBus.getDefault().post(eventModal, "UDP_single");
                    }else if(data.getType().equals("UDP_init_single_back")){//单聊初始化UDP返回（对方没打开和自己的聊天窗口）
                        EventModal eventModal=new EventModal();
                        eventModal.setType("UDP_init_single_back");
                        EventBus.getDefault().post(eventModal, "UDP_single");
                    }else if(data.getType().equals("goaway_single")){//单聊初始化UDP返回（对方没打开和自己的聊天窗口）
                        EventModal eventModal=new EventModal();
                        eventModal.setType("goaway_single");
                        eventModal.setData(data);
                        EventBus.getDefault().post(eventModal, "UDP_single");
                    }else if(data.getType().equals("prepareUDP_back")){
                        EventModal eventModal=new EventModal();
                        eventModal.setType("prepareUDP_back");
                        eventModal.setData(data.getData());
                        EventBus.getDefault().post(eventModal, "UDP_group");
                    }else if(data.getType().equals("other_login")){//当前账号已被其他人登录
                        EventBus.getDefault().post(new EventModal(), "logout");
                    }else if(data.getType().equals("start_talk_single")||data.getType().equals("stop_talk_single")){//单聊对方打开聊天
                        EventModal eventModal=new EventModal();
                        eventModal.setType(data.getType());
                        eventModal.setData(data);
                        EventBus.getDefault().post(eventModal, "UDP_single");
                    }else if(data.getType().equals("change_photo")){
                        EventModal eventModal=new EventModal();
                        eventModal.setType("changePhoto");
                        eventModal.setData(data);
                        EventBus.getDefault().post(eventModal, "userEvent");
                    }else if(data.getType().equals("changGroup")){
                        EventModal eventModal=new EventModal();
                        eventModal.setType("chang_talk_back");
                        eventModal.setData(data.getData());
                        EventBus.getDefault().post(eventModal, "chang_talk");
                    }else if(data.getType().equals("addGroupManager")){//添加群组管理员
                        EventModal eventModal=new EventModal();
                        eventModal.setType("addGroupManager");
                        eventModal.setData(data);
                        EventBus.getDefault().post(eventModal, "group_event");
                    }
                    beatService.clear();
                }
                @Override
                public void onClose(int i, String s, boolean remote) {//连接断开，remote判定是客户端断开还是服务端断开

                    System.out.println("WebSocket Connection closed by " + ( remote ? "remote peer" : "us" ) + ", info=" + s);
                    //
//                    closeConnect();
                    if(!isClose) {
                        System.out.println("非正常关闭 开始重连");
                        EventBus.getDefault().post(new EventModal(), "websocket_close");
//                        reConnect();
                    }
                    beatService.stop();
                }
                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                    System.out.println("error:" + e);
                }
            };
        }
    }
    public void connect(int id) {//连接
        if(mWebSocketClient==null){
            try {
                initSocketClient(id);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        if(!mWebSocketClient.isOpen()&&!mWebSocketClient.isConnecting())
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(!mWebSocketClient.isConnecting())
                        mWebSocketClient.connect();
                }
            },delay);
    }
    boolean isClose=false;
    public void disconnect(){
        isClose=true;
        closeConnect();
    }
    private void closeConnect() {//断开连接
        try {
            if(mWebSocketClient!=null&&mWebSocketClient.isOpen())
                mWebSocketClient.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            mWebSocketClient = null;
        }
    }
    public synchronized void reConnect(){//意外断线重连
        if(mWebSocketClient.isConnecting()){
            return;
        }
            try {
                if (mWebSocketClient != null)
                    mWebSocketClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mWebSocketClient = null;
                delay=1000;
                connect(userID);
            }
    }
    public void sendMessage(SocketModel data){
        if(mWebSocketClient!=null&&mWebSocketClient.isOpen()){
            mWebSocketClient.send(JSON.toJSONString(data));
        }
    }
}
