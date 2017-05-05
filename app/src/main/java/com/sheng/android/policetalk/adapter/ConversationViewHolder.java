package com.sheng.android.policetalk.adapter;

import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.sheng.android.policetalk.R;
import com.sheng.android.policetalk.URLConfig;
import com.sheng.android.policetalk.fragment.FragmentMessage;
import com.sheng.android.policetalk.modal.Conversation;
import com.sheng.android.policetalk.modal.Group;
import com.sheng.android.policetalk.modal.User;
import com.sheng.android.policetalk.util.ImgGrayTransformation;
import com.sheng.android.policetalk.view.RoundImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

import cn.bingoogolapple.swipeitemlayout.BGASwipeItemLayout;

/**
 * Created by Administrator on 2017/4/15.
 */

public class ConversationViewHolder extends BaseViewHolder<Conversation> implements View.OnClickListener{
    public TextView item_tv,bar_num;
    public RoundImageView head;
    private BGASwipeItemLayout itemLayout;
    private TextView tv_item_swipe_delete;
    private SwipeItemOnItemChildClickListener swipeItemOnItemChildClickListener = null;
    private List<BGASwipeItemLayout> mOpenedSil;
    private ImgGrayTransformation grayColorFilter;

    private FragmentMessage fragmentMessage;
    public ConversationViewHolder(ViewGroup parent, SwipeItemOnItemChildClickListener swipeItemOnItemChildClickListener, List<BGASwipeItemLayout> mOpenedSil, Resources res,FragmentMessage fragmentMessage) {
        super(parent, R.layout.item_conversation);
        this.swipeItemOnItemChildClickListener = swipeItemOnItemChildClickListener;
        this.mOpenedSil = mOpenedSil;
        this.fragmentMessage=fragmentMessage;
        item_tv=$(R.id.tv_item);
        bar_num=$(R.id.bar_num);
        head=$(R.id.img_head);
        tv_item_swipe_delete = $(R.id.tv_item_swipe_delete);
        itemLayout=$(R.id.sil_item_swipe_root);
        grayColorFilter=new ImgGrayTransformation();

        tv_item_swipe_delete.setText(res.getString(R.string.delete));
    }
    @Override
    public void setData(final Conversation conversation) {
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
        if(conversation.getUnread_count()!=0){
            bar_num.setText(String.valueOf(conversation.getUnread_count()));
            bar_num.setVisibility(View.VISIBLE);
        }else{
            bar_num.setVisibility(View.GONE);
        }
        if(conversation.getType().equals("single")) {
            User user=fragmentMessage.getUser(conversation.getTarget_id());
            if(user!=null) {
                item_tv.setText(user.getUsername());
                if (user.getOnline()) {
                    Picasso.with(getContext()).load(URLConfig.getHeadPhoto(user.getPhoto())).placeholder(R.mipmap.head_online).into(head);
//                holder.head.setImageResource(R.mipmap.head_online);
                } else {
                    if (user.getPhoto() == null || user.getPhoto().equals(""))
                        head.setImageResource(R.mipmap.head_offline);
                    else
                        Picasso.with(getContext()).load(URLConfig.getHeadPhoto(user.getPhoto())).placeholder(R.mipmap.head_offline).transform(grayColorFilter).into(head);
                }
            }else{
                item_tv.setText("已删除的用户");
                head.setImageResource(R.mipmap.head_offline);
            }
        }else if(conversation.getType().equals("group")){
            Group group=fragmentMessage.getGroup(conversation.getTarget_id());
            if(group!=null)
                item_tv.setText(group.getName());
            else
                item_tv.setText("已删除的群组");
            head.setImageResource(R.mipmap.head_group);
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
