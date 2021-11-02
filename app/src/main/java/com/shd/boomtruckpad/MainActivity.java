package com.shd.boomtruckpad;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothSocket;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.CacheDiskStaticUtils;
import com.blankj.utilcode.util.CacheDiskUtils;
import com.blankj.utilcode.util.CacheDoubleStaticUtils;
import com.bumptech.glide.Glide;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.github.mikephil.charting.charts.BarChart;
import com.hikvision.open.hikvideoplayer.HikVideoPlayer;
import com.hikvision.open.hikvideoplayer.HikVideoPlayerCallback;
import com.hikvision.open.hikvideoplayer.HikVideoPlayerFactory;
import com.shd.boomtruckpad.config.ConfigPara;
import com.shd.boomtruckpad.dialog.CustomEditTextDialog;
import com.shd.boomtruckpad.entity.VtDateValueBean;
import com.shd.boomtruckpad.util.CommonUtils;
import com.shd.boomtruckpad.util.DbController;
import com.shd.boomtruckpad.util.UtilBar;
import com.shd.boomtruckpad.view.PlayWindowContainer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cn.com.heaton.blelibrary.ble.Ble;
import cn.com.heaton.blelibrary.ble.callback.BleConnectCallback;
import cn.com.heaton.blelibrary.ble.callback.BleNotifyCallback;
import cn.com.heaton.blelibrary.ble.callback.BleReadCallback;
import cn.com.heaton.blelibrary.ble.callback.BleWriteCallback;
import cn.com.heaton.blelibrary.ble.model.BleDevice;
import cn.com.heaton.blelibrary.ble.utils.ByteUtils;
import cn.com.heaton.blelibrary.ble.utils.UuidUtils;

import static android.content.ContentValues.TAG;


/**
 * @author: Jun
 * @Date: 2021/7/10 21:49
 * @Description:
 */

public class MainActivity extends Activity implements RadioGroup.OnCheckedChangeListener , HikVideoPlayerCallback, TextureView.SurfaceTextureListener {
    public static final String EXTRA_TAG = "device";
    private String ipAddress = "192.168.0.178";
    private String ipAddressYun = "rtsp://admin:2016157s@192.168.31.245:554/ch1/main/av_stream";
    private int port = 4001;
    private RadioGroup mRgTop, rg_bottom, rg_option_left, rg_option_right, rg_option_fault, chooseType, rg_option_yun_left, rg_option_yun_right;
    private RadioButton mRbSupportLegs, mRbSportArm, mRbFault, mRbYunTab;
    private RadioButton rb_bottom_down, rb_bottom_up, rb_bottom_fault;
    private RadioButton rb_option_turntable, rb_option_bottom_arm, rb_option_top_arm, rb_option_platform, rb_option_in_arm;
    private RadioButton rb_option_leveling, rb_option_machine_hopper, rb_option_people_hopper, rb_option_machine_hopper_updown, rb_option_people_hopper_updown;
    private RadioButton rb_option_new, rb_option_all, speed, location, rb_yun_tab;
    private RadioButton rb_option_yun_tab, rb_option_yun_fy;//云台顺逆/俯仰按钮
    private TextView tv_input_value, tv_option_title1, tv_option_title2;
    private Button bt_inverse, bt_along, bt_yun_along, bt_yun_inverse;//云台顺逆/俯仰按钮

    //状态及回显数据
    private TextView tv_motor_speed, tv_bottom_voltage, tv_platform_voltage, tv_long_angle_voltage, tv_run_time, tv_pressure;
    private TextView tv_down_arm_angle, tv_up_arm_angle, tv_telescopic_boom_length, tv_hopper_x_angle, tv_hopper_y_angle;
    private TextView tv_people_hopper_angle, tv_people_hopper_height, tv_machine_hopper_angle, tv_machine_hopper_height;
    private TextView tv_turntable_angle, tv_turntable_angle1, tv_platform_angle;
    private TextView tv_down_x, tv_down_y;
    //中间的大块切换
    private LinearLayout ll_option_car_up, ll_option_fault, ll_option_yun_tab;
    private RelativeLayout rg_option_car_down;
    private ImageView iv_stop;
    //支腿操作
    private TextView tv_auto_out, tv_auto_in;
    private ImageView iv_car_leg;
    private ImageView iv_left_front_v, iv_right_front_v, iv_left_behind_v, iv_right_behind_v, iv_left_front_h, iv_right_front_h, iv_left_behind_h, iv_right_behind_h;
    //操作状态斗臂车
    private int optionMode = 1;
    //操作状态云台
    private int optionModeYt = 11;
    //故障
    private int faultMode = 1;
    //页面标识
    private int pageMode = 2;
    //急停标志
    private boolean stopFlag = false;
    //是否选择位置or速度
    private boolean isSpeed = true;
    //是否选择下车自动收缩状态
    private boolean autoIn = false;
    //是否选择下车自动伸出状态
    private boolean autoOut = false;
    private boolean mPlayerStatus;
    private HikVideoPlayer mPlayer;
    private TextureView textureView;
    private BarChart barChart;//上车布局进度条的视图
    private Button yt_count_point;//计算坐标按钮
    private TextView yt_count_X, yt_count_Y, yt_count_Z, yt_count_R;//坐标x y z r 的坐标值
    private TextView yt_horizontal_circle, yt_vertical_circle, yt_laser_distance;//激光测距，水平旋转，垂直旋转
    private TextView yt_input_value;//输入数值弹框按钮
    private float tLyData=0;//陀螺仪的角度
    private float tLyData_init=0;//初始陀螺仪数据，用于获取初始数据

    private boolean bTConnStatus =false;//蓝牙连接状态


    //运动到指定目标数据标识
    private boolean activityToTargetFlag = true;

    //处理左右两边的radiogroup
    private Boolean changeGroup = false;

    //故障列表
    private FaultAdapter mAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private List<FaultBean> mFaultList = new ArrayList<>();
    private RecyclerView rv_fault_list;

    //TCP通信
    private boolean isConnected = false;
    Socket socket = null;
    OutputStream writer = null;
    InputStream reader = null;
    private String line;
    //位置控制输入的值
    private int locationValue = 0;


