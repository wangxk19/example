package com.shd.boomtruckpad.util;

import com.shd.boomtruckpad.config.ConfigPara;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommonUtils {
    public   static int outPosition=0;
    public static float[] getXyzPoint(Map<String,Float> param){
        float β = param.get("β");     // 俯仰角
        float γ = param.get("γ");        // 水平旋转角
        float LO1I = param.get("LO1I");    // O'I长度（传感器和点之间的距离）
        float LO1U = ConfigPara.LO1U;    // O'U长度
        float LUW = ConfigPara.LUW;     // UW长度
        float z= (float) (LO1I * Math.sin(β *(Math.PI)  / 180));
        float y=(float) (LO1I * Math.cos(β * (Math.PI) / 180) * Math.sin(γ * (Math.PI) / 180));
        float x =(float) (LO1I * Math.cos(β * (Math.PI) / 180) * Math.cos(γ * (Math.PI) / 180)) ;
        //换算
        float[] returnF=new float[]{x,y,z};
        return returnF;
     }
     //解析data缓冲数据
     public static byte[] parseData(byte[] baseData){

         Byte[] bufferBytes = new Byte[1024];
         Byte[] aByte = new Byte[20];
         ByteBuffer buffer= ByteBuffer.allocate(256);
         int inPosition=0;
         int state=0;
         buffer.put(baseData);
         for(int i=0;i<baseData.length;i++){
             if(inPosition==1024){
                 inPosition=0;
             }
             inPosition=inPosition+i;
             bufferBytes[inPosition]=baseData[i];
         }
         int temp=0;

         List<Byte> tempList=new ArrayList<>();

         if(inPosition==1024){
             inPosition=0;
         }
         int tempContinue=0;
         while (outPosition>inPosition){
             if(bufferBytes[outPosition]==0x8a){
                 temp=bufferBytes[outPosition+1];
                 if(1024>=(outPosition+temp+2)){
                     outPosition=outPosition+temp+2;
                     return null;

                 }else {
                     tempContinue=(outPosition+temp+2)-1024;
                     if((outPosition+temp+2)-1024<inPosition){
                         outPosition=(outPosition+temp+2)-1024;

                     }
                 }
             }
             outPosition++;
             continue;
         }
         while (outPosition<inPosition){
             if(bufferBytes[outPosition]==0x8a){
                 tempList.add(bufferBytes[outPosition]);
                 temp=bufferBytes[outPosition+1];
                 if(inPosition>(outPosition+temp+2)){
                     outPosition=outPosition+temp+2;
                     return null;

                 }
             }
             outPosition++;
             continue;
         }




        return null;
     }

    public static byte[] readRule(byte[] baseData){

        int position=0;
        int state=0;
        for(int i=0;i<baseData.length;i++){
            position=position+i;
            baseData[position]=baseData[i];


        }

        return null;
    }
    private void switchMode (Byte b) {
        int state=0;
        switch (state){
            case 1:


        }

    }



    }
