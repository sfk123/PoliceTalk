package com.sheng.android.policetalk.view;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;

import com.sheng.android.policetalk.R;

/**
 *
 * 
 * @author swerit
 * 
 */
public class LoadingDialog {
	// 最上面一层的透明弹窗
	private static PopupWindow mPopArtical;
	private static View popView;
	private static LayoutInflater layoutInflater;
	
	/**
	 * 将图片放大显示
	 * @param context：要显示这个弹窗的上下文
	 * @param picUrl：要显示的图片的链接
	 */
	public static void showWindow(Context context){
		layoutInflater 	 = LayoutInflater.from(context);
		popView 		 = layoutInflater.inflate(R.layout.progress_dialog, null);
		dismiss();
		
		//初始化弹窗
		mPopArtical = new PopupWindow(popView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		//设置弹窗进入和消失的动画效果
		mPopArtical.setAnimationStyle(R.style.loadingdialog_animation);
		
		mPopArtical.showAtLocation(popView, Gravity.TOP, 0, 0);
	}
	
	public static void showQuitWindow(Context context) {
		layoutInflater = layoutInflater.from(context);
		popView = layoutInflater.inflate(R.layout.quit_progress_dialog, null);
		
		//初始化弹窗
		mPopArtical = new PopupWindow(popView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		//设置弹窗进入和消失的动画效果
		mPopArtical.setAnimationStyle(R.style.loadingdialog_animation);
		
		mPopArtical.showAtLocation(popView, Gravity.TOP, 0, 0);
	}
	
	public static void dismiss(){
		if (mPopArtical!=null && mPopArtical.isShowing()){
			mPopArtical.dismiss();
			mPopArtical = null;
		}
	}
	
	
	public static boolean isShowing(){
		if (mPopArtical != null)
			return mPopArtical.isShowing();
		
		return false;
	}
}
