package com.shd.boomtruckpad;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @author: Jun
 * @Date: 2021/7/12 15:14
 * @Description:
 */
@Entity
public class FaultBean {
    @Id(autoincrement = true)//设置自增长
    long id;
    String faultContent;
    String createTime;



    @Generated(hash = 2004942952)
    public FaultBean(long id, String faultContent, String createTime) {
        this.id = id;
        this.faultContent = faultContent;
        this.createTime = createTime;
    }

    @Generated(hash = 306665377)
    public FaultBean() {
    }

   

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFaultContent() {
        return faultContent;
    }

    public void setFaultContent(String faultContent) {
        this.faultContent = faultContent;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
