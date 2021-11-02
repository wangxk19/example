package com.shd.boomtruckpad.util.tcp;

import android.util.Log;

import com.tjstudy.tcplib.BaseRecParse;
import com.tjstudy.tcplib.utils.DigitalUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义转换规则
 * 自定义接收数据的协议
 * 示例：接收到的数据必须以$$(2个字节)开头 数据的大小（short,byte 2个字节 数据大小=2+2+数据大小）
 */

public class MyRecParse extends BaseRecParse<RecData> {
    private static final String TAG = "MyRecdataFilter";

    @Override
    public List<RecData> parse() {
        ArrayList<RecData> recDataList = new ArrayList<>();
        byte[] baseData = getBaseData();//总数据

        int removeSize = 0;
        byte[] head = new byte[2];

        for (int i = 0; i < baseData.length - 1; i++) {

        }
        notifyLeftData(removeSize);
        return recDataList;
    }
}
