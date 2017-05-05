package com.sheng.android.policetalk.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sheng.android.policetalk.R;
import com.sheng.android.policetalk.URLConfig;
import com.sheng.android.policetalk.modal.ReturnData;
import com.sheng.android.policetalk.util.HttpCallBack;
import com.sheng.android.policetalk.util.HttpUtil;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/4/8.
 */

public class UploadPhotoDialog extends Dialog{
    @Bind(R.id.btn_photo)
    Button btn_photo;
    @Bind(R.id.btn_camera)
    Button btn_camera;
    public UploadPhotoDialog(Context context, int theme) {
        super(context, theme);
    }
    public UploadPhotoDialog(Context context,View.OnClickListener onClickListener){
        super(context, R.style.Theme_Transparent);
        setContentView(R.layout.dialog_upload_photo);
        ButterKnife.bind(this);
        setCanceledOnTouchOutside(true);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.gravity = Gravity.CENTER;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(lp);
        btn_photo.setOnClickListener(onClickListener);
        btn_camera.setOnClickListener(onClickListener);
    }
}
