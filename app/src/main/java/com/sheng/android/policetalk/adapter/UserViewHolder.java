package com.sheng.android.policetalk.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.sheng.android.policetalk.R;
import com.sheng.android.policetalk.URLConfig;
import com.sheng.android.policetalk.modal.User;
import com.sheng.android.policetalk.util.ImgGrayTransformation;
import com.sheng.android.policetalk.view.RoundImageView;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

import cn.bingoogolapple.swipeitemlayout.BGASwipeItemLayout;

/**
 * Created by Administrator on 2017/4/15.
 */

public class UserViewHolder extends BaseViewHolder<User> implements View.OnClickListener{
    private TextView tv;
    private RoundImageView img_head;
    private TextView tv_item_swipe_delete;
    private BGASwipeItemLayout itemLayout;
    private SwipeItemOnItemChildClickListener swipeItemOnItemChildClickListener = null;
    private List<BGASwipeItemLayout> mOpenedSil;
    private ImgGrayTransformation grayColorFilter;

    public UserViewHolder(ViewGroup parent, SwipeItemOnItemChildClickListener swipeItemOnItemChildClickListener, List<BGASwipeItemLayout> mOpenedSil,Resources res) {
        super(parent, R.layout.item_group_member);
        this.swipeItemOnItemChildClickListener = swipeItemOnItemChildClickListener;
        this.mOpenedSil = mOpenedSil;
        tv=$(R.id.tv_item);
        img_head=$(R.id.img_head);
        tv_item_swipe_delete = $(R.id.tv_item_swipe_delete);
        itemLayout=$(R.id.sil_item_swipe_root);
        grayColorFilter=new ImgGrayTransformation();
        tv_item_swipe_delete.setText(res.getString(R.string.delete));
    }
    @Override
    public void setData(final User user) {
        tv_item_swipe_delete.setOnClickListener(this);
        itemLayout.setDelegate(new BGASwipeItemLayout.BGASwipeItemLayoutDelegate() {
            @Override
            public void onBGASwipeItemLayoutOpened(BGASwipeItemLayout swipeItemLayout) {
                closeOpenedSwipeItemLayoutWithAnim();
                mOpenedSil.add(swipeItemLayout);
            }

            @Override
            public void onBGASwipeItemLayoutClosed(BGASwipeItemLayout swipeItemLayout) {
                mOpenedSil.remove(swipeItemLayout);
            }

            @Override
            public void onBGASwipeItemLayoutStartOpen(BGASwipeItemLayout swipeItemLayout) {
                closeOpenedSwipeItemLayoutWithAnim();
            }
        });
        tv.setText(user.getUsername());
        if(user.getOnline()){
            tv.setTextColor(Color.BLACK);
            Picasso.with(getContext()).load(URLConfig.getHeadPhoto(user.getPhoto())).placeholder(R.mipmap.head_online).into(img_head);
        }else{
            tv.setTextColor(Color.DKGRAY);
            if(user.getPhoto()==null||user.getPhoto().equals(""))
                img_head.setImageResource(R.mipmap.head_offline);
            else
                Picasso.with(getContext()).load(URLConfig.getHeadPhoto(user.getPhoto())).placeholder(R.mipmap.head_offline).transform(grayColorFilter).into(img_head);
        }
    }
    @Override
    public void onClick(View view) {
        if (null != this.swipeItemOnItemChildClickListener) {
            this.swipeItemOnItemChildClickListener.onItemChildClick(null, view, this.getAdapterPosition());
        }
    }

    public void closeOpenedSwipeItemLayoutWithAnim() {
        for (BGASwipeItemLayout sil : mOpenedSil) {
            sil.closeWithAnim();
        }
        mOpenedSil.clear();
    }
}
