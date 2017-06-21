package com.singal.nfcandroid.base;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.support.v7.app.AppCompatActivity;

/**
 * 1.子类需要在onCreate方法中做Activity的初始化
 * 2.子类需要在onNewIntent方法中进行NFC标签相关操作
 *
 * 当launchMode设置为singleTop是，第一次运行调用onCreate方法第二次运行将
 * 不会创建新的activity实例，将调用onNewIntent方法，所以我们在获取intent传递
 * 过了的TAG数据操作放在onNewIntent方法中执行，如果在占中已经有该Activity的实例，就重用在实例
 * （会调用实例的onNewIntent）
 *
 * 只要NFC标签靠近就执行
 *
 *
 * Created by li on 2017/6/20.
 */

public class BaseNfcActivity extends AppCompatActivity {
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;

    /**
     * 启动activity 界面可见时
     */
    @Override
    protected void onStart() {
        super.onStart();
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        //一旦截获NFC消息，就会通过PendingIntent调用窗口
        mPendingIntent = PendingIntent.getActivity(this,0,new Intent(this,getClass()),0);
    }

    /**
     * 获取焦点，按钮可以点击
     */
    @Override
    protected void onResume() {
        super.onResume();
        //设置处理优于所有其他NFC的处理
        if(mNfcAdapter != null){
            //要求权限
            mNfcAdapter.enableForegroundDispatch(this,mPendingIntent,null,null);
        }
    }

    /**
     * 暂停activity 界面获取焦点  按钮可以点击
     */
    @Override
    protected void onPause() {
        super.onPause();
        //恢复默认状态
        if(mNfcAdapter != null){
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }
}
