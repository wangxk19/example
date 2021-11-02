package com.shd.boomtruckpad.util;

import android.graphics.Color;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.shd.boomtruckpad.FaultBean;
import com.shd.boomtruckpad.entity.VtDateValueBean;

import java.util.ArrayList;
import java.util.List;

public class UtilBar {
    private YAxis leftAxis;             //左侧Y轴
    private YAxis rightAxis;            //右侧Y轴
    private XAxis xAxis;                //X轴
    private Legend legend;              //图例
    private LimitLine limitLine;//限制线
    /**
     * 初始化BarChart图表
     *
     *
     */
    public void initBarChart(BarChart barChart,int color) {
        /***图表设置***/
        //背景颜色
        barChart.setBackgroundColor(color);
        //不显示图表网格
        barChart.setDrawGridBackground(false);
        //背景阴影
        barChart.setDrawBarShadow(false);
        barChart.setHighlightFullBarEnabled(false);
        //显示边框
        barChart.setDrawBorders(true);

        /***XY轴的设置***/
        //X轴设置显示位置在底部
        xAxis = barChart.getXAxis();
        leftAxis = barChart.getAxisLeft();
        rightAxis = barChart.getAxisRight();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);


        xAxis.setDrawGridLines(false);
        //右侧Y轴网格线设置为虚线
        rightAxis.setDrawGridLines(false);
        //rightAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setEnabled(false);
        rightAxis.setTextColor(Color.WHITE);

        //不显示右下角的描述
        Description description = new Description();
        description.setEnabled(false);
        barChart.setDescription(description);
        /***折线图例 标签 设置***/
        legend = barChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextSize(15f);
        legend.setTextColor(Color.WHITE);
        //显示位置
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        //是否绘制在图表里面
        legend.setDrawInside(false);
    }
    /**
     * 柱状图始化设置 一个BarDataSet 代表一列柱状图
     *
     * @param barDataSet 柱状图
     * @param color      柱状图颜色
     */
    public void initBarDataSet(BarDataSet barDataSet, int color) {
        barDataSet.setColor(color);
        barDataSet.setFormLineWidth(1f);
        barDataSet.setFormSize(15.f);
        //不显示柱状图顶部值
        barDataSet.setDrawValues(true);

//        barDataSet.setValueTextSize(10f);
        barDataSet.setValueTextColor(Color.WHITE);

    }
    public void showBarChart(List<VtDateValueBean> dateValueList, String name, int color,BarChart bar) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        //X轴自定义值
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return dateValueList.get((int) value % dateValueList.size()).getItemName();
            }
        });
        //右侧Y轴自定义值
        rightAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return ((int) (value * 100)) + "%";
            }
        });
        for (int i = 0; i < dateValueList.size(); i++) {
            /**
             * 此处还可传入Drawable对象 BarEntry(float x, float y, Drawable icon)
             * 即可设置柱状图顶部的 icon展示
             */
            BarEntry barEntry = new BarEntry(i, (float) dateValueList.get(i).getFValue());
            entries.add(barEntry);
        }
        // 每一个BarDataSet代表一类柱状图
        BarDataSet barDataSet = new BarDataSet(entries, name);
        initBarDataSet(barDataSet, color);

//        // 添加多个BarDataSet时
//        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
//        dataSets.add(barDataSet);
//        BarData data = new BarData(dataSets);

        BarData data = new BarData(barDataSet);
        data.setBarWidth(0.3f);//设置柱状的宽度
        bar.setData(data);
    }


}
