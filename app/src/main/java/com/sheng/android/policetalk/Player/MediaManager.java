package com.sheng.android.policetalk.Player;

import java.io.IOException;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;

public class MediaManager {


	private static MediaPlayer mPlayer;
	
	private static boolean isPause;
	private static OnCompletionListener oldCompletionListener;
	public static  void playSound(String filePathString,
			OnCompletionListener onCompletionListener) {
		pause();
		if (mPlayer==null) {
			mPlayer=new MediaPlayer();
			//保险起见，设置报错监听
			mPlayer.setOnErrorListener(new OnErrorListener() {
				
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					// TODO Auto-generated method stub
					mPlayer.reset();
					return false;
				}
			});
		}else {
			if(oldCompletionListener!=null)
				oldCompletionListener.onCompletion(mPlayer);
			mPlayer.reset();//就恢复
		}
		oldCompletionListener=onCompletionListener;
		try {
			mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mPlayer.setOnCompletionListener(onCompletionListener);
			mPlayer.setDataSource(filePathString);
			mPlayer.prepare();
			mPlayer.start();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//停止函数
	public static void pause(){
		if (mPlayer!=null&&mPlayer.isPlaying()) {
			mPlayer.pause();
			isPause=true;
		}
	}
	
	//继续
	public static void resume()
	{
		if (mPlayer!=null&&isPause) {
			mPlayer.start();
			isPause=false;
		}
	}
	

	public  static void release()
	{
		if (mPlayer!=null) {
			mPlayer.release();
			mPlayer=null;
		}
	}

	/**
	 * 获取音频时长
	 * @return
     */
	public static int getTime(String filePathString){
		if (mPlayer==null) {
			mPlayer=new MediaPlayer();
			//保险起见，设置报错监听
			mPlayer.setOnErrorListener(new OnErrorListener() {

				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					// TODO Auto-generated method stub
					mPlayer.reset();
					return false;
				}
			});
		}else {
			mPlayer.reset();//就回复
		}

		try {
			mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mPlayer.setDataSource(filePathString);
			mPlayer.prepare();
			int second= mPlayer.getDuration();
			mPlayer.reset();
			return second;
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
}
