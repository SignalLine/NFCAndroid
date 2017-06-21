package com.singal.nfcandroid.activity;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.singal.nfcandroid.R;
import com.singal.nfcandroid.base.BaseNfcActivity;

/**
 * 自动运行程序
 */
public class RunAppActivity extends BaseNfcActivity {

    private String mPackageName = "com.android.mms";//短信
    private static String TAG = RunAppActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_app);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if(mPackageName == null){
            return;
        }
        //获取Tag对象
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        writeNFCTag(tag);
    }

    /**
     * 往标签里面写数据的方法
     *
     * @param tag
     */
    private void writeNFCTag(Tag tag) {
        if(tag == null)
            return;
        NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{NdefRecord.createApplicationRecord(mPackageName)});
        //1.转换成字节获得的大小
        int size = ndefMessage.toByteArray().length;
        try {
            //2.判断NFC标签的数据类型
            Ndef ndef = Ndef.get(tag);
            //判断是否为NDEF标签
            if(ndef != null){
                ndef.connect();
                //判断是否支持可写
                if(!ndef.isWritable()){
                    return;
                }
                //判断标签的容量是否够用
                if(ndef.getMaxSize() < size){
                    return;
                }
                //3写入数据
                ndef.writeNdefMessage(ndefMessage);

                Toast.makeText(this, "写入成功", Toast.LENGTH_SHORT).show();
            }else {
                //当我们买回了的NFC标签是没有格式化的，或者没有分区的执行此步骤
                //Ndef格式类
                NdefFormatable format = NdefFormatable.get(tag);
                //判断是否活得NdefFormatable对象 有一些标签是只读或者不允许格式的
                if(format != null){
                    //连接
                    format.connect();
                    //格式化并将信息写入标签
                    format.format(ndefMessage);

                    Toast.makeText(this, "写入成功", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this, "写入失败", Toast.LENGTH_SHORT).show();
                }
            }


        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG,"NFC 写入失败");
        }

    }
}
