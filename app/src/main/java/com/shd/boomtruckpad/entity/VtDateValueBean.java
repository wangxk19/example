package com.shd.boomtruckpad.entity;

public class VtDateValueBean {

    private float FValue;
    private String itemName;

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public float getFValue() {
        return FValue;
    }

    public void setFValue(float FValue) {
        this.FValue = FValue;
    }

    public VtDateValueBean(float FValue, String itemName) {
        this.FValue = FValue;
        this.itemName = itemName;
    }
}
