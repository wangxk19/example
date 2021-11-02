package com.shd.boomtruckpad.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.shd.boomtruckpad.DaoMaster;
import com.shd.boomtruckpad.DaoSession;
import com.shd.boomtruckpad.FaultBean;
import com.shd.boomtruckpad.FaultBeanDao;

import java.util.List;

/**
 * @author: Jun
 * @Date: 2021/7/12 15:52
 * @Description:
 */

public class DbController {
    /**
     * Helper
     */
    private DaoMaster.DevOpenHelper mHelper;
    /**
     * 数据库
     */
    private SQLiteDatabase db;
    /**
     * DaoMaster
     */
    private DaoMaster mDaoMaster;
    /**
     * DaoSession
     */
    private DaoSession mDaoSession;
    /**
     * 上下文
     */
    private Context context;
    /**
     * dao
     */
    private FaultBeanDao faultBeanDao;

    private static DbController mDbController;

    /**
     * 获取单例
     */
    public static DbController getInstance(Context context){
        if(mDbController == null){
            synchronized (DbController.class){
                if(mDbController == null){
                    mDbController = new DbController(context);
                }
            }
        }
        return mDbController;
    }
    /**
     * 初始化
     * @param context
     */
    public DbController(Context context) {
        this.context = context;
        mHelper = new DaoMaster.DevOpenHelper(context,"fault.db", null);
        mDaoMaster =new DaoMaster(getWritableDatabase());
        mDaoSession = mDaoMaster.newSession();
        faultBeanDao = mDaoSession.getFaultBeanDao();
    }
    /**
     * 获取可读数据库
     */
    private SQLiteDatabase getReadableDatabase(){
        if(mHelper == null){
            mHelper = new DaoMaster.DevOpenHelper(context,"fault.db",null);
        }
        SQLiteDatabase db =mHelper.getReadableDatabase();
        return db;
    }

    /**
     * 获取可写数据库
     * @return
     */
    private SQLiteDatabase getWritableDatabase(){
        if(mHelper == null){
            mHelper =new DaoMaster.DevOpenHelper(context,"fault.db",null);
        }
        SQLiteDatabase db = mHelper.getWritableDatabase();
        return db;
    }

    /**
     * 会自动判定是插入还是替换
     * @param
     */
    public void insertOrReplace(FaultBean info){
        faultBeanDao.insertOrReplace(info);
    }
    /**插入一条记录，表里面要没有与之相同的记录
     *
     * @param
     */
    public long insert(FaultBean info){
        return  faultBeanDao.insert(info);
    }

    /**
     * 按条件查询数据
     */
    public List<FaultBean> searchByWhere(String wherecluse){
        List<FaultBean>personInfors = (List<FaultBean>) faultBeanDao.queryBuilder().where(FaultBeanDao.Properties.CreateTime.eq(wherecluse)).build().unique();
        return personInfors;
    }
    /**
     * 查询所有数据
     */
    public List<FaultBean> searchAll(){
        List<FaultBean>personInfors=faultBeanDao.queryBuilder().list();
        return personInfors;
    }

}
