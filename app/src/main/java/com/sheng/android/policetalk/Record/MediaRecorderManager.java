package com.sheng.android.policetalk.Record;

import android.media.MediaRecorder;

import com.sheng.android.policetalk.modal.EventModal;

import org.simple.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

/**
 * Created by Administrator on 2017/3/21.
 */

public class MediaRecorderManager {
    private MediaRecorder mRecorder;
    private String mDirString;
    private String mCurrentFilePathString;
    private boolean isPrepared = false;
    private static MediaRecorderManager mInstance;
    private long startTime=0;
    private MediaRecorderManager() {
    }
    public boolean isPrepared(){
        return isPrepared;
    }
    public static MediaRecorderManager getInstance() {
        if (mInstance == null) {
            synchronized (MediaRecorderManager.class) {
                if (mInstance == null) {
                    mInstance = new MediaRecorderManager();
                }
            }
        }
        return mInstance;
    }
    public String getDirPath(){
        return mDirString;
    }
    public void Prepare(String dir){
        mDirString=dir;
        File dir_file = new File(mDirString);
        if (!dir_file.exists()) {
            dir_file.mkdirs();
        }
        isPrepared=true;
    }
    // 准备方法
    public void startAudio() {
        if(!isPrepared)
            return;
        try {
            File dir = new File(mDirString);

            String fileNameString = generalFileName();
            File file = new File(dir, fileNameString);

            mCurrentFilePathString = file.getAbsolutePath();

            mRecorder = new MediaRecorder();
            // 设置输出文件
            mRecorder.setOutputFile(file.getAbsolutePath());
            // 设置meidaRecorder的音频源是麦克风
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            // 设置文件音频的输出格式为amr
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
            // 设置音频的编码格式为amr
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.prepare();
            startTime=new Date().getTime();
            mRecorder.start();

        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    private String generalFileName() {
        return UUID.randomUUID().toString() + ".amr";
    }
    // 释放资源
    public void release() {
        try {
            if (mRecorder != null) {
                mRecorder.stop();
                mRecorder.release();
                mRecorder = null;
            }
        }catch (Exception e){}finally {
            mRecorder=null;
        }
        EventModal eventModal=new EventModal();
        eventModal.setType(String.valueOf(new Date().getTime()-startTime));
        eventModal.setData(mCurrentFilePathString);
        EventBus.getDefault().post(eventModal, "MediaRecorder");
    }
}
