package com.shd.boomtruckpad;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * @author: Jun
 * @Date: 2021/7/12 15:13
 * @Description:
 */

public class FaultAdapter extends RecyclerView.Adapter<FaultAdapter.MyHolder> {
    private Context mContext;
    private List<FaultBean> mList;

    FaultAdapter(Context context,List<FaultBean> list) {
        mList = list;
        mContext = context;
    }

    public void setData(List<FaultBean> list){
        mList = list;
        this.notifyDataSetChanged();
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclevire_item_fault, parent, false);
        MyHolder holder = new MyHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        holder.tvId.setText(String.valueOf(mList.get(position).getId()));
        holder.tvFault.setText(mList.get(position).getFaultContent());
        holder.tvCreateTime.setText(mList.get(position).getCreateTime());
        if(position%2==1){
            holder.llMain.setBackgroundColor(ContextCompat.getColor(mContext,R.color.color_recycle_item_dark_bg));
        }

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    /**
     * 自定义的ViewHolder
     */
    class MyHolder extends RecyclerView.ViewHolder {
        LinearLayout llMain;
        TextView tvId,tvFault,tvCreateTime;

        public MyHolder(View itemView) {
            super(itemView);
            llMain = itemView.findViewById(R.id.ll_main);
            tvId = itemView.findViewById(R.id.tv_id);
            tvFault = itemView.findViewById(R.id.tv_fault);
            tvCreateTime = itemView.findViewById(R.id.tv_fault_time);
        }
    }
}
