package com.sheng.android.policetalk.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.linchaolong.android.imagepicker.ImagePicker;
import com.linchaolong.android.imagepicker.cropper.CropImage;
import com.linchaolong.android.imagepicker.cropper.CropImageView;
import com.sheng.android.policetalk.R;
import com.sheng.android.policetalk.URLConfig;
import com.sheng.android.policetalk.dao.CommonUtils;
import com.sheng.android.policetalk.modal.EventModal;
import com.sheng.android.policetalk.modal.ReturnData;
import com.sheng.android.policetalk.modal.User;
import com.sheng.android.policetalk.service.WebSocketService;
import com.sheng.android.policetalk.util.HttpCallBack;
import com.sheng.android.policetalk.util.HttpUtil;
import com.sheng.android.policetalk.util.MyUtil;
import com.sheng.android.policetalk.view.LoadingDialog;
import com.sheng.android.policetalk.view.RepwdDialog;
import com.sheng.android.policetalk.view.RoundImageView;
import com.sheng.android.policetalk.view.TextMoveLayout;
import com.sheng.android.policetalk.view.UploadPhotoDialog;
import com.squareup.picasso.Picasso;

import org.simple.eventbus.EventBus;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/3/20.
 */

public class FragmentSettings extends Fragment implements HttpCallBack{

