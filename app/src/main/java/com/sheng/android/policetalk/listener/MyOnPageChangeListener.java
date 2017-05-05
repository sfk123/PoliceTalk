package com.sheng.android.policetalk.listener;

import android.support.v4.view.ViewPager;

/**
 * Created by Administrator on 2017/3/20.
 */

public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
    private PageChange pageChange;
//    private int one = offset *2 +bmpW;//两个相邻页面的偏移量

    public MyOnPageChangeListener(PageChange pageChange){
        this.pageChange=pageChange;
    }
    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPageSelected(int currentIndex) {
        pageChange.onPageSelected(currentIndex);
    }
}
