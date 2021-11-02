package com.shd.boomtruckpad;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.ByteBufferEncoder;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import cn.com.heaton.blelibrary.ble.Ble;
import cn.com.heaton.blelibrary.ble.BleLog;
import cn.com.heaton.blelibrary.ble.callback.BleConnectCallback;
import cn.com.heaton.blelibrary.ble.callback.BleNotifyCallback;
import cn.com.heaton.blelibrary.ble.callback.BleReadCallback;
import cn.com.heaton.blelibrary.ble.callback.BleScanCallback;
import cn.com.heaton.blelibrary.ble.callback.BleStatusCallback;
import cn.com.heaton.blelibrary.ble.callback.BleWriteCallback;
import cn.com.heaton.blelibrary.ble.model.BleDevice;
import cn.com.heaton.blelibrary.ble.model.ScanRecord;
import cn.com.heaton.blelibrary.ble.utils.ByteUtils;
import cn.com.heaton.blelibrary.ble.utils.Utils;
import cn.com.heaton.blelibrary.ble.utils.UuidUtils;
import cn.com.superLei.aoparms.annotation.Permission;
import cn.com.superLei.aoparms.annotation.PermissionDenied;
import cn.com.superLei.aoparms.annotation.PermissionNoAskDenied;
import cn.com.superLei.aoparms.common.permission.AopPermissionUtils;

/**
 * @author: Jun
 * @Date: 2021/9/25 11:41
 * @Description:
 */

public class
SplashActivity extends Activity {
    private String TAG = SplashActivity.class.getSimpleName();
    public static final int REQUEST_PERMISSION_LOCATION = 2;
    public static final int REQUEST_PERMISSION_WRITE = 3;
    public static final int REQUEST_GPS = 4;
    private BluetoothGattCharacteristic writeBluetoothGattCharacteristic;

    private Ble<BleRssiDevice> ble = Ble.getInstance();
    private  BleDevice bleDevice;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ImageView cache = findViewById(R.id.cache);
        Glide.with(SplashActivity.this).load(R.drawable.cache).into(cache);
        initBleStatus();
        checkGpsStatus();
        requestPermission();

        /*ByteBuffer  tempBuffer =ByteBuffer.allocate(1024);
        byte[] optionData = {(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x10, 0x11, 0x12};

        tempBuffer.put(optionData);
        tempBuffer.flip();
        while (tempBuffer.limit()-tempBuffer.position()>10){
            byte temp=tempBuffer.get();
            Log.v("temp",temp+"");

        }
        LinkedList<Byte> linList=new LinkedList<>();
        for(byte b:optionData){
            linList.addLast(b);
        }
        while (linList.){

        }*/
    }




    //请求权限
    @Permission(value = {Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},
            requestCode = REQUEST_PERMISSION_LOCATION,
            rationale = "需要蓝牙相关权限")
    public void requestPermission() {

        checkBlueStatus();
    }

    @PermissionDenied
    public void permissionDenied(int requestCode, List<String> denyList) {
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            Log.e(TAG, "permissionDenied>>>:定位权限被拒 " + denyList.toString());
        } else if (requestCode == REQUEST_PERMISSION_WRITE) {
            Log.e(TAG, "permissionDenied>>>:读写权限被拒 " + denyList.toString());
        }
    }

    @PermissionNoAskDenied
    public void permissionNoAskDenied(int requestCode, List<String> denyNoAskList) {
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            Log.e(TAG, "permissionNoAskDenied 定位权限被拒>>>: " + denyNoAskList.toString());
        } else if (requestCode == REQUEST_PERMISSION_WRITE) {
            Log.e(TAG, "permissionDenied>>>:读写权限被拒>>> " + denyNoAskList.toString());
        }
        AopPermissionUtils.showGoSetting(this, "为了更好的体验，建议前往设置页面打开权限");
    }

    //监听蓝牙开关状态
    private void initBleStatus() {
        ble.setBleStatusCallback(new BleStatusCallback() {
            @Override
            public void onBluetoothStatusChanged(boolean isOn) {
                BleLog.i(TAG, "onBluetoothStatusOn: 蓝牙是否打开>>>>:" + isOn);
                if (isOn){
                    checkGpsStatus();
                }else {
                    if (ble.isScanning()) {
                        ble.stopScan();
                    }
                }
            }
        });
    }

    //检查蓝牙是否支持及打开
    private void checkBlueStatus() {
        if (!ble.isSupportBle(this)) {
            finish();
        }
        if (!ble.isBleEnable()) {
            checkGpsStatus();
        }
    }

    private void checkGpsStatus(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Utils.isGpsOpen(SplashActivity.this)){
            new AlertDialog.Builder(SplashActivity.this)
                    .setTitle("提示")
                    .setMessage("为了更精确的扫描到Bluetooth LE设备,请打开GPS定位")
                    .setPositiveButton("确定", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent,REQUEST_GPS);
                    })
                    .setNegativeButton("取消", null)
                    .create()
                    .show();
        }else {
            ble.startScan(scanCallback);
            Log.v("onLeScan","scanCallback");
        }
    }

    //蓝牙扫描监听
    private final BleScanCallback<BleRssiDevice> scanCallback = new BleScanCallback<BleRssiDevice>() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onLeScan(final BleRssiDevice device, int rssi, byte[] scanRecord) {
            synchronized (ble.getLocker()) {
                if (device.getBleName() != null && device.getBleName().contains("ATK-BLE")) {
                    if (ble.isScanning()) {
                        ble.stopScan();
                    }
                    //to do 获取陀螺仪的初始数据
                    startActivity(new Intent(SplashActivity.this, MainActivity.class).putExtra(MainActivity.EXTRA_TAG, device));
                }
            }
        }

        @Override
        public void onStart() {
            super.onStart();
        }

        @Override
        public void onStop() {
            super.onStop();
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(TAG, "onScanFailed: " + errorCode);
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == Ble.REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
        } else if (requestCode == Ble.REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            ble.startScan(scanCallback);
        }else if (requestCode == REQUEST_GPS){

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