    @Bind(R.id.tv_current)
    TextView tv_current;
    @Bind(R.id.tv_move)
    TextMoveLayout tv_move;
    @Bind(R.id.seekbar)
    SeekBar seekbar;
    @Bind(R.id.layout_volume)
    LinearLayout layout_volume;
    @Bind(R.id.img_head)
    RoundImageView img_head;
    @Bind(R.id.ico_language)
    ImageView ico_language;
    @Bind(R.id.lable_current)
    TextView lable_current;
    @Bind(R.id.lable_md_pwd)
    TextView lable_md_pwd;
    @Bind(R.id.lable_clear)
    TextView lable_clear;
    @Bind(R.id.lable_language)
    TextView lable_language;
    @Bind(R.id.lable_voice)
    TextView lable_voice;
    @Bind(R.id.btn_exit)
    Button btn_exit;
    private TextView moveText;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ImagePicker imagePicker = new ImagePicker();
    private int screenWidth;
    private TextPaint mPaint;
    private RepwdDialog repwdDialog;
    private final int clear_ok=1001;
    private final int http_ok=1002;
    private final int http_fail=1003;
    private Resources res;
    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case clear_ok:
                    if(getContext()!=null)
                    Toast.makeText(getContext(),"清空完成",Toast.LENGTH_SHORT).show();
                    EventModal eventModal=new EventModal();
                    eventModal.setType("refresh");
                    EventBus.getDefault().post(eventModal,"updateConversation");
                    break;
                case http_ok:
                    if(LoadingDialog.isShowing())
                        LoadingDialog.dismiss();
                    ReturnData responseBody=(ReturnData)msg.obj;
                    Toast.makeText(getContext(),responseBody.getMessage(),Toast.LENGTH_SHORT).show();
                    if(responseBody.isStatus()){
                        Picasso.with(getContext()).load(URLConfig.getHeadPhoto(responseBody.getData().toString())).placeholder(R.mipmap.head_current).into(img_head);
                    }
                    EventModal eventModal1=new EventModal();
                    eventModal1.setType("changePhoto_local");
                    eventModal1.setData(responseBody.getData().toString());
                    EventBus.getDefault().post(eventModal1,"userEvent");
                    break;
                case http_fail:
                    if(LoadingDialog.isShowing())
                        LoadingDialog.dismiss();
                    Toast.makeText(getContext(),msg.obj.toString(),Toast.LENGTH_SHORT).show();
                    break;
            }
            super.handleMessage(msg);
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("HeadlinesFragment", "onCreate");
        if (getArguments() != null) {
//            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }
    public void setUser(User user){
        tv_current.setText(user.getPhone()+"("+user.getUsername()+")");
        if(user.getPhoto()!=null)
            Picasso.with(getContext()).load(URLConfig.getHeadPhoto(user.getPhoto())).placeholder(R.mipmap.head_current).into(img_head);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_settings, container, false);
        ButterKnife.bind(this,view);
        repwdDialog=new RepwdDialog(getContext());
        setMoveTextView();
        ViewTreeObserver observer = layout_volume.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            boolean isMeasured=false;
            public boolean onPreDraw() {
                if (!isMeasured) {
                    if (mPaint == null) {
                        mPaint = new TextPaint();
                    }
                    float measureText = mPaint.measureText(moveText.getText().toString().trim());
                    Rect bounds = seekbar.getProgressDrawable().getBounds();
                    int xFloat = (int) (bounds.width() * seekbar.getProgress() / seekbar.getMax() - measureText / 2 + MyUtil.ToDipSize( 16,getContext()))-MyUtil.ToDipSize(10,getActivity());
                    moveText.layout(xFloat, 20, screenWidth, 80);
                    isMeasured = true;
                }
                return true;
            }
        });
        return view;
    }
    /**
     * 设置moveTextView的基础参数
     */
    private void setMoveTextView() {
        sharedPreferences = getContext().getSharedPreferences("wujay", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        String volume=sharedPreferences.getString("volume","");
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>volume:"+volume);
        screenWidth = getActivity().getWindowManager().getDefaultDisplay().getWidth();
        moveText = new TextView(getContext());
        if(volume.equals(""))
            moveText.setText(1.0 + "");
        else
            moveText.setText(volume);
        moveText.setTextColor(getResources().getColor(R.color.button_color));
        moveText.setTextSize(16);

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 50);
        tv_move.addView(moveText, layoutParams);
        moveText.layout(5, 20, screenWidth, 80);

        seekbar.setMax(100);
        int volume_int=(int)((Double.parseDouble(moveText.getText().toString())-1)*100);
        seekbar.setProgress(volume_int);
        seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListenerImp());

        Locale myLocale;
        String language=sharedPreferences.getString("language","中文");
        if(language.equals("中文")){
            myLocale = new Locale("zh");
            ico_language.setImageResource(R.mipmap.language_zh);
        }else{
            ico_language.setImageResource(R.mipmap.language_en);
            myLocale = new Locale("en");
        }
        res = getContext().getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        setString();
    }

    private void setString(){
        lable_current.setText(res.getString(R.string.current_username));
        lable_md_pwd.setText(res.getString(R.string.modify_pwd));
        lable_clear.setText(res.getString(R.string.clear));
        lable_language.setText(res.getString(R.string.change_language));
        lable_voice.setText(res.getString(R.string.volume_amplification));
        btn_exit.setText(res.getString(R.string.btn_exit));
    }
    @Override
    public void onSuccess(ReturnData responseBody) {
        Message message1=new Message();
        message1.what=http_ok;
        message1.obj=responseBody;
        myHandler.sendMessage(message1);
    }

    @Override
    public void onFailure(String message) {
        Message message1=new Message();
        message1.what=http_fail;
        message1.obj=message;
        myHandler.sendMessage(message1);
    }

    /**
     * seekbar滑动监听
     */
    private class OnSeekBarChangeListenerImp implements
            SeekBar.OnSeekBarChangeListener {

        // 触发操作，拖动
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            setMoveTextLayout();
        }

        // 开始拖动时候触发的操作
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        // 停止拖动时候
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }

    private void setMoveTextLayout() {
        if (mPaint == null) {
            mPaint = new TextPaint();
        }

        float measureText = mPaint.measureText(moveText.getText().toString().trim());
        Rect bounds = seekbar.getProgressDrawable().getBounds();
        System.out.println("width:"+bounds.width());
        int xFloat = (int) (bounds.width() * seekbar.getProgress() / seekbar.getMax() - measureText / 2 + MyUtil.ToDipSize( 16,getContext()))-MyUtil.ToDipSize(10,getActivity());
        moveText.layout(xFloat, 20, screenWidth, 80);
        double d=seekbar.getProgress()/100.0+1;
        BigDecimal bg = new BigDecimal(d).setScale(2, RoundingMode.UP);
       d =bg.doubleValue();
        moveText.setText( String.valueOf(d));
        editor.putString("volume",String.valueOf(d));
        editor.commit();
    }
    @OnClick(R.id.btn_exit)
    public void exit(View view){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // 设置显示信息
        builder.setMessage(res.getString(R.string.exit_confirm)).setTitle(res.getString(R.string.sys_notice))
                .
                // 设置确定按钮
                        setPositiveButton(res.getString(R.string.btn_determine),
                        new DialogInterface.OnClickListener() {
                            // 单击事件
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                Intent startIntent = new Intent(getContext(), WebSocketService.class);
                                getActivity().stopService(startIntent);
                                getActivity().finish();
                            }
                        }).
                // 设置取消按钮
                        setNegativeButton(res.getString(R.string.btn_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                            }
                        });
        // 创建对话框
        AlertDialog ad = builder.create();
        // 显示对话框
        ad.show();
    }
    @OnClick(R.id.layout_clear)
    public void clear(View view){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // 设置显示信息
        builder.setMessage(res.getString(R.string.clear_confirm))
                .
                // 设置确定按钮
                        setPositiveButton(res.getString(R.string.btn_determine),
                        new DialogInterface.OnClickListener() {
                            // 单击事件
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                clearCach();
                            }
                        }).
                // 设置取消按钮
                        setNegativeButton(res.getString(R.string.btn_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                            }
                        });
        // 创建对话框
        AlertDialog ad = builder.create();
        // 显示对话框
        ad.show();
    }
    @OnClick(R.id.layout_repwd)//修改密码
    public void rePWD(View v){
        repwdDialog.show();
    }
    @OnClick(R.id.layout_user)
    public void modifyPhoto(View v){
        //// 设置标题
        imagePicker.setTitle(res.getString(R.string.picture_from));
        // 设置是否裁剪图片
        imagePicker.setCropImage(true);
        // 启动图片选择器
        imagePicker.startChooser(this, new ImagePicker.Callback() {
            // 选择图片回调
            @Override public void onPickImage(Uri imageUri) {

            }

            // 裁剪图片回调
            @Override public void onCropImage(Uri imageUri) {
                LoadingDialog.showWindow(getContext());
//                System.out.println("Uri:"+imageUri.toString());
                try {
                    File file=new File(new URI(imageUri.toString()));
                    HttpUtil.getInstance().UploadPhoto(URLConfig.getUploadPhotoUrl(),new HashMap<String, String>(),file,FragmentSettings.this);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
//                draweeView.setImageURI(imageUri);
//                draweeView.getHierarchy().setRoundingParams(RoundingParams.asCircle());
            }

            // 自定义裁剪配置
            @Override public void cropConfig(CropImage.ActivityBuilder builder) {
                builder
                        // 是否启动多点触摸
                        .setMultiTouchEnabled(true)
                        // 设置网格显示模式
                        .setGuidelines(CropImageView.Guidelines.OFF)
                        // 圆形/矩形
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        // 调整裁剪后的图片最终大小
                        .setRequestedSize(108, 108)
                        // 宽高比
                        .setAspectRatio(1, 1);
            }

            // 用户拒绝授权回调
            @Override public void onPermissionDenied(int requestCode, String[] permissions,
                                                     int[] grantResults) {
            }
        });
    }
    private void clearCach(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                CommonUtils commonUtils=new CommonUtils(getContext());
                commonUtils.clearRecord();
                File dir = Environment.getExternalStorageDirectory();
                if(dir!=null){
                    File file=new File(dir.getAbsolutePath()+"/PTT");
                    if(file.exists()){
                        File[] files=file.listFiles();
                        for(File f:files){
                            if(f.exists()){
                                f.delete();
                            }
                        }
                    }
                }
                Message message=new Message();
                message.what=clear_ok;
                myHandler.sendMessage(message);
            }
        }).start();
    }
    @OnClick(R.id.layout_language_change)
    public void languageChange(View v){
        final String[] items = new String[] { "中文", "English"};
        AlertDialog dialog = new AlertDialog.Builder(getContext()).setTitle(res.getString(R.string.language_change))
                .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Locale myLocale;
                        if(items[which].equals("中文")){
                            myLocale = new Locale("zh");
                            ico_language.setImageResource(R.mipmap.language_zh);
                        }else{
                            ico_language.setImageResource(R.mipmap.language_en);
                            myLocale = new Locale("en");
                        }
                        editor.putString("language",items[which]);
                        editor.commit();
                        dialog.dismiss();
                        res = getContext().getResources();
                        DisplayMetrics dm = res.getDisplayMetrics();
                        Configuration conf = res.getConfiguration();
                        conf.locale = myLocale;
                        res.updateConfiguration(conf, dm);
                        setString();
                        EventBus.getDefault().post(new EventModal(),"changeLanguage");
                    }
                }).create();
        dialog.show();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imagePicker.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                                     @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        imagePicker.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
}
