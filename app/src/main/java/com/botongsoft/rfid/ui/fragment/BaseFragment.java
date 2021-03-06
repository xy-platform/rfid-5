package com.botongsoft.rfid.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.botongsoft.rfid.common.utils.LogUtils;

import butterknife.ButterKnife;

/**
 * Author   :hymanme
 * Email    :hymanme@163.com
 * Create at 2016/7/13
 * Description:
 */

public abstract class BaseFragment extends Fragment {
    protected final String TAG = getClass().getName();
    protected View mRootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initRootView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, mRootView);
        initEvents();
        initData(savedInstanceState == null);
        LogUtils.e("onCreateView",   "onCreateView0000000000000");
        return mRootView;
    }

    /**
     * 初始化根布局
     *
     * @return View 视图
     */
    protected abstract void initRootView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    /**
     * 初始化监听事件等
     */
    protected abstract void initEvents();

    /**
     * 加载数据
     * @param isSavedNull
     */
    protected abstract void initData(boolean isSavedNull);
}
