package com.sheng.android.policetalk;


import android.os.Handler;

import com.sheng.android.policetalk.Record.AudioRecorder;
import com.sheng.android.policetalk.UDP.UDPBeatService;
import com.sheng.android.policetalk.UDP.UDPService;
import com.sheng.android.policetalk.UDP.UDPServiceStopCallBack;

public class AudioWrapper {

	private AudioRecorder audioRecorder;

	private static AudioWrapper instanceAudioWrapper;

	private AudioWrapper() {
		audioRecorder=AudioRecorder.getInstance();
	}

	public static AudioWrapper getInstance() {
		if (null == instanceAudioWrapper) {
			instanceAudioWrapper = new AudioWrapper();
		}
		return instanceAudioWrapper;
	}
	public void destory(){
		stopListen(null);
		stopRecord(true);
		UDPBeatService.getInstence().stop();
		instanceAudioWrapper=null;
	}
	public void initUDP(String type, int currentID, int group_id,int port){//初始化群消息监听
		UDPService.getInstence().init(type,currentID,group_id,port);
	}
	public void InitOK(){
		UDPService.getInstence().initOK();
	}
	public Boolean isInitOK(){
		return UDPService.getInstence().isInitOK();
	}
	public void startRecord(Handler handler) {
		audioRecorder.startRecording(handler);
	}
	public void prepareOkStartRecord(){
		audioRecorder.prepareOkStartRecord();
	}
	public void stopRecord(boolean isGoaway) {
			audioRecorder.stopRecording();
		if(!isGoaway)
		UDPService.getInstence().stopTalk();
	}

	public void stopListen(UDPServiceStopCallBack callBack) {
		UDPService.getInstence().destory(callBack);
	}
	public void beat(){
		UDPService.getInstence().sendBeat();
	}

}
