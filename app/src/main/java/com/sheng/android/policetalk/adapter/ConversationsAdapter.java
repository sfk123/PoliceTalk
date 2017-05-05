package com.sheng.android.policetalk.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.sheng.android.policetalk.fragment.FragmentMessage;
import com.sheng.android.policetalk.modal.Conversation;
import com.sheng.android.policetalk.modal.User;

import java.util.ArrayList;
import java.util.List;

import cn.bingoogolapple.swipeitemlayout.BGASwipeItemLayout;

/**
 * Created by Administrator on 2017/4/15.
 */

public class ConversationsAdapter extends RecyclerArrayAdapter<Conversation> {
    private Context mContext;
    private Resources res;

    private FragmentMessage fragmentMessage;
    public ConversationsAdapter(Context mContext, Resources res,FragmentMessage fragmentMessage) {
        super(mContext);
        this.mContext = mContext;
        this.res=res;
        this.fragmentMessage=fragmentMessage;
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
        return new ConversationViewHolder(parent, swipeItemOnItemChildClickListener, mOpenedSil,res,fragmentMessage);
    }

}
