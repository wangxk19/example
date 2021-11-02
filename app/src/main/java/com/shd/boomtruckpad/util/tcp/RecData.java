package com.shd.boomtruckpad.util.tcp;

/**
 * 接收数据的类型
 * 每帧数据13个字节 1个头 4个id 8个data
 */

public class RecData {
    private byte head;
    private byte[] id;
    private byte[] data;



    public RecData(byte head, byte[] id, byte[] data) {
        this.head = head;
        this.id = id;
        this.data = data;
    }

    public byte getHead() {
        return head;
    }

    public void setHead(byte head) {
        this.head = head;
    }

    public byte[] getId() {
        return id;
    }

    public void setId(byte[] id) {
        this.id = id;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
