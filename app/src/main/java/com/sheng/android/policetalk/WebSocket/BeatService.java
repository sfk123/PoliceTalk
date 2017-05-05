package com.sheng.android.policetalk.WebSocket;

import com.sheng.android.policetalk.modal.SocketModel;

/**
 * Created by Administrator on 2017/4/3.
 */

public class BeatService implements Runnable {
    private WebSocketConnection webSocketConnection;
    private int client_id;
    private int count=0;
    private boolean isRunning=true;
    public BeatService(WebSocketConnection webSocketConnection,int client_id){
        this.webSocketConnection=webSocketConnection;
        this.client_id=client_id;
    }
    public void clear(){
        count=0;
    }
    public void stop(){
        isRunning=false;
    }
    @Override
    public void run() {
        while(isRunning){
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            if(count>15){//认为已经断开连接，重连
//                webSocketConnection.
//            }else
                if(count>10){//读写空闲20s，发送心跳包
                SocketModel socketModel=new SocketModel();
                socketModel.setType("ping");
                socketModel.setClientID(client_id);
                webSocketConnection.sendMessage(socketModel);
            }else{
                count++;
            }
        }
    }
}
