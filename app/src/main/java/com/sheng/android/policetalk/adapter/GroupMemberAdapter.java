package com.sheng.android.policetalk.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.sheng.android.policetalk.modal.User;

import java.util.ArrayList;
import java.util.List;

import cn.bingoogolapple.swipeitemlayout.BGASwipeItemLayout;

/**
 * Created by Administrator on 2017/4/15.
 */

public class GroupMemberAdapter extends RecyclerArrayAdapter<User> {
    private Context mContext;
    private Resources res;

    public GroupMemberAdapter(Context mContext,Resources res) {
        super(mContext);
        this.mContext = mContext;
        this.res=res;
    }
    SwipeItemOnItemChildClickListener swipeItemOnItemChildClickListener;

    public void setSwipeItemOnItemChildClickListener(SwipeItemOnItemChildClickListener swipeItemOnItemChildClickListener) {
        this.swipeItemOnItemChildClickListener = swipeItemOnItemChildClickListener;
    }

    /**
     * 当前处于打开状态的item
     */
    private List<BGASwipeItemLayout> mOpenedSil = new ArrayList<>();

    public void closeOpenedSwipeItemLayoutWithAnim() {
        for (BGASwipeItemLayout sil : mOpenedSil) {
            sil.closeWithAnim();
        }
        mOpenedSil.clear();
    }
    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new UserViewHolder(parent, swipeItemOnItemChildClickListener, mOpenedSil,res);
    }

}