    //操作信号都臂车
    byte[] optionData = {(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    //云台控制信号(上/下/左/右)
    byte[] optionDataYt = {(byte) 0x8A, 0x07, 0x01, (byte) 0xFF, 0x01, 0x00, 0x5b, 0x00, 0x00, 0x67, 0x73, 0x1F};

    //云台查询信号(水平)
    byte[] optionDataYtSelectH = {(byte) 0x8A, 0x07, 0x01, (byte) 0xFF, 0x01, 0x00, 0x51, 0x00, 0x00, 0x52, 0x71, 0x1F};
    //云台查询信号(垂直)
    byte[] optionDataYtSelectV = {(byte) 0x8A, 0x07, 0x01, (byte) 0xFF, 0x01, 0x00, 0x53, 0x00, 0x00, 0x54, 0x75, 0x1F};
    //陀螺仪开始查询数据
    byte[] optionDataTLyStart = {(byte) 0x8A, 0x06, 0x02,  (byte) 0xa5, 0x5a, 0x04, 0x01, 0x05, (byte)0xaa, (byte)0xdb, 0x1F};
    //陀螺仪结束查询数据
    byte[] optionDataTLyEnd= {(byte) 0x8A, 0x06, 0x02, (byte) 0xa5, 0x5a, 0x04, 0x02, 0x06,(byte)0xaa, (byte)0xdb, 0x1F};

    //动作目标值1
    byte[] activityTarget1 = {(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x02, (byte) 0x8b, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x00, 0x00, 0x00, 0x00};
    //动作目标值2
    byte[] activityTarget2 = {(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x03, (byte) 0x8b, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x00, 0x00, 0x00, 0x00};
    //控制信号
    byte[] controlData = {(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    //下车标志
    boolean downCarFlag = false;

    //获取当前时间
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// HH:mm:ss
    Date date = null;

    private boolean touchFlag = false;//斗臂车按钮点击状态
    private boolean touchFlagYt = false;//云台按钮点击状态

    private boolean isAlong = false;//斗臂车运动状态
    private boolean isYtAlong = false;//云台顺逆/俯仰控制状态
    //蓝牙相关参数
    private BleDevice bleDevice;
    private Ble<BleDevice> ble;
    private BluetoothGattCharacteristic writeBluetoothGattCharacteristic;
    private PlayWindowContainer frameLayout;

    private TextView mb_yt_x,mb_yt_y,mb_yt_z,mb_yt_r,sj_yt_x,sj_yt_y,sj_yt_z,sj_yt_r;//实际/目标点坐标

    private float ytFyAngle;//云台俯仰角
    private float ytSpAngle;//云台水平角
    private float LQC;
    private float LQC_init=0.0998f;
    private float LDG;
    private float LDG_init=0.1529f;

    private float LHI;
    private float LHI_init=0.1359f;

    private float LO1I;
    private ImageView btImage;//蓝牙图标
    private ImageView  btImageM;//蓝牙图标
    private double x,y,z,r;
    private double x_n,y_n,z_n,r_n;
    private ArrayList<VtDateValueBean> dateValueList;
    private UtilBar utilBar;
    private RadioButton rb_option_yun_ct,rb_option_yun_wt;//粗调/微调按钮
    private int yt_speed=15;
    private int yt_speed_fy=10;
    private Python py;
    private PyObject pyo;
    private PyObject pyo1;
    private JSONObject json = null;
    private float temp_TlyData=0;//获取初始陀螺仪数据
    //private float tempData;//获取初始陀螺仪数据
    private RadioButton init_data_state;//开始测量按钮
    private boolean cl_status=false;//开始测量按钮
    private boolean isCount=false;
    private Button get_init_data;//获取初始数据按钮


//    手动-------------》比例
//    自动-------------》位置

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initListener();
        //connect();
        //sendDataHandler.postDelayed(runnable, 500);
        //蓝牙连接
        if (Build.VERSION.SDK_INT >= 6.0) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        }
        initData();
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(MainActivity.this));
        }
        py=Python.getInstance ();
        pyo=py.getModule ("myscript");
        utilBar = new UtilBar();
        utilBar.initBarChart(barChart, getResources().getColor(R.color.colorPrimary));
        dateValueList = new ArrayList<VtDateValueBean>();
    }

    ////////////////////////////////////////////////////蓝牙//////////////////////////////////////////////////////////
    private void initData() {
        ble = Ble.getInstance();
        bleDevice = getIntent().getParcelableExtra(EXTRA_TAG);
        if (bleDevice == null) return;
        ble.connect(bleDevice, connectCallback);
    }


    //蓝牙连接监听
    private BleConnectCallback<BleDevice> connectCallback = new BleConnectCallback<BleDevice>() {
        @Override
        public void onConnectionChanged(BleDevice device) {
            Log.e(TAG, "onConnectionChanged: " + device.getConnectionState() + Thread.currentThread().getName());
            if (device.isConnected()) {
                Log.v("bleRssiDevicesco", "已连接");
                Ble<BleDevice> ble = Ble.getInstance();
                if (ble.isScanning()) {
                    ble.stopScan();
                }
                bTConnStatus=true;
                //8A 06 02 A5 5A 04 01 05 AA DB 1F 开始发送查询陀螺仪的命令
                write(writeBluetoothGattCharacteristic, null,optionDataTLyStart);
                //Log.v("陀螺仪",Arrays.toString(optionDataTLyStart));

                btImage.setImageResource(R.mipmap.booth_tooth_2);//设置蓝牙连接成功照片
                btImageM.setImageResource(R.mipmap.booth_tooth_2);//设置蓝牙连接成功照片
                sendDataHandlerBt.postDelayed(runnableBT, 500);//连接后开始轮询查询水平/垂直角度数据
            } else if (device.isConnecting()) {
                Log.v("bleRssiDevicesco", "连接中");
                bTConnStatus=false;
                btImage.setImageResource(R.mipmap.booth_tooth1);//设置蓝牙连接失败照片
                btImageM.setImageResource(R.mipmap.booth_tooth1);//设置蓝牙连接失败照片
            } else if (device.isDisconnected()) {
                Log.v("bleRssiDevicesco", "未连接");
                bTConnStatus=false;
                btImage.setImageResource(R.mipmap.booth_tooth1);//设置蓝牙连接失败照片
                btImageM.setImageResource(R.mipmap.booth_tooth1);//设置蓝牙连接失败照片
            }
        }

        @Override
        public void onConnectFailed(BleDevice device, int errorCode) {
            super.onConnectFailed(device, errorCode);
            Log.v("bleRssiDevices", "连接异常，异常状态码:" + errorCode);
        }

        @Override
        public void onConnectCancel(BleDevice device) {
            super.onConnectCancel(device);
            Log.v("bleRssiDevices", "onConnectCancel: " + device.getBleName());

        }
        //蓝牙接受消息
        @Override
        public void onServicesDiscovered(BleDevice device, BluetoothGatt gatt) {
            super.onServicesDiscovered(device, gatt);
            for (int i = 0; i < gatt.getServices().size(); i++) {
                if (!UuidUtils.isBaseUUID(gatt.getServices().get(i).getUuid().toString())) {
                    //获取非BaseUUID的service
                    for (int j = 0; j < gatt.getServices().get(i).getCharacteristics().size(); j++) {
                        int charaProp = gatt.getServices().get(i).getCharacteristics().get(j).getProperties();
                        List<BleDevice> connetedDevices = Ble.getInstance().getConnectedDevices();
                        UUID serviceUuid = gatt.getServices().get(i).getCharacteristics().get(j).getService().getUuid();
                        UUID characteristicUuid = gatt.getServices().get(i).getCharacteristics().get(j).getUuid();
                        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0 || (charaProp & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                            Ble.getInstance().enableNotifyByUuid(
                                    connetedDevices.get(0),
                                    true,
                                    serviceUuid,
                                    characteristicUuid,
                                    new BleNotifyCallback<BleDevice>() {
                                        @Override
                                        public void onChanged(BleDevice device, BluetoothGattCharacteristic characteristic) {
                                            Log.v("bleRssiDevices",String.format("Notifications enabled\nvalue: %s", ByteUtils.bytes2HexStr(characteristic.getValue())));
                                            parseYt(characteristic.getValue());
                                            /*if (characteristic.getValue()[1] == 0x06&&characteristic.getValue().length==11) {
                                                Log.v("接受陀螺仪的数据", Arrays.toString(characteristic.getValue()) );
                                                tempData = (float) ((short) (((characteristic.getValue()[3] & 0x00FF) << 8) |(0x00FF & characteristic.getValue()[4]) )/10);
                                            }*/
                                        }
                                        @Override
                                        public void onNotifySuccess(BleDevice device) {
                                            super.onNotifySuccess(device);
                                            Log.v("bleRssiDevices", "bleRssiDevices onNotifySuccess: 11" );


                                        }

                                        @Override
                                        public void onNotifyCanceled(BleDevice device) {
                                            super.onNotifyCanceled(device);

                                        }
                                    });
                        }
                        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) != 0) {

                            if (!connetedDevices.isEmpty()){
                                Ble.getInstance().readByUuid(
                                        connetedDevices.get(0),
                                        serviceUuid,
                                        characteristicUuid,
                                        new BleReadCallback<BleDevice>() {
                                            @Override
                                            public void onReadSuccess(BleDevice dedvice, BluetoothGattCharacteristic characteristic) {
                                                super.onReadSuccess(dedvice, characteristic);
                                                Log.v("bleRssiDevices", String.format("value: %s%s","(0x)", ByteUtils.bytes2HexStr(characteristic.getValue())));

                                            }

                                            @Override
                                            public void onReadFailed(BleDevice device, int failedCode) {
                                                super.onReadFailed(device, failedCode);
                                                Log.v("bleRssiDevices","读取特征失败:"+failedCode);
                                            }
                                        });
                            }
                        }
                        //可写的节点
                        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0 || (charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {

                            writeBluetoothGattCharacteristic = gatt.getServices().get(i).getCharacteristics().get(j);

                        }

                    }
                }
            }


        }

        @Override
        public void onReady(BleDevice device) {
            super.onReady(device);
            //连接成功后，设置通知
//            ble.enableNotify(device, true, new BleNotifyCallback<BleDevice>() {
//                @Override
//                public void onChanged(BleDevice device, BluetoothGattCharacteristic characteristic) {
//                    UUID uuid = characteristic.getUuid();
//                    Log.v("bleRssiDevices", "bleRssiDevices onChanged==uuid:" + uuid.toString());
//                    Log.v("bleRssiDevices", "bleRssiDevices onChanged==data:" + ByteUtils.toHexString(characteristic.getValue()));
//
//                }
//
//                @Override
//                public void onNotifySuccess(BleDevice device) {
//                    super.onNotifySuccess(device);
//                    Log.v("bleRssiDevices","bleRssiDevices onNotifySuccess: " + device.getBleName());
//                }
//            });
        }

    };

    //发送蓝牙数据
    private void write(BluetoothGattCharacteristic characteristic, BluetoothGattDescriptor descriptor, byte[] bytes){
        List<BleDevice> connetedDevices = Ble.getInstance().getConnectedDevices();
        UUID serviceUuid = characteristic.getService().getUuid();
        UUID characteristicUuid = characteristic.getUuid();

        if (!connetedDevices.isEmpty()){
            BleDevice bleDevice = connetedDevices.get(0);
            writeChar(bleDevice, bytes, serviceUuid, characteristicUuid);

        }
    }
    //发送蓝牙数据Char
    private void writeChar(BleDevice bleDevice, byte[] bytes, UUID serviceUuid, UUID characteristicUuid){
        Ble.getInstance().writeByUuid(
                bleDevice,
                bytes,
                serviceUuid,
                characteristicUuid,
                new BleWriteCallback<BleDevice>() {
                    @Override
                    public void onWriteSuccess(BleDevice device, BluetoothGattCharacteristic characteristic) {

                    }

                    @Override
                    public void onWriteFailed(BleDevice device, int failedCode) {
                        super.onWriteFailed(device, failedCode);

                    }
                });
    }


////////////////////////////////////////////////////蓝牙//////////////////////////////////////////////////////////


        private void initView () {
            tv_motor_speed = findViewById(R.id.tv_motor_speed);
            tv_pressure = findViewById(R.id.tv_pressure);
            tv_bottom_voltage = findViewById(R.id.tv_bottom_voltage);
            tv_platform_voltage = findViewById(R.id.tv_platform_voltage);
            tv_long_angle_voltage = findViewById(R.id.tv_long_angle_voltage);
            tv_run_time = findViewById(R.id.tv_run_time);
            tv_down_arm_angle = findViewById(R.id.tv_down_arm_angle);
            tv_up_arm_angle = findViewById(R.id.tv_up_arm_angle);
            tv_telescopic_boom_length = findViewById(R.id.tv_telescopic_boom_length);
            tv_hopper_x_angle = findViewById(R.id.tv_hopper_x_angle);
            tv_hopper_y_angle = findViewById(R.id.tv_hopper_y_angle);
            chooseType = findViewById(R.id.chooseType);


            tv_people_hopper_angle = findViewById(R.id.tv_people_hopper_angle);
            tv_people_hopper_height = findViewById(R.id.tv_people_hopper_height);
            tv_machine_hopper_angle = findViewById(R.id.tv_machine_hopper_angle);
            tv_machine_hopper_height = findViewById(R.id.tv_machine_hopper_height);
            tv_turntable_angle = findViewById(R.id.tv_turntable_angle);
            tv_turntable_angle1 = findViewById(R.id.tv_turntable_angle1);
            tv_platform_angle = findViewById(R.id.tv_platform_angle);


            iv_stop = findViewById(R.id.iv_stop);

            tv_down_x = findViewById(R.id.tv_down_x);
            tv_down_y = findViewById(R.id.tv_down_y);

            ll_option_car_up = findViewById(R.id.ll_option_car_up);
            ll_option_fault = findViewById(R.id.ll_option_fault);
            ll_option_yun_tab = findViewById(R.id.ll_option_yun_tab);
            rg_option_car_down = findViewById(R.id.rg_option_car_down);
            ll_option_car_up.setVisibility(View.VISIBLE);
            rg_option_car_down.setVisibility(View.GONE);
            ll_option_fault.setVisibility(View.GONE);

            tv_auto_out = findViewById(R.id.tv_auto_out);
            tv_auto_in = findViewById(R.id.tv_auto_in);
            iv_car_leg = findViewById(R.id.iv_car_leg);
            iv_left_front_v = findViewById(R.id.iv_left_front_v);
            iv_right_front_v = findViewById(R.id.iv_right_front_v);
            iv_left_behind_v = findViewById(R.id.iv_left_behind_v);
            iv_right_behind_v = findViewById(R.id.iv_right_behind_v);
            iv_left_front_h = findViewById(R.id.iv_left_front_h);
            iv_right_front_h = findViewById(R.id.iv_right_front_h);
            iv_left_behind_h = findViewById(R.id.iv_left_behind_h);
            iv_right_behind_h = findViewById(R.id.iv_right_behind_h);

            rv_fault_list = findViewById(R.id.rv_fault_list);

            rg_option_fault = findViewById(R.id.rg_option_fault);
            tv_input_value = findViewById(R.id.tv_input_value);
            tv_option_title1 = findViewById(R.id.tv_option_title1);
            tv_option_title2 = findViewById(R.id.tv_option_title2);

            bt_inverse = findViewById(R.id.bt_inverse);
            bt_along = findViewById(R.id.bt_along);
            bt_yun_inverse = findViewById(R.id.bt_yun_inverse);
            bt_yun_along = findViewById(R.id.bt_yun_along);

            mRgTop = findViewById(R.id.rg_top);
            mRbSupportLegs = findViewById(R.id.rb_support_legs);
            mRbYunTab = findViewById(R.id.rb_main_yun_tab);
            mRbSportArm = findViewById(R.id.rb_sport_arm);
            mRbFault = findViewById(R.id.rb_fault);
            rg_option_left = findViewById(R.id.rg_option_left);
            rg_option_right = findViewById(R.id.rg_option_right);
            rg_option_yun_left = findViewById(R.id.rg_option_yun_left);
            rg_option_yun_right = findViewById(R.id.rg_option_yun_right);
            rg_bottom = findViewById(R.id.rg_bottom);

            rb_bottom_down = findViewById(R.id.rb_bottom_down);
            rb_bottom_up = findViewById(R.id.rb_bottom_up);

            rb_bottom_fault = findViewById(R.id.rb_bottom_fault);

            rb_option_new = findViewById(R.id.rb_option_new);
            rb_option_all = findViewById(R.id.rb_option_all);
            rb_option_turntable = findViewById(R.id.rb_option_turntable);
            rb_option_bottom_arm = findViewById(R.id.rb_option_bottom_arm);
            rb_option_top_arm = findViewById(R.id.rb_option_top_arm);
            rb_option_platform = findViewById(R.id.rb_option_platform);
            rb_option_in_arm = findViewById(R.id.rb_option_in_arm);
            //速度控制按钮
            speed = findViewById(R.id.speed);
            //位置控制按钮
            location = findViewById(R.id.location);
            //云台控制按钮
            rb_yun_tab = findViewById(R.id.rb_yun_tab);


            rb_option_leveling = findViewById(R.id.rb_option_leveling);
            rb_option_machine_hopper = findViewById(R.id.rb_option_machine_hopper);
            rb_option_people_hopper = findViewById(R.id.rb_option_people_hopper);
            rb_option_machine_hopper_updown = findViewById(R.id.rb_option_machine_hopper_updown);
            rb_option_people_hopper_updown = findViewById(R.id.rb_option_people_hopper_updown);
            yt_horizontal_circle=findViewById(R.id.yt_horizontal_circle);
            yt_vertical_circle=findViewById(R.id.yt_vertical_circle);

            yt_laser_distance=findViewById(R.id.yt_laser_distance);

            //云台控制按钮获取
            rb_option_yun_fy = findViewById(R.id.rb_option_yun_fy);
            rb_option_yun_tab = findViewById(R.id.rb_option_yun_tab);
            //videoView=findViewById(R.id.surface_view);

            mFaultList.add(new FaultBean(1, "下车急停报警", "2021-07-10 10:38"));
            mFaultList.add(new FaultBean(2, "伸缩开关不在初始位置", "2021-07-10 10:38"));
            mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            mAdapter = new FaultAdapter(MainActivity.this, mFaultList);
            rv_fault_list.setLayoutManager(mLinearLayoutManager);
            rv_fault_list.setAdapter(mAdapter);
            //云台一些按钮
            yt_input_value = findViewById(R.id.yt_input_value);
            mb_yt_x= findViewById(R.id.mb_yt_x);//目标点x
            mb_yt_y= findViewById(R.id.mb_yt_y);//目标点y
            mb_yt_z= findViewById(R.id.mb_yt_z);//目标点z
            mb_yt_r= findViewById(R.id.mb_yt_r);//目标点r
            sj_yt_x= findViewById(R.id.sj_yt_x);//实际点x
            sj_yt_y= findViewById(R.id.sj_yt_y);//实际点y
            sj_yt_z= findViewById(R.id.sj_yt_z);//实际点z
            sj_yt_r= findViewById(R.id.sj_yt_r);//实际点r
            frameLayout = findViewById(R.id.frame_layout);//云台播放窗口
            //增加云台代码
            frameLayout = findViewById(R.id.frame_layout);
            mPlayer = HikVideoPlayerFactory.provideHikVideoPlayer();
            //设置默认值
            mPlayer.setHardDecodePlay(true);
            mPlayer.setSmartDetect(true);
            textureView = findViewById(R.id.texture_view);
            textureView.setSurfaceTextureListener(this);
            yt_count_point=findViewById(R.id.yt_count_point);//计算坐标按钮



            yt_count_X=findViewById(R.id.yt_count_X);
            yt_count_Y=findViewById(R.id.yt_count_Y);
            yt_count_Z=findViewById(R.id.yt_count_Z);
            yt_count_R=findViewById(R.id.yt_count_R);

            barChart = findViewById(R.id.barchar);//柱状图
            //蓝牙图标
            btImage= findViewById(R.id.btImage);
            btImageM= findViewById(R.id.btImageM);
            //粗调/微调按钮
            rb_option_yun_ct=findViewById(R.id.rb_option_yun_ct);
            rb_option_yun_wt=findViewById(R.id.rb_option_yun_wt);
            //开始测量按钮
            init_data_state=findViewById(R.id.init_data_state);
            get_init_data=findViewById(R.id.get_init_data);
        }


        private void initListener () {
            tv_auto_out.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Glide.with(MainActivity.this).load(R.drawable.gif_leg_out).into(iv_car_leg);
                    iv_left_front_h.setImageResource(R.mipmap.icon_x_left_arrow_gray);
                    iv_left_behind_h.setImageResource(R.mipmap.icon_x_left_arrow_gray);
                    iv_right_front_h.setImageResource(R.mipmap.icon_x_right_arrow_gray);
                    iv_right_behind_h.setImageResource(R.mipmap.icon_x_right_arrow_gray);
                    iv_left_front_v.setImageResource(R.mipmap.icon_y_arrow_not_in_place);
                    iv_left_behind_v.setImageResource(R.mipmap.icon_y_arrow_not_in_place);
                    iv_right_front_v.setImageResource(R.mipmap.icon_y_arrow_not_in_place);
                    iv_right_behind_v.setImageResource(R.mipmap.icon_y_arrow_not_in_place);
                    optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x00, 0x40, 0x00, 0x13, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                    //必须保持3秒
                    autoOut = true;//赋值给自动伸出状态true
                    touchFlag = true;
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            touchFlag = false;
                        }
                    }, 3000);
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            autoOut = false;
                        }
                    }, 3000);
                    return false;
                }


            });
            tv_auto_in.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Glide.with(MainActivity.this).load(R.drawable.gif_leg_in).into(iv_car_leg);
                    iv_left_front_h.setImageResource(R.mipmap.icon_x_right_arrow_gray);
                    iv_left_behind_h.setImageResource(R.mipmap.icon_x_right_arrow_gray);
                    iv_right_front_h.setImageResource(R.mipmap.icon_x_left_arrow_gray);
                    iv_right_behind_h.setImageResource(R.mipmap.icon_x_left_arrow_gray);
                    iv_left_front_v.setImageResource(R.mipmap.icon_y_arrow_not_in_place);
                    iv_left_behind_v.setImageResource(R.mipmap.icon_y_arrow_not_in_place);
                    iv_right_front_v.setImageResource(R.mipmap.icon_y_arrow_not_in_place);
                    iv_right_behind_v.setImageResource(R.mipmap.icon_y_arrow_not_in_place);
                    optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x00, 0x40, 0x00, 0x15, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                    autoIn = true;//赋值给自动收缩状态true
                    touchFlag = true;
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            touchFlag = false;
                        }
                    }, 3000);
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            autoIn = false;
                        }
                    }, 3000);
                    return false;
                }
            });
            rg_option_left.setOnCheckedChangeListener(this);
            rg_option_right.setOnCheckedChangeListener(this);
            rg_option_yun_left.setOnCheckedChangeListener(this);
            rg_option_yun_right.setOnCheckedChangeListener(this);


            iv_stop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    optionDataYt = new byte[]{(byte) 0x8A, 0x07, 0x01, (byte) 0xFF, 0x01, 0x00, 0x00, 0x00, 0x00, 0x01, 0x73, 0x1F};
                    write(writeBluetoothGattCharacteristic, null,optionDataYt);
                    stopFlag = !stopFlag;
                    if (stopFlag) {
                        //操作信号 自动操作到位后要清除掉自动使能信号
                        optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                        //动作目标值1
                        activityTarget1 = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x02, (byte) 0x8b, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x00, 0x00, 0x00, 0x00};
                        //动作目标值2
                        activityTarget2 = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x03, (byte) 0x8b, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x00, 0x00, 0x00, 0x00};


                        activityToTargetFlag = true;
                        iv_stop.setImageResource(R.mipmap.icon_stop_start);
                    } else {
                        optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x00, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                        iv_stop.setImageResource(R.mipmap.icon_stop_stop);

                    }
                }
            });

            bt_inverse.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    isAlong = false;
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        touchFlag = false;
                    }
                    if (event.getAction() == MotionEvent.ACTION_BUTTON_PRESS ||
                            event.getAction() == MotionEvent.ACTION_DOWN ||
                            event.getAction() == MotionEvent.ACTION_HOVER_ENTER ||
                            event.getAction() == MotionEvent.ACTION_MASK ||
                            event.getAction() == MotionEvent.ACTION_POINTER_DOWN ||
                            event.getAction() == MotionEvent.ACTION_MOVE) {
                        if (optionMode == 9 || optionMode == 10) {
                            sendCmd((byte) 60);
                        } else if (optionMode == 5 || optionMode == 7 || optionMode == 8) {
                            sendCmd((byte) 60);
                        } else {
                            sendCmd((byte) 40);
                        }
                        touchFlag = true;
                    } else {
                        touchFlag = false;
                    }
                    return false;
                }
            });
            bt_along.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    isAlong = true;
                /*if (event.getAction() == MotionEvent.ACTION_UP) {
                    touchFlag = false;
                }*/
                    if (event.getAction() == MotionEvent.ACTION_BUTTON_PRESS ||
                            event.getAction() == MotionEvent.ACTION_DOWN ||
                            event.getAction() == MotionEvent.ACTION_HOVER_ENTER ||
                            event.getAction() == MotionEvent.ACTION_MASK ||
                            event.getAction() == MotionEvent.ACTION_POINTER_DOWN ||
                            event.getAction() == MotionEvent.ACTION_MOVE) {
                        if (optionMode == 9 || optionMode == 10) {
                            sendCmd((byte) 200);
                        } else if (optionMode == 5 || optionMode == 7 || optionMode == 8) {
                            sendCmd((byte) 200);
                        } else {
                            sendCmd((byte) 200);
                        }
                        touchFlag = true;
                    } else {
                        touchFlag = false;
                    }
                    isAlong = true;
                    return false;
                }
            });

            tv_input_value.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (stopFlag) {
                        Toast.makeText(MainActivity.this, "急停状态不允许该操作！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    initAndShowDialog();
                }
            });
            yt_input_value.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (stopFlag) {
                        Toast.makeText(MainActivity.this, "急停状态不允许该操作！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    initAndShowDialogYt();
                }
            });
            mRbSportArm.setChecked(true);
            rb_bottom_up.setChecked(true);
            rb_option_turntable.setChecked(true);
            rb_option_new.setChecked(true);
            rg_option_fault.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.rb_option_new) {
                        faultMode = 1;
                    } else if (checkedId == R.id.rb_option_all) {
                        faultMode = 2;
                    }
                }
            });
            rg_bottom.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == rb_bottom_down.getId()) {
                        downCarFlag = true;
                        pageMode = 1;
                        mRbSupportLegs.setChecked(true);
                        tv_option_title1.setBackgroundColor(getResources().getColor(R.color.color_violet));
                        tv_option_title1.setText("");
                        tv_option_title2.setBackgroundColor(getResources().getColor(R.color.color_violet));
                        tv_option_title2.setText("");
                        ll_option_car_up.setVisibility(View.GONE);
                        rg_option_car_down.setVisibility(View.VISIBLE);
                        ll_option_yun_tab.setVisibility(View.GONE);
                        ll_option_fault.setVisibility(View.GONE);
                    } else if (checkedId == rb_bottom_up.getId()) {
                        downCarFlag = false;
                        pageMode = 2;
                        mRbSportArm.setChecked(true);
                        tv_option_title1.setBackgroundColor(getResources().getColor(R.color.color_option_bg));
                        tv_option_title1.setText("操作区");
                        tv_option_title2.setBackgroundColor(getResources().getColor(R.color.color_option_bg));
                        tv_option_title2.setText("操作区");
                        ll_option_car_up.setVisibility(View.VISIBLE);
                        rg_option_car_down.setVisibility(View.GONE);
                        ll_option_fault.setVisibility(View.GONE);
                        ll_option_yun_tab.setVisibility(View.GONE);
                    } else if (checkedId == rb_bottom_fault.getId()) {
                        downCarFlag = false;
                        pageMode = 3;
                        mRbFault.setChecked(true);
                        tv_option_title1.setBackgroundColor(getResources().getColor(R.color.color_option_bg));
                        tv_option_title1.setText("故障记录");
                        tv_option_title2.setBackgroundColor(getResources().getColor(R.color.color_option_bg));
                        tv_option_title2.setText("故障记录");
                        ll_option_car_up.setVisibility(View.GONE);
                        rg_option_car_down.setVisibility(View.GONE);
                        ll_option_fault.setVisibility(View.VISIBLE);
                        ll_option_yun_tab.setVisibility(View.GONE);

                    } else if (checkedId == rb_yun_tab.getId()) {
                        downCarFlag = false;
                        pageMode = 4;
                        mRbYunTab.setChecked(true);
                        rb_option_yun_tab.setChecked(true);
                        tv_option_title1.setBackgroundColor(getResources().getColor(R.color.color_violet));
                        tv_option_title1.setText("");
                        tv_option_title2.setBackgroundColor(getResources().getColor(R.color.color_violet));
                        tv_option_title2.setText("");
                        ll_option_car_up.setVisibility(View.GONE);
                        rg_option_car_down.setVisibility(View.GONE);
                        ll_option_fault.setVisibility(View.GONE);
                        ll_option_yun_tab.setVisibility(View.VISIBLE);
                        //play rtsp stream
                        new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                                if (!mPlayerStatus) {
                                    startRealPlay(textureView.getSurfaceTexture());
                                }
                            }
                        }, 1000);


                    }
                }
            });
            //增加是否选择速度控制还是位置控制的按钮
            chooseType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.speed) {//注意此处id的写法
                        optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x00, 0x50, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                        isSpeed = true;
                        Log.v("speed选中", "s");
                    } else {
                        optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x00, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                        controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x00, 0x00, 0x0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                        isSpeed = false;
                        Log.v("location选择", "s");
                    }
                }
            });
            //增加云台顺逆和俯仰控制监听
            rb_option_yun_wt.setChecked(true);
            bt_yun_inverse.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    isYtAlong = false;
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        touchFlagYt = false;
                        sendCmdYt((byte) yt_speed,(byte) yt_speed_fy);
                    }
                    if (event.getAction() == MotionEvent.ACTION_DOWN ) {
                        touchFlagYt = true;

                        sendCmdYt((byte) yt_speed,(byte) yt_speed_fy);
                    }
                    return false;
                }
            });
            bt_yun_along.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    isYtAlong = true;
                    if (event.getAction() == MotionEvent.ACTION_DOWN ) {
                        touchFlagYt = true;
                        sendCmdYt((byte) yt_speed,(byte) yt_speed_fy);

                    }
                    if (event.getAction() == MotionEvent.ACTION_UP )  {
                        touchFlagYt = false;
                        sendCmdYt((byte) yt_speed,(byte) yt_speed_fy);
                    }
                    return false;
                }
            });
            //云天计算坐标按钮点击事件
            yt_count_point.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Map<String, Float> paraMap = new HashMap<String, Float>();
                    isCount=true;
                    paraMap.put("β",ytFyAngle);
                    paraMap.put("γ",ytSpAngle);
                    paraMap.put("LO1I",LO1I);
                    //云天计算坐标
                    float[] xyzPoint = CommonUtils.getXyzPoint(paraMap);
                    yt_count_X.setText(String.format("%.3f", xyzPoint[0]));
                    yt_count_Y.setText(String.format("%.3f", xyzPoint[1]));
                    yt_count_Z.setText(String.format("%.3f", xyzPoint[2]));


                    x_n=xyzPoint[0];y_n=xyzPoint[1];z_n=xyzPoint[2];
                    mb_yt_x.setText(String.format("%.3f", xyzPoint[0]));
                    mb_yt_y.setText(String.format("%.3f", xyzPoint[1]));
                    mb_yt_z.setText(String.format("%.3f", xyzPoint[2]));
                    mb_yt_r.setText(tLyData+"");
                    r=tLyData;//目标点陀螺仪的数据

                }
            });
            rb_option_yun_wt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rb_option_yun_wt.setChecked(true);
                    rb_option_yun_ct.setChecked(false);
                    yt_speed=10;
                    yt_speed_fy=5;
                }

            });
            rb_option_yun_ct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rb_option_yun_wt.setChecked(false);
                    rb_option_yun_ct.setChecked(true);
                    yt_speed=50;
                    yt_speed_fy=20;
                }
            });
            init_data_state.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(cl_status){
                        init_data_state.setChecked(true);
                        init_data_state.setText("开始测量");
                        cl_status=false;
                    }else {
                        init_data_state.setText("结束测量");
                        cl_status=true;

                    }
                    temp_TlyData=tLyData;
                }
            });
            get_init_data.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LQC_init=LQC;
                    LDG_init=LDG;
                    LHI_init=LHI;
                    tLyData_init=tLyData;
                    Log.v("获取初始数据","LQC=="+LQC_init+" LDG=="+LDG_init+" LHI=="+LHI_init+" tLyData=="+tLyData_init);
                }
            });

        }


        @Override
        public boolean onKeyDown ( int keyCode, KeyEvent event){
            Log.d(TAG, "手柄按键:" + keyCode);
            switch (keyCode) {
                //上
                case 19:
                    if (pageMode == 2) {
                        if (optionMode > 1) {
                            optionMode -= 1;
                        }
                    } else if (pageMode == 3) {
                        if (rg_option_fault.getCheckedRadioButtonId() == R.id.rb_option_all) {
                            rb_option_new.setChecked(true);
                        }
                    }
                    break;
                //下
                case 20:
                    if (pageMode == 2) {
                        if (optionMode < 10) {
                            optionMode += 1;
                        }
                    } else if (pageMode == 3) {
                        if (rg_option_fault.getCheckedRadioButtonId() == R.id.rb_option_new) {
                            rb_option_all.setChecked(true);
                        }
                    }
                    break;
                //左
                case 21:
                    if (pageMode == 2) {
                        if (optionMode > 5) {
                            optionMode -= 5;
                        }
                    }
                    break;
                //右
                case 22:
                    if (pageMode == 2) {
                        if (optionMode < 6)
                            optionMode += 5;
                    }
                    break;
                case 96:
                    if (pageMode == 2) {
                        initAndShowDialog();
                    }
                    break;
                case 97:
                    rb_bottom_fault.setChecked(true);
                    break;
                case 99:
                    rb_bottom_up.setChecked(true);
                    break;
                case 100:
                    rb_bottom_down.setChecked(true);
                    break;
                case 108:
                    if (pageMode == 1) {
                        Glide.with(MainActivity.this).load(R.drawable.gif_leg_out).into(iv_car_leg);
                    } else if (pageMode == 2) {
                        sendCmd((byte) 100);
                    }

                    break;
                case 109:
                    if (pageMode == 1) {
                        Glide.with(MainActivity.this).load(R.drawable.gif_leg_in).into(iv_car_leg);
                    } else if (pageMode == 2) {
                        sendCmd((byte) 100);
                    }

                    break;
            }
            switchMode();
            return true;
        }

        private void switchMode () {
            switch (optionMode) {
                case 1:
                    rb_option_turntable.setChecked(true);
                    break;
                case 2:
                    rb_option_bottom_arm.setChecked(true);
                    break;
                case 3:
                    rb_option_top_arm.setChecked(true);
                    break;
                case 4:
                    rb_option_platform.setChecked(true);
                    break;
                case 5:
                    rb_option_in_arm.setChecked(true);
                    break;
                case 6:
                    rb_option_leveling.setChecked(true);
                    break;
                case 7:
                    rb_option_machine_hopper.setChecked(true);
                    break;
                case 8:
                    rb_option_people_hopper.setChecked(true);
                    break;
                case 9:
                    rb_option_machine_hopper_updown.setChecked(true);
                    break;
                case 10:
                    rb_option_people_hopper_updown.setChecked(true);
                    break;
            }
        }

        //选择按钮改变事件
        @SuppressLint("ResourceType")
        @Override
        public void onCheckedChanged (RadioGroup group,int checkedId){

            if (stopFlag) {
                Toast.makeText(MainActivity.this, "急停状态不允许该操作！", Toast.LENGTH_SHORT).show();
                return;
            }
            //增加判断位置控制操作时（还未到位），不允许进行另外操作
            if (controlData[11] != 0) {
                Toast.makeText(MainActivity.this, "位置控制还未到位，不允许操作另外动作！", Toast.LENGTH_SHORT).show();
                return;
            }
            if (group != null && checkedId > -1 && changeGroup == false) {
                if (group == rg_option_left) {
                    changeGroup = true;
                    rg_option_right.clearCheck();
                    changeGroup = false;
                } else if (group == rg_option_right) {
                    changeGroup = true;
                    rg_option_left.clearCheck();
                    changeGroup = false;
                } else if (group == rg_option_yun_left) {
                    changeGroup = true;
                    rg_option_yun_right.clearCheck();
                    changeGroup = false;

                } else if (group == rg_option_yun_right) {
                    changeGroup = true;
                    rg_option_yun_left.clearCheck();
                    changeGroup = false;
                }

            }
            switch (checkedId) {
                case R.id.rb_option_turntable:
                    optionMode = 1;
                    bt_along.setText("顺");
                    bt_inverse.setText("逆");
                    break;
                case R.id.rb_option_bottom_arm:
                    optionMode = 2;
                    bt_along.setText("升");
                    bt_inverse.setText("降");
                    break;
                case R.id.rb_option_top_arm:
                    optionMode = 3;
                    bt_along.setText("升");
                    bt_inverse.setText("降");
                    break;
                case R.id.rb_option_platform:
                    optionMode = 4;
                    bt_along.setText("顺");
                    bt_inverse.setText("逆");
                    break;
                case R.id.rb_option_in_arm:
                    optionMode = 5;
                    bt_along.setText("伸");
                    bt_inverse.setText("缩 ");
                    break;
                case R.id.rb_option_leveling:
                    optionMode = 6;
                    bt_along.setText("升");
                    bt_inverse.setText("将");
                    break;
                case R.id.rb_option_machine_hopper:
                    optionMode = 7;
                    bt_along.setText("外");
                    bt_inverse.setText("内");
                    break;
                case R.id.rb_option_people_hopper:
                    optionMode = 8;
                    bt_along.setText("外");
                    bt_inverse.setText("内");
                    break;
                case R.id.rb_option_machine_hopper_updown:
                    optionMode = 9;
                    bt_along.setText("升");
                    bt_inverse.setText("降");
                    break;
                case R.id.rb_option_people_hopper_updown:
                    optionMode = 10;
                    bt_along.setText("升");
                    bt_inverse.setText("降");
                    break;
                case R.id.rb_option_yun_tab:
                    optionModeYt = 11;
                    bt_yun_along.setText("顺");
                    bt_yun_inverse.setText("逆");
                    break;
                case R.id.rb_option_yun_fy:
                    optionModeYt = 12;
                    bt_yun_along.setText("俯");
                    bt_yun_inverse.setText("仰");
                    break;
            }
        }

        //显示位置控制弹出框
        public void showDialog ( int mode, String along,final String alongHint,
        final String reverseStr, final String reverseStrHint){
            final CustomEditTextDialog customDialog = new CustomEditTextDialog(this);
            final RadioGroup rgSelect = (RadioGroup) customDialog.getRgSelect();
            final RadioButton rbOption1 = (RadioButton) customDialog.getOption1();
            final RadioButton rbOption2 = (RadioButton) customDialog.getOption2();
            final EditText reverseText = (EditText) customDialog.getReverseEditText();
            if (mode == 1) {
                rgSelect.setVisibility(View.VISIBLE);
            } else {
                rgSelect.setVisibility(View.GONE);
            }
            rbOption1.setText(along);
            rbOption2.setText(reverseStr);

            reverseText.setHint("取值范围:" + alongHint);
            final int[] type = new int[1];
            //todo 根据mode及填写的数据发送命令
            rgSelect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == rbOption1.getId()) {
                        type[0] = 0;
                        reverseText.setHint("取值范围:" + alongHint);
                    } else if (checkedId == rbOption2.getId()) {
                        type[0] = 1;
                        reverseText.setHint("取值范围:" + reverseStrHint);
                    }

                }
            });
            customDialog.setOnSureListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //判断输入值是否合法
                    if (TextUtils.isEmpty(reverseText.getText().toString())) {
                        Toast.makeText(MainActivity.this, "请输入合法的数值", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //增加非法输入判断
                    float str = (float) 0.0;
                    try {
                        str = Float.parseFloat(reverseText.getText().toString());
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "请输入合法的数值", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String[] alongArr = alongHint.split("~");
                    if (alongArr != null && alongArr.length == 2) {

                        if (str < Float.parseFloat(alongArr[0]) || str > Float.parseFloat(alongArr[1])) {
                            Toast.makeText(MainActivity.this, "请输入合法的数值", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    ///////////自动命令拼装 自动信号必须使能/////////
                    int speed = Float.valueOf(str).intValue();
                    int temp = (int) (str * 10);
                    //赋值给全局变量距离
                    locationValue = speed;
                    //自动----位置控制 首先将18b的位置控制设置为1
                    optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x00, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                    switch (optionMode) {
                        case 1:
                            if (type[0] == 0) {
                                //controlData = new byte[]{(byte) 0xfe, (byte) 0xfd,0x00,0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x01, 0x00, (byte) (speed * 10 / 256), (byte) (speed * 10 / 256), 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                                controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x01, 0x00, (byte) (speed * 10 / 256), (byte) ((speed * 10) % 256), 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            } else {
                                controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x02, 0x00, (byte) (speed * 10 / 256), (byte) ((speed * 10) % 256), 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            }
                            break;
                        case 2:
                            controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x03, 0x00, (byte) (temp / 256), (byte) (temp % 256), 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            break;
                        case 3:
                            if (speed < 0) {
                                controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x04, 0x00, (byte) (temp >> 8), (byte) temp, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            } else {
                                controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x04, 0x00, (byte) (temp / 256), (byte) (temp % 256), 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            }
                            break;
                        case 4:
                            if (speed < 0) {
                                controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x05, 0x00, (byte) (temp >> 8), (byte) temp, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            } else {
                                controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x05, 0x00, (byte) (temp / 256), (byte) (temp % 256), 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            }
                            break;
                        case 5:
                            if (speed < 0) {
                                controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x06, 0x00, (byte) (speed >> 8), (byte) speed, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            } else {
                                controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x06, 0x00, (byte) (speed / 256), (byte) (speed % 256), 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            }
                            break;
                        case 6:
                            if (speed < 0) {
                                controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x07, 0x00, (byte) (speed >> 8), (byte) speed, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            } else {
                                controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x07, 0x00, (byte) (speed / 256), (byte) (speed % 256), 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            }
                            break;
                        case 7:
                            if (speed < 0) {
                                controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x08, 0x00, (byte) (temp >> 8), (byte) temp, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            } else {
                                controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x08, 0x00, (byte) (temp / 256), (byte) (temp % 256), 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            }
                            break;
                        case 8:
                            if (speed < 0) {
                                controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x09, 0x00, (byte) (speed >> 8), (byte) speed, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            } else {
                                controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x09, 0x00, (byte) ((speed * 10) / 256), (byte) ((speed * 10) % 256), 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            }
                            break;
                        case 9:
                            if (speed < 0) {
                                controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x0a, 0x00, (byte) (speed >> 8), (byte) speed, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            } else {
                                controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x0a, 0x00, (byte) (speed / 256), (byte) (speed % 256), 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            }
                            break;
                        case 10:
                            if (speed < 0) {
                                controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x0b, 0x00, (byte) (speed >> 8), (byte) speed, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            } else {
                                controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x0b, 0x00, (byte) (speed / 256), (byte) (speed % 256), 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            }
                            break;
                    }
                    activityToTargetFlag = false;
                    customDialog.dismiss();
                }
            });
            customDialog.setOnCanlceListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    customDialog.dismiss();
                }
            });


            customDialog.show();
        }
        public void showDialogYt ( int mode, String along,final String alongHint,
        final String reverseStr, final String reverseStrHint){
            final CustomEditTextDialog customDialog = new CustomEditTextDialog(this);
            final RadioGroup rgSelect = (RadioGroup) customDialog.getRgSelect();
            final RadioButton rbOption1 = (RadioButton) customDialog.getOption1();
            final RadioButton rbOption2 = (RadioButton) customDialog.getOption2();
            final EditText reverseText = (EditText) customDialog.getReverseEditText();
            if (mode == 1) {
                rgSelect.setVisibility(View.VISIBLE);
            } else {
                rgSelect.setVisibility(View.GONE);
            }
            rbOption1.setText(along);
            rbOption2.setText(reverseStr);

            reverseText.setHint("取值范围:" + alongHint);
            final int[] typeYt = new int[1];
            //todo 根据mode及填写的数据发送命令
            rgSelect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == rbOption1.getId()) {
                        typeYt[0] = 0;
                        reverseText.setHint("取值范围:" + alongHint);
                    } else if (checkedId == rbOption2.getId()) {
                        typeYt[0] = 1;
                        reverseText.setHint("取值范围:" + reverseStrHint);
                    }

                }
            });
            customDialog.setOnSureListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    byte[] aaa = {(byte)0x8A, 0x07,0x01 , (byte) 0xFF,0x01,0x00,0x4B,0x32,0x00, (byte) 0x8C};
                    getXOR(aaa);
                    //判断输入值是否合法
                    if (TextUtils.isEmpty(reverseText.getText().toString())) {
                        Toast.makeText(MainActivity.this, "请输入合法的数值", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //增加非法输入判断
                    float str = (float) 0.0;
                    try {
                        str = Float.parseFloat(reverseText.getText().toString());
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "请输入合法的数值", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String[] alongArr = alongHint.split("~");
                    if (alongArr != null && alongArr.length == 2) {

                        if (str < Float.parseFloat(alongArr[0]) || str > Float.parseFloat(alongArr[1])) {
                            Toast.makeText(MainActivity.this, "请输入合法的数值", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    ///////////自动命令拼装 自动信号必须使能/////////
                    int speed =Float.valueOf(str).intValue() * 100;
                    Log.v("yt_vertical_circle_1",optionModeYt+"");

                   if(optionModeYt==12){
                       speed =(45-Float.valueOf(str).intValue()) * 100;
                    }
                    //赋值给全局变量距离
                    //自动----位置控制 首先将18b的位置控制设置为1
                    switch (optionModeYt) {
                        case 11:
                            optionDataYt = new byte[]{(byte) 0x8A, 0x07, 0x01, (byte) 0xFF, 0x01, 0x00, 0x4B, (byte) (speed / 256), (byte) (speed % 256), (byte)0x8c,  (byte)0x00, 0x00};
                            break;
                        case 12:
                            optionDataYt = new byte[]{(byte) 0x8A, 0x07, 0x01, (byte) 0xFF, 0x01, 0x00, 0x4D, (byte) (speed / 256), (byte) (speed % 256), (byte)0xb6, (byte)0x00, 0x00};
                            break;
                    }
                    Log.v("optionDataYt",speed+"");
                    optionDataYt[optionDataYt.length - 3] = getYtCheck(optionDataYt);//替换云台验证码数据
                    optionDataYt[optionDataYt.length - 2] = getXOR(optionDataYt);//替换与或验证码数据
                    optionDataYt[optionDataYt.length - 1] = 0x1f;//替换与或验证码数据
                    //8A 07 01 FF 01 00 4B 32 00 7E 75 1F
                    write(writeBluetoothGattCharacteristic, null,optionDataYt);
                    Log.v("optionDataYt",Arrays.toString(optionDataYt));
                    customDialog.dismiss();
                }
            });
            customDialog.setOnCanlceListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    customDialog.dismiss();
                }
            });
            customDialog.show();
        }
        //斗臂车发送指令
        void sendCmd ( byte speed){
            //手动命令  -- 比例控制
            //手动自动互斥命令 自动信号不使能
            optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            //手动速度命令
            switch (optionMode) {
                case 1:
                    if (isAlong) {
                        optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x01, 0x00, 0x50, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                    } else {
                        optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x02, 0x00, 0x50, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                    }
                    activityTarget1 = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x02, (byte) 0x8b, 0x7f, speed, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x00, 0x00, 0x00, 0x00};
                    break;
                case 2:
                    if (isAlong) {
                        optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x04, 0x00, 0x50, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                    } else {
                        optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x08, 0x00, 0x50, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                    }
                    activityTarget1 = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x02, (byte) 0x8b, speed, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x00, 0x00, 0x00, 0x00};
                    break;
                case 3:
                    if (isAlong) {
                        optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x10, 0x00, 0x50, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                    } else {
                        optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x20, 0x00, 0x50, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                    }
                    activityTarget1 = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x02, (byte) 0x8b, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, speed, 0x7f, 0x00, 0x00, 0x00, 0x00};
                    break;
                case 4:
                    if (isAlong) {
                        optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x40, 0x00, 0x50, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                    } else {
                        optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, (byte) 0x80, 0x00, 0x50, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                    }
                    activityTarget1 = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x02, (byte) 0x8b, 0x7f, 0x7f, 0x7f, speed, 0x7f, 0x7f, 0x7f, 0x7f, 0x00, 0x00, 0x00, 0x00};
                    break;
                case 5:
                    if (isAlong) {
                        optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x01, 0x50, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                    } else {
                        optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x02, 0x50, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                    }
                    activityTarget1 = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x02, (byte) 0x8b, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, speed, 0x00, 0x00, 0x00, 0x00};
                    break;
                case 6:

                    if (isAlong) {
                        optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x04, 0x50, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                    } else {
                        optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x08, 0x50, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                    }
                    activityTarget1 = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x02, (byte) 0x8b, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, speed, 0x7f, 0x7f, 0x00, 0x00, 0x00, 0x00};
                    break;
                case 7:
                    if (isAlong) {
                        optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x10, 0x50, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                    } else {
                        optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x20, 0x50, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                    }
                    activityTarget1 = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x02, (byte) 0x8b, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, speed, 0x7f, 0x7f, 0x00, 0x00, 0x00, 0x00};
                    break;
                case 8:
                    if (isAlong) {
                        optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, (byte) 0x80, 0x50, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                    } else {
                        optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x40, 0x50, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                    }
                    activityTarget2 = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x03, (byte) 0x8b, 0x7f, speed, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x00, 0x00, 0x00, 0x00};
                    break;
                case 9:
                    if (isAlong) {
                        optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x00, 0x51, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                    } else {
                        optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x00, 0x52, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                    }
                    activityTarget1 = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x02, (byte) 0x8b, 0x7f, 0x7f, speed, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x00, 0x00, 0x00, 0x00};
                    break;
                case 10:
                    if (isAlong) {
                        optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x00, 0x54, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                    } else {
                        optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x00, 0x58, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                    }
                    activityTarget2 = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x03, (byte) 0x8b, speed, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x00, 0x00, 0x00, 0x00};
                    break;
            }
        }
        //云台发送指令格式
        void sendCmdYt ( byte speed, byte speedF){
            //手动速度命令

            if (touchFlagYt) {
                switch (optionModeYt) {
                    case 11:
                        if (isYtAlong) {
                            optionDataYt = new byte[]{(byte) 0x8A, 0x07, 0x01, (byte) 0xFF, 0x01, 0x00, 0x02, speed, 0x00, 0x67, 0x00, 0x00};
                        } else {
                            optionDataYt = new byte[]{(byte) 0x8A, 0x07, 0x01, (byte) 0xFF, 0x01, 0x00, 0x04, speed, 0x00, 0x69, 0x00, 0x00};
                        }
                        optionDataYt[optionDataYt.length - 3] = getYtCheck(optionDataYt);//替换云台验证码数据
                        optionDataYt[optionDataYt.length - 2] = getXOR(optionDataYt);//替换与或验证码数据
                        optionDataYt[optionDataYt.length - 1] = 0x1f;//替换与或验证码数据
                        break;
                    case 12:
                        if (isYtAlong) {
                            optionDataYt = new byte[]{(byte) 0x8A, 0x07, 0x01, (byte) 0xFF, 0x01, 0x00, 0x10, 0x00, speedF, 0x2f, 0x00, 0x00};
                        } else {
                            optionDataYt = new byte[]{(byte) 0x8A, 0x07, 0x01, (byte) 0xFF, 0x01, 0x00, 0x08, 0x00, speedF, 0x27, 0x00, 0x00};
                        }
                        optionDataYt[optionDataYt.length - 3] = getYtCheck(optionDataYt);//替换云台验证码数据
                        optionDataYt[optionDataYt.length - 2] = getXOR(optionDataYt);//替换与或验证码数据
                        optionDataYt[optionDataYt.length - 1] = 0x1f;//替换与或验证码数据
                        break;
                }
            } else {
                //松开按钮发送停止指令:
                optionDataYt = new byte[]{(byte) 0x8A, 0x07, 0x01, (byte) 0xFF, 0x01, 0x00, 0x00, 0x00, 0x00, 0x01, 0x73, 0x1F};
            }
            write(writeBluetoothGattCharacteristic, null,optionDataYt);
            Log.v("optionDataYt",Arrays.toString(optionDataYt));
        }
        void initAndShowDialog () {
            String alongStr = "", alongStrHint = "", reverseStr = "", reverseStrHint = "";
            switch (optionMode) {
                case 1:
                    alongStr = "转台顺";
                    alongStrHint = "0~360";
                    reverseStr = "转台逆";
                    reverseStrHint = "0~360";
                    break;
                case 2:
                    alongStr = "下臂升降";
                    alongStrHint = "0~91.8";
                    break;
                case 3:
                    alongStr = "上臂升降";
                    alongStrHint = "-24.5~85";
                    break;
                case 4:
                    alongStr = "平台顺逆";
                    alongStrHint = "-93.2~92.9";

                    break;
                case 5:
                    alongStr = "内臂伸缩";
                    alongStrHint = "0~3590";

                    break;
                case 6:
                    alongStr = "调平升降";
                    alongStrHint = "0~360";

                    break;
                case 7:
                    alongStr = "机器斗旋转";
                    alongStrHint = "0~92.5";
                    break;
                case 8:
                    alongStr = "人斗外旋转";
                    alongStrHint = "0~92.5";
                    break;
                case 9:
                    alongStr = "机器斗升降";
                    alongStrHint = "0~500";

                    break;
                case 10:
                    alongStr = "人斗升降";
                    alongStrHint = "0~500";

                    break;
            }
            showDialog(optionMode, alongStr, alongStrHint, reverseStr, reverseStrHint);
        }
        void initAndShowDialogYt () {
            String alongStr = "", alongStrHint = "", reverseStr = "", reverseStrHint = "";
            switch (optionModeYt) {
                case 11:
                    alongStr = "云台顺";
                    alongStrHint = "0~360";
                    reverseStr = "云台逆";
                    reverseStrHint = "0~360";
                    break;
                case 12:
                    alongStr = "云台俯";
                    alongStrHint = "0~90";
                    reverseStr = "云台俯";
                    reverseStrHint = "0~90";
                    break;
            }
            showDialogYt(optionModeYt, alongStr, alongStrHint, reverseStr, reverseStrHint);
        }
        /////////////////////////////TCP////////////////////////////////////////////////
        /**
         * 发送数据
         */
        Handler sendDataHandler = new Handler();
        Handler sendDataHandlerBt = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                //所有相关寄存器不停发数据轮询
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (isConnected) {
                            optionData[optionData.length - 1] = getXOR(optionData);
                            send(optionData);
                            activityTarget1[activityTarget1.length - 1] = getXOR(activityTarget1);
                            send(activityTarget1);
                            activityTarget2[activityTarget2.length - 1] = getXOR(activityTarget2);
                            send(activityTarget2);
                            controlData[controlData.length - 1] = getXOR(controlData);
                            send(controlData);
                        }
                    }
                }).start();
                //顺逆按钮是否按下的标志
                if (!touchFlag) {
                    //数据发送完成后将所有寄存器数据设置为初始状态
                    if (downCarFlag) {
                        //点击下车时候提前发送下车命令
                        optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x00, 0x40, 0x00, 0x11, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                        if (autoIn) {
                            //当点击自动收回指令，发送自动收回指令
                            optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x00, 0x40, 0x00, 0x15, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                        }
                        if (autoOut) {
                            //当点击自动伸出指令，发送自动伸出指令
                            optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x00, 0x40, 0x00, 0x13, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                        }
                        if (stopFlag) {
                            optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x00, 0x00, 0x0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                        }
                    } else {
                        //增加判断如果点击急停，所有指令斗清零
                        if (stopFlag) {
                            optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x00, 0x00, 0x0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                        } else {
                            if (isSpeed) {
                                optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x00, 0x50, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            } else {
                                optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x00, 0x60, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            }
                        }
                    }
                    activityTarget1 = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x02, (byte) 0x8b, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x00, 0x00, 0x00, 0x00};
                    activityTarget2 = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x03, (byte) 0x8b, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x00, 0x00, 0x00, 0x00};
                } else {
                    if (stopFlag) {
                        optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                        controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x00, 0x00, 0x0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                    } else {
                        if (!isSpeed) {
                            optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x00, 0x60, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            //controlData = new byte[]{(byte) 0xfe, (byte) 0xfd,0x00,0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x00, 0x00, 0x0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                        }
                    }

                    //顺逆按钮按下时动作使能信号清0 其他状态置1
//               optionData = new byte[]{(byte) 0xfe, (byte) 0xfd,0x00,0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                    //activityTarget2 = new byte[]{(byte) 0xfe, (byte) 0xfd,0x00,0x08, 0x00, 0x00, 0x03, (byte) 0x8b, 0x00, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x00, 0x00, 0x00, 0x00};
                }
                sendDataHandler.postDelayed(this, 30);
            }
        };
        //蓝牙轮询查询角度（垂直/水平等信息）
        Runnable runnableBT = new Runnable() {
        @Override
        public void run() {
            //所有相关寄存器不停发数据轮询
            if(!touchFlagYt){
                try {
                    write(writeBluetoothGattCharacteristic, null,optionDataYtSelectV);
                    Thread.sleep(100);
                    write(writeBluetoothGattCharacteristic, null,optionDataYtSelectH);
                    Log.v("bTConnStatus-horizon",Arrays.toString(optionDataYtSelectH));
                    Log.v("bTConnStatus-vertical",Arrays.toString(optionDataYtSelectV));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            sendDataHandlerBt.postDelayed(this, 500);

        }
    };
        /**
         * 接收数据
         */
        private Handler handler = new Handler() {
            @SuppressLint("HandlerLeak")
            @Override
            /* 当有消息发送出来的时候就执行Handler的这个方法 */
            public void handleMessage(Message msg) {
                super.handleMessage(msg);


            }
        };


        /* 连接按钮处理函数：建立Socket连接 */
        @SuppressLint("HandlerLeak")
        public void connect () {
            if (false == isConnected) {
                new Thread() {
                    public void run() {
                        try {
                            /* 建立socket */
                            socket = new Socket(ipAddress, port);
                            /* 输出流 */
                            writer = socket.getOutputStream();
                            /* 输入流 */
                            reader = socket.getInputStream();
                            /* 调试输出 */
                            Log.i(TAG, "输入输出流获取成功");
                            Log.i(TAG, "检测数据");
                            /* 读数据并更新UI */
                            byte[] buf = new byte[100];
                            int i;
                            while ((i = reader.read(buf)) != -1) {
                                line = new String(buf, 0, i);

                                parse(buf);
//                            Log.i(TAG, "send to handler");
                            }
                        } catch (UnknownHostException e) {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "无法建立连接", Toast.LENGTH_SHORT).show();
                                }
                            });
                            Log.i(TAG, "无法建立连接");
                            e.printStackTrace();
                            isConnected = false;
                        } catch (IOException e) {
                            e.printStackTrace();
                            isConnected = false;
                        }
                    }
                }.start();
                isConnected = true;
                /* 更新UI */
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                    }
                });
                Log.i(TAG, "连接成功");
            } else {
                isConnected = false;
                /* 关闭socket */
                onDestroy();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "连接已断开", Toast.LENGTH_SHORT).show();
                    }
                });
                Log.i(TAG, "连接已断开");
            }
        }

        /* 发送按钮处理函数：向输出流写数据 */
        public void send ( byte[] data){
            if (writer == null) {
                return;
            }
            try {
                /* 向输出流写数据 */
                writer.write(data);
                writer.flush();
                //Log.i(TAG, "发送数据");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onDestroy () {
            sendDataHandler.removeCallbacks(runnable);
            try {
                /* 关闭socket */
                if (null != socket) {
                    socket.shutdownInput();
                    socket.shutdownOutput();
                    socket.getInputStream().close();
                    socket.getOutputStream().close();
                    socket.close();
                }
            } catch (IOException e) {
                Log.d(TAG, e.getMessage());
            }

            if (bleDevice != null) {
                if (bleDevice.isConnecting()) {
                    ble.cancelConnecting(bleDevice);
                } else if (bleDevice.isConnected()) {
                    ble.disconnect(bleDevice);
                }
            }
            ble.cancelCallback(connectCallback);
            super.onDestroy();
        }

        //接受数据的处理
        public void parse ( final byte[] baseData){

            for (int i = 0; i < baseData.length - 13; i++) {
                if (baseData[i] == 0x08 && baseData[i + 1] == 0x00 && baseData[i + 2] == 0x00) {
                    if (baseData.length - i > 13) {//说明数据完整
                        final int m = i;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                refreshView(Arrays.copyOfRange(baseData, m, m + 13));
                            }
                        });

                        i += 12;
                    } else {
                        Log.e(TAG, "parse: 已没有完整的数据");
                        break;//如果长度不足 就等着
                    }
                }
            }
        }
        //解析云台数据
        public void parseYt ( final byte[] baseData){
            Log.v("解析云台数据",Arrays.toString(baseData));
            Log.v("解析云台数据9",Arrays.toString(baseData));
            if(baseData.length==20||baseData.length==11||baseData.length==12){
                if(baseData[0]==(byte) 0x8a){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.v("解析云台数据--",Arrays.toString(baseData));
                            refreshViewYt(baseData);
                        }
                    });
                }
            }
        }
        //回显都臂车返回的数据到pad
        void refreshView ( byte[] baseData){
            if (baseData.length == 13) {
                if (baseData[3] == 0x01) {

                    if (baseData[4] == (byte) 0xa0) {
                        Log.v("1a0数据解析", Arrays.toString(baseData));
                        if (baseData[9] == 0x03) {
                            Log.v("机器斗升降到位", activityToTargetFlag + "");
                            activityToTargetFlag = false;
                            controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x00, 0x00, 0x0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x00, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                        }
                        if (baseData[9] == 0x0c) {
                            Log.v("人斗升降状态反馈", activityToTargetFlag + "");
                            activityToTargetFlag = false;
                            controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x00, 0x00, 0x0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x00, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                        }

                        if (baseData[8] == 0x03) {
                            Log.v("平台到位", activityToTargetFlag + "");
                            activityToTargetFlag = false;
                            controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x00, 0x00, 0x0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x00, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                        }
                        if (baseData[8] == 0x0c) {
                            Log.v("上臂伸缩状态到位", activityToTargetFlag + "");
                            activityToTargetFlag = false;
                            controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x00, 0x00, 0x0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x00, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                        }
                        if (baseData[8] == 0x30) {
                            Log.v("机器斗顺逆状态到位", activityToTargetFlag + "");
                            activityToTargetFlag = false;
                            controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x00, 0x00, 0x0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x00, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                        }
                        if (baseData[8] == 0xc0) {
                            Log.v("人斗顺逆状态到位", activityToTargetFlag + "");
                            activityToTargetFlag = false;
                            controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x00, 0x00, 0x0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x00, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                        }
                        if (baseData[7] == 0x03) {
                            Log.v("转台顺状态到位", activityToTargetFlag + "");
                            activityToTargetFlag = false;
                            controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x00, 0x00, 0x0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x00, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                        }
                        if (baseData[7] == 0x0c) {
                            Log.v("转台逆状态反馈到位", activityToTargetFlag + "");
                            activityToTargetFlag = false;
                            controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x00, 0x00, 0x0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x00, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                        }
                        if (baseData[7] == 0x30) {
                            Log.v("下臂升降状态反馈", activityToTargetFlag + "");
                            activityToTargetFlag = false;
                            controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x00, 0x00, 0x0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x00, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                        }
                        if (baseData[7] == 0xc0) {
                            Log.v("上臂升降状态反馈", activityToTargetFlag + "");
                            activityToTargetFlag = false;
                            controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x00, 0x00, 0x0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                            optionData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x01, (byte) 0x8b, 0x00, 0x00, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                        }
                        //处理支腿到位状态
                        if (baseData[7] == (byte) 0xff) {
                            iv_right_front_h.setImageResource(R.mipmap.icon_car_leg);
                            Glide.with(MainActivity.this).load(R.mipmap.icon_car_leg).into(iv_car_leg);
                        }
                        if ((baseData[8] & 0x0f) == 0x0f) {
                            Glide.with(MainActivity.this).load(R.mipmap.icon_car_leg).into(iv_car_leg);
                        }

                        if ((baseData[7] & 0x01) == 0x01) {
                            Glide.with(MainActivity.this).load(R.mipmap.icon_x_left_arrow_green).into(iv_left_front_h);
                        } else {
                            Glide.with(MainActivity.this).load(R.mipmap.icon_x_left_arrow_gray).into(iv_left_front_h);
                        }
                        if ((baseData[7] & 0x02) == 0x02) {
                            iv_right_front_h.setImageResource(R.mipmap.icon_x_right_arrow_green);
                        } else {
                            iv_right_front_h.setImageResource(R.mipmap.icon_x_right_arrow_gray);
                        }
                        if ((baseData[7] & 0x04) == 0x04) {
                            iv_left_behind_h.setImageResource(R.mipmap.icon_x_left_arrow_green);
                        } else {
                            iv_left_behind_h.setImageResource(R.mipmap.icon_x_left_arrow_gray);
                        }
                        if ((baseData[7] & 0x08) == 0x08) {
                            iv_right_behind_h.setImageResource(R.mipmap.icon_x_right_arrow_green);
                        } else {
                            iv_right_behind_h.setImageResource(R.mipmap.icon_x_right_arrow_gray);
                        }
                        if ((baseData[7] & 0x10) == 0x10) {
                            iv_left_front_v.setImageResource(R.mipmap.icon_y_arrow_out_in_place);
                        } else {
                            iv_left_front_v.setImageResource(R.mipmap.icon_y_arrow_not_in_place);
                        }
                        if ((baseData[7] & 0x20) == 0x20) {
                            iv_right_front_v.setImageResource(R.mipmap.icon_y_arrow_out_in_place);
                        } else {
                            iv_left_front_v.setImageResource(R.mipmap.icon_y_arrow_not_in_place);
                        }
                        if ((baseData[7] & 0x40) == 0x40) {
                            iv_left_behind_v.setImageResource(R.mipmap.icon_y_arrow_out_in_place);
                        } else {
                            iv_left_front_v.setImageResource(R.mipmap.icon_y_arrow_not_in_place);
                        }
                        if ((baseData[7] & 0x80) == 0x80) {
                            iv_right_behind_v.setImageResource(R.mipmap.icon_y_arrow_out_in_place);
                        } else {
                            iv_left_front_v.setImageResource(R.mipmap.icon_y_arrow_not_in_place);
                        }
                        if ((baseData[8] & 0x01) == 0x01) {
                            iv_left_front_v.setImageResource(R.mipmap.icon_y_arrow_in_in_place);
                            iv_left_front_h.setImageResource(R.mipmap.icon_x_right_arrow_green);
                        } else {
                            iv_left_front_v.setImageResource(R.mipmap.icon_y_arrow_not_in_place);
                            iv_left_front_h.setImageResource(R.mipmap.icon_x_right_arrow_gray);
                        }
                        if ((baseData[8] & 0x02) == 0x02) {
                            iv_right_front_v.setImageResource(R.mipmap.icon_y_arrow_in_in_place);
                            iv_right_front_h.setImageResource(R.mipmap.icon_x_left_arrow_green);
                        } else {
                            iv_right_front_v.setImageResource(R.mipmap.icon_y_arrow_not_in_place);
                            iv_right_front_h.setImageResource(R.mipmap.icon_x_left_arrow_gray);
                        }
                        if ((baseData[8] & 0x04) == 0x04) {
                            iv_left_behind_v.setImageResource(R.mipmap.icon_y_arrow_in_in_place);
                            iv_left_behind_h.setImageResource(R.mipmap.icon_x_right_arrow_green);
                        } else {
                            iv_left_behind_v.setImageResource(R.mipmap.icon_y_arrow_not_in_place);
                            iv_left_behind_h.setImageResource(R.mipmap.icon_x_right_arrow_gray);
                        }
                        if ((baseData[8] & 0x08) == 0x08) {
                            iv_right_behind_v.setImageResource(R.mipmap.icon_y_arrow_in_in_place);
                            iv_right_behind_h.setImageResource(R.mipmap.icon_x_left_arrow_green);
                        } else {
                            iv_right_behind_v.setImageResource(R.mipmap.icon_y_arrow_not_in_place);
                            iv_right_behind_h.setImageResource(R.mipmap.icon_x_left_arrow_gray);
                        }

                    } else if (baseData[4] == (byte) 0xa1) {

                    } else if (baseData[4] == (byte) 0xa2) {

                        tv_motor_speed.setText((short) (((baseData[10] & 0x00FF) << 8) | (0x00FF & baseData[9])) + "rmp");
                        tv_run_time.setText((short) (((baseData[12] & 0x00FF) << 8) | (0x00FF & baseData[11])) + "h");
                    }
                } else if (baseData[3] == 0x02) {
                    if (baseData[4] == (byte) 0xa0) {
                        //增加判断转台到达目标值对指令进行清0
                        short temp;
                        switch (optionMode) {
                            case 1:
                                temp = (short) (((baseData[6] & 0x00FF) << 8) | (0x00FF & baseData[5]));
                                Log.v("2A0转台temp", "****" + temp);
                                if (temp < (locationValue * 10 + 5)
                                        && temp > (locationValue * 10 - 5)) {
                                    Log.v("2A0转台", "999999****" + locationValue);
                                    controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x00, 0x00, 0x0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};


                                }
                            case 2:
                                temp = (short) (((baseData[8] & 0x00FF) << 8) | (0x00FF & baseData[7]));
                                Log.v("2A0下臂temp", "****" + temp);
                                if (temp < (locationValue * 10 + 5)
                                        && temp > (locationValue * 10 - 5)) {
                                    Log.v("2A0下臂", "999999****" + locationValue);
                                    controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x00, 0x00, 0x0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                                }
                            case 3:
                                temp = (short) (((baseData[10] & 0x00FF) << 8) | (0x00FF & baseData[9]));
                                Log.v("2A0上臂temp", "****" + temp);
                                if (temp < (locationValue * 10 + 5)
                                        && temp > (locationValue * 10 - 5)) {
                                    Log.v("2A0上臂", "999999****" + locationValue);
                                    controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x00, 0x00, 0x0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                                }
                            case 5:
                                temp = (short) (((baseData[12] & 0x00FF) << 8) | (0x00FF & baseData[11]));
                                if (temp < (locationValue + 5)
                                        && temp > (locationValue - 5)) {
                                    controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x00, 0x00, 0x0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                                    Log.v("清除2A0伸缩臂", "999999****" + locationValue);
                                }
                        }
                        tv_turntable_angle.setText((float) ((short) (((baseData[6] & 0x00FF) << 8) | (0x00FF & baseData[5])) / 10.0) + "°");
                        tv_turntable_angle1.setText((float) ((short) (((baseData[6] & 0x00FF) << 8) | (0x00FF & baseData[5])) / 10.0) + "°");
                        tv_down_arm_angle.setText((float) ((short) (((baseData[8] & 0x00FF) << 8) | (0x00FF & baseData[7])) / 10.0) + "°");
                        tv_up_arm_angle.setText((float) ((short) (((baseData[10] & 0x00FF) << 8) | (0x00FF & baseData[9])) / 10.0) + "°");
                        tv_telescopic_boom_length.setText((short) (((baseData[12] & 0x00FF) << 8) | (0x00FF & baseData[11])) + "mm");


//                    tv_bottom_voltage.setText(baseData[9] + "V");
//                    tv_platform_voltage.setText(baseData[10] + "V");
//                    tv_long_angle_voltage.setText(baseData[11] + "V");
                    } else if (baseData[4] == (byte) 0xa1) {
                        //故障

                        date = new Date(System.currentTimeMillis());
                        if (baseData[5] % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("下车急停报警");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[5] >> 1 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("遥控器急停报警");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[5] >> 2 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("伸缩开关不在初始位报警");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[5] >> 3 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("油门加速开关不在初始位报警");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);

                        }
                        if (baseData[5] >> 4 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("启动熄火开关不在初始位报警");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[5] >> 5 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("臂架未到位，选择支腿操作报警");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[5] >> 6 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("遥控器安全信号报警");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[5] >> 7 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("上装阀短路报警");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        ////byte[6]
                        if (baseData[6] % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("支腿阀短路报警");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[6] >> 1 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("伸出阀短路报警");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[6] >> 2 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("缩回阀短路报警");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[6] >> 3 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("左前水平阀短路报警");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);

                        }
                        if (baseData[6] >> 4 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("左前垂直阀短路报警");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[6] >> 5 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("右前水平阀短路报警");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[6] >> 6 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("右前垂直阀短路报警");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[6] >> 7 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("左后水平阀短路报警");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        /////byte[7]/////////////
                        if (baseData[7] % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("左后垂直阀短路报警");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[7] >> 1 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("右后水平阀短路报警");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[7] >> 2 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("右后垂直阀短路报警");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[7] >> 3 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("应急泵短路报警");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);

                        }
                        if (baseData[7] >> 4 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("蜂鸣器短路报警");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[7] >> 5 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("下车开关信号短路报警");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[7] >> 6 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("压力传感器超量程报警");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[7] >> 7 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("底盘电池低电报警");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        /////byte[8]/////////////
                        if (baseData[8] % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("长角传感器电池低电报警");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[8] >> 1 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("平台电池低电报警");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[8] >> 2 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("Wifi通讯故障报警");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[8] >> 3 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("机器人通讯故障报警");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);

                        }
                        if (baseData[8] >> 4 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("支腿未到位选择上车");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[8] >> 5 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("调平超时报警");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[8] >> 6 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("系统憋压");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[8] >> 7 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("虚腿报警");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        /////byte[9]/////////////
                        if (baseData[9] % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("支腿遥控器总线故障");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[9] >> 1 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("水平仪总线故障");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[9] >> 2 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("上臂长角总线故障");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[9] >> 3 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("转台遥控器总线故障");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);

                        }
                        if (baseData[9] >> 4 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("转台编码器总线故障");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[9] >> 5 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("下臂倾角总线故障");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[9] >> 6 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("平台编码器总线故障");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[9] >> 7 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("调平水平仪总线故障");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        /////byte[10]/////////////
                        if (baseData[10] % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("人斗旋转检测总线故障");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[10] >> 1 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("人斗升降检测总线故障");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[10] >> 2 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("机器斗选转总线故障");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[10] >> 3 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("机器斗升降总线故障");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);

                        }
                        if (baseData[10] >> 4 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("转台控制器总线故障");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[10] >> 5 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("平台控制器总线故障");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[10] >> 6 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("底盘通讯故障");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                        if (baseData[10] >> 7 % 2 == 1) {
                            FaultBean bean = new FaultBean();
                            bean.setFaultContent("长角控制器总线故障");
                            bean.setCreateTime(simpleDateFormat.format(date));
                            DbController.getInstance(MainActivity.this).insert(bean);
                        }
                    } else if (baseData[4] == (byte) 0xa2) {
                        tv_hopper_x_angle.setText("X:" + (float) ((short) (((baseData[11] & 0x00FF) << 8) | (0x00FF & baseData[10])) / 10.0) + "°");

                        //到位标志
                        if (baseData[7] != 0x00 || baseData[8] != 0x00 || baseData[9] != 0x00) {
                            activityToTargetFlag = true;
                        }

                    }

                } else if (baseData[3] == 0x03) {

                    if (baseData[4] == (byte) 0xa0) {
                        tv_hopper_y_angle.setText("Y:" + (float) ((short) (((baseData[6] & 0x00FF) << 8) | (0x00FF & baseData[5])) / 10.0) + "°");

                        tv_platform_angle.setText((float) ((short) (((baseData[8] & 0x00FF) << 8) | (0x00FF & baseData[7])) / 10.0) + "°");

                        tv_people_hopper_angle.setText((float) ((short) (((baseData[10] & 0x00FF) << 8) | (0x00FF & baseData[9])) / 10.0) + "°");

                        tv_people_hopper_height.setText((short) (((baseData[12] & 0x00FF) << 8) | (0x00FF & baseData[11])) + "mm");
                        if (locationValue != 0) {
                            short tmp;
                            switch (optionMode) {
                                case 4:
                                    tmp = (short) (((baseData[8] & 0x00FF) << 8) | (0x00FF & baseData[7]));
                                    if (tmp < (locationValue * 10 + 5)
                                            && tmp > (locationValue * 10 - 5)) {
                                        Log.v("3A0平台", "999999****" + locationValue);
                                        controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x00, 0x00, 0x0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                                    }
                                case 8:
                                    tmp = (short) (((baseData[10] & 0x00FF) << 8) | (0x00FF & baseData[9]));
                                    Log.v("3A0人斗旋转tmp", "****" + tmp);
                                    if (Math.abs(tmp) <= (locationValue * 10 + 5)
                                            && Math.abs(tmp) > (locationValue * 10 - 5)) {
                                        Log.v("3A0人斗旋转", "999999****" + locationValue);
                                        controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x00, 0x00, 0x0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                                    }
                                case 10:
                                    tmp = (short) ((baseData[12] << 8) | baseData[11]);
                                    Log.v("3A0人斗升降", "****" + locationValue);
                                    Log.v("3A0人斗升降tmp", "****" + Math.abs(tmp));
                                    if (Math.abs(tmp) <= (locationValue + 5)
                                            && tmp > (locationValue - 5)) {
                                        Log.v("3A0人斗升降", "999999****" + locationValue);
                                        controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x00, 0x00, 0x0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                                    }
                            }
                        }
                    } else if (baseData[4] == (byte) 0xa1) {
                    }
                } else if (baseData[3] == 0x04) {
                    if (baseData[4] == (byte) 0xa0) {
                        tv_machine_hopper_angle.setText((float) ((short) (((baseData[6] & 0x00FF) << 8) | (0x00FF & baseData[5])) / 10.0) + "°");
                        tv_machine_hopper_height.setText((short) (((baseData[8] & 0x00FF) << 8) | (0x00FF & baseData[7])) + "mm");
                        tv_down_x.setText("X:" + (float) (baseData[10] / 10.0));
                        tv_down_y.setText("Y:" + (float) (baseData[9] / 10.0));
                        short temp;
                        switch (optionMode) {
                            case 7:
                                temp = (short) (((baseData[6] & 0x00FF) << 8) | (0x00FF & baseData[5]));
                                Log.v("4A0机器斗旋转temp", "****" + temp);
                                if (temp < (locationValue * 10 + 5)
                                        && temp > (locationValue * 10 - 5)) {
                                    Log.v("4A0机器斗旋转", "999999****" + locationValue);
                                    controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x00, 0x00, 0x0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

                                }
                            case 9:
                                temp = (short) ((baseData[8] << 8) | baseData[7]);
                                Log.v("4A0机器斗升降temp", "****" + Math.abs(temp));
                                if (Math.abs(temp) <= (locationValue + 5)
                                        && Math.abs(temp) > (locationValue - 5)) {
                                    Log.v("4A0机器斗升降", "999999****" + locationValue);
                                    controlData = new byte[]{(byte) 0xfe, (byte) 0xfd, 0x00, 0x08, 0x00, 0x00, 0x04, (byte) 0x8b, 0x00, 0x00, 0x0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                                }
                        }
                    } else if (baseData[4] == (byte) 0xa1) {
                    }
                }
            }
        }
        //接受云台反馈数据，回显pad上面
        void refreshViewYt ( byte[] baseData){
            if(baseData.length>8){
                Log.v("全部数据", Arrays.toString(baseData));
                if (baseData[1] == 0x10) {
                    Log.v("获取激光测距仪数据", Arrays.toString(baseData));
                    //1号激光测距仪 伸缩臂
                    LHI= (((baseData[3] & 0xFF)<< 24) |
                            ((baseData[4] & 0xFF)<< 16) |((baseData[5]& 0xFF)<< 8)|( 0xFF & baseData[6]) )/10000.0f;
                    Log.v("获取激光测距仪数据LHI", LHI+"");
                    //2号激光测距仪 上臂角度
                    LDG=(((baseData[7] & 0xFF) << 24) |
                            ((baseData[8]& 0xFF)<< 16) |((baseData[9]& 0xFF) << 8)| ( 0xFF &baseData[10] ))/10000f;
                    Log.v("获取激光测距仪数据LDG",LDG+"");
                    //3号激光测距仪 下臂角度
                    LQC=(((baseData[11] & 0xFF) << 24) |
                            ((baseData[12]& 0xFF)<< 16) |((baseData[13]& 0xFF)<< 8)|( 0xFF &baseData[14]) )/10000f;
                    Log.v("获取激光测距仪数据LQC", LQC+"");

                    //4号激光测距 云台激光测距的距离
                    LO1I= (((baseData[15] & 0xFF)<< 24) |
                            ((baseData[16] & 0xFF)<< 16) |((baseData[17]& 0xFF)<< 8)|( 0xFF & baseData[18]) )/10000.0f;
                    yt_laser_distance.setText(LO1I+"m");

                }
                if (baseData[1] == 0x06) {
                    tLyData = (float) ((short) (((baseData[3] & 0xFF) << 8) |(0xFF & baseData[4]) )/10);
                    r_n=tLyData;
                    sj_yt_r.setText(tLyData+"°");
                    yt_count_R.setText(tLyData+"°");
                    Log.v("接受陀螺仪的数据",tLyData+"");
                }
                if (baseData[1] == 0x07&&baseData.length==12) {
                    Log.v("获取云台角度数据", Arrays.toString(baseData));
                    if (baseData[6] == 0x59) {
                        //水平旋转角度
                        ytSpAngle= (((baseData[7]  & 0xFF)<<8)|(0xFF & baseData[4])) / 100.0f;
                        yt_horizontal_circle.setText(ytSpAngle + "°");
                    }
                    if (baseData[6] == 0x5b) {
                        //垂直旋转角度
                        ytFyAngle=45-(float) ((short) (((baseData[7] & 0xFF) << 8) | (0xFF & baseData[8])) / 100.0);
                        ytFyAngle=(Math.round(ytFyAngle*1000)/1000);
                        yt_vertical_circle.setText(ytFyAngle + "°");
                    }
                }
                if(isCount){
                    float temp_LQC=LQC-LQC_init+0.74f;
                    float temp_LDG=LDG-LDG_init+0.90f;
                    float temp_LHI=LHI-LHI_init+0.35f;
                    float temp_tlyData=tLyData-tLyData_init;
                    try {
                        pyo1=pyo.callAttr ("jisuan",ConfigPara.J1, ConfigPara.LPQ,
                                ConfigPara.LPC,temp_LQC,ConfigPara.LB1B,ConfigPara.LAB,ConfigPara.LPD,
                                ConfigPara.LAP,temp_LDG,ConfigPara.LD1D,ConfigPara.LG1G,ConfigPara.LEG,ConfigPara.LED1,
                                temp_LHI,ConfigPara.LGH,temp_tlyData,ConfigPara.m,ConfigPara.n,ConfigPara.s);

                        json = new JSONObject(pyo1.toString ());
                        x = json.getDouble("x");
                        y =json.getDouble("y");
                        z =json.getDouble("z");
                        Log.v("sj_yt_x",String.format("%.3f", x)+"m");
                        Log.v("sj_yt_y",String.format("%.3f", y)+"m");
                        Log.v("sj_yt_z",String.format("%.3f", z)+"m");
                        sj_yt_x.setText( String.format("%.3f", x)+"m");
                        sj_yt_y.setText( String.format("%.3f", y)+"m");
                        sj_yt_z.setText( String.format("%.3f", z)+"");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    dateValueList.clear();//先清空
                    //x_n=1.0;y_n=1.0;z_n=1.0;r_n=1.0;
                    //Toast.makeText (getApplicationContext (),pyo1.toString (),Toast.LENGTH_LONG).show ();
                    dateValueList.add(new VtDateValueBean(((float) (x/x_n))*100, "1"));
                    dateValueList.add(new VtDateValueBean(((float) (y/y_n))*100, "2"));
                    dateValueList.add(new VtDateValueBean(((float) (z/z_n))*100, "3"));
                    dateValueList.add(new VtDateValueBean(((float) (r/r_n))*100, "4"));
                    barChart.clear();
                    utilBar.showBarChart(dateValueList, "进度条(%)", getResources().getColor(R.color.ios_btntext_blue), barChart);
                }
            }

        }

//////////////////////////////////工具////////////////////
        public byte getXOR ( byte[] datas){
            byte result = datas[0];
            for (int i = 1; i < datas.length; i++) {
                result = (byte) (result ^ (datas[i]));
            }

            return result;
        }
///////////////////////云台校验码生成////////////////////////////
        public byte getYtCheck ( byte[] datas){
            //获取云台验证码
            return (byte) (datas[4] + datas[5] + datas[6] + datas[7]+ datas[8]);
        }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (!mPlayerStatus) {
            //恢复处于暂停播放状态的窗口
            startRealPlay(textureView.getSurfaceTexture());
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private void startRealPlay(SurfaceTexture surface) {
        mPlayer.setSurfaceTexture(surface);
        new Thread(() -> {
            //TODO 注意: 不要通过判断 startRealPlay() 方法返回 true 来确定播放成功，播放成功会通过HikVideoPlayerCallback回调，startRealPlay() 方法返回 false 即代表 播放失败;
            if(TextUtils.isEmpty(ipAddressYun)){
                Log.v("startRealPlay","摄像头地址非法");
                return;
            }
            if (!mPlayer.startRealPlay(ipAddressYun, MainActivity.this)) {
                onPlayerStatus(Status.FAILED, mPlayer.getLastError());
            }
        }).start();

    }

    @Override
    public void onPlayerStatus(Status status, int i) {
        //TODO 注意: 由于 HikVideoPlayerCallback 是在子线程中进行回调的，所以一定要切换到主线程处理UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //只有播放成功时，才允许开启电子放大
                frameLayout.setAllowOpenDigitalZoom(status == Status.SUCCESS);
                switch (status) {
                    case SUCCESS:
                        //播放成功
                        mPlayerStatus = true;
                        textureView.setKeepScreenOn(true);//保持亮屏
                        break;
                    case FAILED:
                        //播放失败
                        mPlayerStatus = false;
                        Log.v("onPlayerStatus","失败");
                        break;
                    case EXCEPTION:
                        //取流异常
                        mPlayerStatus = false;
                        mPlayer.stopPlay();//TODO 注意:异常时关闭取流
                        Log.v("onPlayerStatus","取流异常");
                        break;
                }
            }
        });
    }


}
