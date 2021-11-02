package com.shd.boomtruckpad.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.shd.boomtruckpad.R;

/**
 * @author: Jun
 * @Date: 2021/7/11 21:25
 * @Description:
 */

public class CustomEditTextDialog extends Dialog {
    Context mContext;
    private TextView btnSure;
    private TextView btnCancle;
    private TextView title;
    private EditText etAlong;
    private EditText etReverse;
    private TextView tvReverse,tvAlong;
    private RadioGroup rg_option_select;
    private RadioButton rb_option1,rb_option2;
    public CustomEditTextDialog(@NonNull Context context) {
        super(context, R.style.CustomDialog);
        this.mContext = context;
        initView();
    }

    //初始化
    public void initView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_input_data, null);
        title = (TextView) view.findViewById(R.id.title);
        tvAlong = (TextView) view.findViewById(R.id.tv_along);
        tvReverse = (TextView) view.findViewById(R.id.tv_reverse);
        etAlong = (EditText) view.findViewById(R.id.et_along);
        etReverse = (EditText) view.findViewById(R.id.et_reverse);
        btnSure = (TextView) view.findViewById(R.id.dialog_confirm_sure);
        btnCancle = (TextView) view.findViewById(R.id.dialog_confirm_cancle);
        rg_option_select = view.findViewById(R.id.rg_option_select);
        rb_option1 = view.findViewById(R.id.rb_option1);
        rb_option2 = view.findViewById(R.id.rb_option2);
        super.setContentView(view);
    }


    public CustomEditTextDialog setTile(String s) {
        title.setText(s);
        return this;
    }

    public CustomEditTextDialog setAlong(String s) {
        tvAlong.setText(s);
        return this;
    }

    public CustomEditTextDialog setAlongHint(String s) {
        tvAlong.setHint(s);
        return this;
    }
    public CustomEditTextDialog setReverse (String s) {
        tvReverse.setText(s);
        return this;
    }
    public CustomEditTextDialog setReverseHint (String s) {
        tvReverse.setHint(s);
        return this;
    }
    //获取当前输入框对象
    public View getAlongEditText() {
        return etAlong;
    }

    //获取当前输入框对象
    public View getRgSelect() {
        return rg_option_select;
    }

    public View getOption1() {
        return rb_option1;
    }
    public View getOption2() {
        return rb_option2;
    }

    public View getReverseEditText() {
        return etReverse;
    }
    //确定键监听器
    public void setOnSureListener(View.OnClickListener listener) {
        btnSure.setOnClickListener(listener);
    }

    //取消键监听器
    public void setOnCanlceListener(View.OnClickListener listener) {
        btnCancle.setOnClickListener(listener);
    }
}

