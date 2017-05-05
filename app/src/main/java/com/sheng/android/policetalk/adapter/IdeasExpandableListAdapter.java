package com.sheng.android.policetalk.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.sheng.android.policetalk.R;
import com.sheng.android.policetalk.URLConfig;
import com.sheng.android.policetalk.modal.Group;
import com.sheng.android.policetalk.modal.User;
import com.sheng.android.policetalk.util.ImageTools;
import com.sheng.android.policetalk.util.ImgGrayTransformation;
import com.sheng.android.policetalk.util.MyUtil;
import com.sheng.android.policetalk.view.RoundImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 联系人列表 可伸缩listview
 * Created by Administrator on 2017/3/20.
 */

public class IdeasExpandableListAdapter extends BaseExpandableListAdapter {
    private Context mContext = null;
    private LayoutInflater layoutInflater;
    private List<Group> groups;
    private ImgGrayTransformation grayColorFilter;
    private Resources res;
    public IdeasExpandableListAdapter(Context context,List<Group> groups) {
        this.mContext = context;
        this.groups = groups;
        if(context!=null) {
            this.layoutInflater = LayoutInflater.from(context);
            grayColorFilter = new ImgGrayTransformation();
            changeLanguage();
        }
    }
    public void changeLanguage(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("wujay", Context.MODE_PRIVATE);
        String language=sharedPreferences.getString("language","中文");
        Locale myLocale;
        if(language.equals("中文")){
            myLocale = new Locale("zh");
        }else{
            myLocale = new Locale("en");
        }
        res = mContext.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        notifyDataSetChanged();
    }
    public boolean areAllItemsEnabled() {
        return false;
    }

    /*
     * 设置子节点对象，在事件处理时返回的对象，可存放一些数据
     */
    public Object getChild(int groupPosition, int childPosition) {
        if(groupPosition==0)
            return groups.get(childPosition);
        else
            return groups.get(groups.size()-1).getMambers().get(childPosition);
    }
    public List<User> getUsers(){
        return groups.get(groups.size()-1).getMambers();
    }
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    /*
     * 字节点视图，这里我们显示一个文本对象
     */
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder holder;
        if(convertView==null){
            convertView = layoutInflater.inflate(R.layout.item_contacts, null);
            holder=new ChildViewHolder();
            holder.setTv((TextView)convertView.findViewById(R.id.tv_item));
            holder.setRoundImageView((RoundImageView)convertView.findViewById(R.id.img_head));
            convertView.setTag(holder);
        }else{
            holder=(ChildViewHolder)convertView.getTag();
        }
        String name;
        if(groupPosition==0){
            System.out.println("----------------------------------------");
            for(User u:groups.get(childPosition).getMambers()){
                System.out.println("name:"+u.getUsername());
            }
            name=groups.get(childPosition).getName()+"  (<font color='#f86964'>"+groups.get(childPosition).getMambers().size()+"人</font>)";
            holder.getRoundImageView().setImageResource(R.mipmap.head_group);
        }else {
            User user=groups.get(groups.size()-1).getMambers().get(childPosition);
            name = user.getUsername();
            if(user.getOnline()){
                Picasso.with(mContext).load(URLConfig.getHeadPhoto(user.getPhoto())).placeholder(R.mipmap.head_online).into(holder.getRoundImageView());
            }else{
                if(user.getPhoto()==null||user.getPhoto().equals("")) {
                    holder.getRoundImageView().setImageResource(R.mipmap.head_offline);
                }else{
//                    System.out.println("设置灰色");
                    Picasso.with(mContext).load(URLConfig.getHeadPhoto(user.getPhoto())).placeholder(R.mipmap.head_online).transform(grayColorFilter).into(holder.getRoundImageView());
                }
            }
        }
        holder.getTv().setText(Html.fromHtml(name));
        return convertView;
    }
    class ChildViewHolder{
        private TextView tv;
        private RoundImageView roundImageView;
        public TextView getTv() {
            return tv;
        }

        public void setTv(TextView tv) {
            this.tv = tv;
        }

        public RoundImageView getRoundImageView() {
            return roundImageView;
        }

        public void setRoundImageView(RoundImageView roundImageView) {
            this.roundImageView = roundImageView;
        }
    }
    /*
     * 返回当前分组的字节点个数
     */
    public int getChildrenCount(int groupPosition) {
        if(groupPosition==0)
            return groups.size()-1;
        else
            return groups.get(groups.size()-1).getMambers().size();
    }

    /*
     * 返回分组对象，用于一些数据传递，在事件处理时可直接取得和分组相关的数据
     */
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    /*
     * 分组的个数
     */
    public int getGroupCount() {
        return 2;
    }

    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    /*
     * 分组视图，这里也是一个文本视图
     */
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        TextView text = null;
        if (convertView == null) {
            text = new TextView(mContext);
        } else {
            text = (TextView) convertView;
        }
        String name;
        if(groupPosition==0){
            name=res.getString(R.string.grouplist);
        }else{
            name=res.getString(R.string.all_member);
        }
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT, MyUtil.ToDipSize(60,mContext));
        text.setLayoutParams(lp);
        text.setTextSize(18);
        text.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        text.setPadding(MyUtil.ToDipSize(40,mContext), 0, 0, 0);
        text.setText(name);
        return text;
    }

    /*
     * 判断分组是否为空，本示例中数据是固定的，所以不会为空，我们返回false
     * 如果数据来自数据库，网络时，可以把判断逻辑写到这个方法中，如果为空
     * 时返回true
     */
    public boolean isEmpty() {
        return false;
    }

    /*
     * 收缩列表时要处理的东西都放这儿
     */
    public void onGroupCollapsed(int groupPosition) {

    }

    /*
     * 展开列表时要处理的东西都放这儿
     */
    public void onGroupExpanded(int groupPosition) {

    }

    /*
     * Indicates whether the child and group IDs are stable across changes to
     * the underlying data.
     */
    public boolean hasStableIds() {
        return false;
    }

    /*
     * Whether the child at the specified position is selectable.
     */
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
