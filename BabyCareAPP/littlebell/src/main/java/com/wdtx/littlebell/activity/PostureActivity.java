package com.wdtx.littlebell.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.wdtx.littlebell.MyApplication;
import com.wdtx.littlebell.R;
import com.wdtx.littlebell.model.Posture;
import com.wdtx.littlebell.model.PostureDistribute;
import com.wdtx.littlebell.model.PostureWeight;
import com.wdtx.littlebell.model.User;
import com.wdtx.littlebell.other.Constant;
import com.wdtx.littlebell.util.VolleyCallBack;
import com.wdtx.littlebell.util.VolleyUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lecho.lib.hellocharts.listener.PieChartOnValueSelectListener;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.PieChartView;

import static com.wdtx.littlebell.other.Constant.KEY_INTEGER_BABY_ID;
import static com.wdtx.littlebell.other.Constant.PREFERENCES_BIND_BABY;

public class PostureActivity extends AppCompatActivity {

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, PostureActivity.class);
        context.startActivity(intent);
    }

    private List<SliceValue> values = new ArrayList<>();
    private PieChartView pieChartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posture);

        Toolbar toolbar = (Toolbar) findViewById(R.id.id_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("睡姿监控");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ListView listView = (ListView) findViewById(R.id.id_lv_posture);
        final Adapter adapter = new Adapter();
        listView.setAdapter(adapter);

        pieChartView = (PieChartView) findViewById(R.id.pie_chart_view);
        pieChartView.setValueSelectionEnabled(true);
        pieChartView.setOnValueTouchListener(new PieChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int i, SliceValue sliceValue) {
                PostureWeight weight = list.get(i);
                String message = weight.getPosture().display() + ":总时长 " + weight
                        .getMinute()+"分钟,占 " + weight.getPercentage() / 100d +"%";
                Toast.makeText(PostureActivity.this,message,Toast.LENGTH_LONG).show();
            }

            @Override
            public void onValueDeselected() {

            }
        });
        prepareData(null);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences preferences = getSharedPreferences
                        (PREFERENCES_BIND_BABY, MODE_PRIVATE);
                Map<String, String> map = new HashMap<>();
                map.put("day", "" + position);
                User user = MyApplication.instance.getUser();
                if (user.getRole() == 2) {
                    map.put("desId", user.getId() + "");
                } else {
                    map.put("desId", preferences.getInt(KEY_INTEGER_BABY_ID, -1) + "");
                }
                VolleyUtil.getInstance().requestPost("/posture/distribute", map,
                        new VolleyCallBack<List<PostureDistribute>>() {
                            @Override
                            public void onSuccess(List<PostureDistribute> data) {
                                adapter.setData(data);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onFail(int code, String msg) {
                                Toast.makeText(PostureActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(VolleyError error) {
                                error.printStackTrace();
                                Toast.makeText(PostureActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
                            }
                        });

                VolleyUtil.getInstance().requestPost("/posture/weight", map, new VolleyCallBack<List<PostureWeight>>() {
                    @Override
                    public void onSuccess(List<PostureWeight> data) {
                        prepareData(data);
                    }

                    @Override
                    public void onFail(int code, String msg) {
                        Toast.makeText(PostureActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(PostureActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner.setSelection(0);
    }

    private List<PostureWeight> list;

    private void prepareData(List<PostureWeight> list) {
        this.list = list;
        values.clear();
        if (list == null || list.size() == 0){
            SliceValue value = new SliceValue(100, ChartUtils.nextColor());
            value.setLabel("无数据");
            values.add(value);
            return;
        }
        for (PostureWeight weight : list) {
            SliceValue value = new SliceValue(weight.getPercentage(), ChartUtils
                    .pickColor());
            value.setLabel(weight.getPosture().display());
            values.add(value);
        }

        PieChartData pieChartData = new PieChartData(values);
        pieChartData.setHasLabels(true);
        pieChartData.setHasLabelsOutside(false);
        pieChartView.setPieChartData(pieChartData);
    }

    private class Adapter extends BaseAdapter {

        private List<PostureDistribute> data = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        public void setData(List<PostureDistribute> data) {
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public PostureDistribute getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(android.R.layout
                        .simple_list_item_2, parent, false);
                holder = new ViewHolder();
                holder.text1 = (TextView) convertView.findViewById(android.R.id
                        .text1);
                holder.text2 = (TextView) convertView.findViewById(android.R.id
                        .text2);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.text1.setText(getItem(position).getPosture().display());
            holder.text2.setText(
                    sdf.format(new Date(getItem(position).getStartTime())) +
                            "~" +
                            sdf.format(new Date(getItem(position).getEndTime())));
            return convertView;
        }
    }

    private class ViewHolder {
        TextView text1;
        TextView text2;
    }


}
