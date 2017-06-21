package com.singal.nfcandroid.activity;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.singal.nfcandroid.R;

import java.util.Arrays;

public class ReadTextActivity extends AppCompatActivity {

    private TextView mNfcText;
    private String mTagText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_text);

        mNfcText = (TextView) findViewById(R.id.tv_nfctext);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        //1.获取Tag对象
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        //获取Ndef的实例
        Ndef ndef = Ndef.get(tag);
        mTagText = ndef.getType() + "\nmaxsize:" + ndef.getMaxSize() + "bytes\n\n";
        readNfcTag(intent);
        mNfcText.setText(mTagText);
    }

    /**
     * 读取NFC标签文本数据
     *
     * @param intent
     */
    private void readNfcTag(Intent intent) {
        if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())){
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage msgs[] = null;
            int contentSize = 0;

            if(rawMsgs != null){
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                    contentSize = msgs[i].toByteArray().length;
                }
            }

            try{
                if(msgs != null){
                    NdefRecord record = msgs[0].getRecords()[0];
                    String textRecord = parseTextRecord(record);
                    mTagText += textRecord + "\n\ntext\n" + contentSize + "bytes";
                }
            }catch (Exception e){
                e.printStackTrace();
                Log.e("NFC","--------读取文本信息错误----------");
            }
        }
    }

    /**
     * 解析NDEF文本数据，从第三个字节开始，后面的文本数据
     *
     * @param record
     * @return
     */
    private String parseTextRecord(NdefRecord record) {
        //判断是NDEF格式  TNF
        if(record.getTnf() != NdefRecord.TNF_WELL_KNOWN){
            return null;
        }
        //判断可变的长度类型
        if(!Arrays.equals(record.getType(),NdefRecord.RTD_TEXT)){
            return null;
        }

        try{
            //获取自己数组，进行分析
            byte[] payload = record.getPayload();
            //下面开始NDEF文本数据第一个自己，状态字节，
            //判断文本是基于UTF-8还是UTF-16 取第一个字节 位与  上16进制的80也就是最高位是1
            //其他位都是0 所以进行  位与   运算后保留最高位
            String textEncoding = ((payload[0] & 0x80) == 0)?"UTF-8":"UTF-16";
            //3f最高两位是0  第六位是1  所以进行位与 运算后获取第六位
            int languageCodeLength = payload[0] & 0x3f;
            //下面开始NDEF文本数据第二个字节 语言编码
            //获取语言编码
            String languageCode = new String(payload,1,languageCodeLength,"US-ASCII");
            //下面开始NDEF文本数据后面字节  解析出文本
            
            return new String(payload,languageCodeLength + 1,
                    payload.length - languageCodeLength - 1,textEncoding);
        }catch (Exception e){
            e.printStackTrace();
            Log.e("NFC","---------解析失败---------");

            return null;
        }
    }
}
