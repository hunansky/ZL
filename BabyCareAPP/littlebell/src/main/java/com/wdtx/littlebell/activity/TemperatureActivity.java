package com.wdtx.littlebell.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.wdtx.littlebell.MyApplication;
import com.wdtx.littlebell.R;
import com.wdtx.littlebell.model.PostureDistribute;
import com.wdtx.littlebell.model.PostureWeight;
import com.wdtx.littlebell.model.Temperature;
import com.wdtx.littlebell.model.User;
import com.wdtx.littlebell.util.VolleyCallBack;
import com.wdtx.littlebell.util.VolleyUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;

import static com.wdtx.littlebell.other.Constant.KEY_INTEGER_BABY_ID;
import static com.wdtx.littlebell.other.Constant.PREFERENCES_BIND_BABY;

public class TemperatureActivity extends AppCompatActivity {

    private LineChartView chart;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, TemperatureActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);

        Toolbar toolbar = (Toolbar) findViewById(R.id.id_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("温度监控");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        chart = (LineChartView) findViewById(R.id.line_chart_view);

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
                VolleyUtil.getInstance().requestPost("/temperature", map,
                        new VolleyCallBack<List<Temperature>>() {
                            @Override
                            public void onSuccess(List<Temperature> data) {
                                generateData(data);
                            }

                            @Override
                            public void onFail(int code, String msg) {
                                Toast.makeText(TemperatureActivity.this, msg, Toast.LENGTH_SHORT)
                                        .show();
                            }

                            @Override
                            public void onError(VolleyError error) {
                                error.printStackTrace();
                                Toast.makeText(TemperatureActivity.this, "网络异常", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

    private void generateData(List<Temperature> temperatures) {
        List<AxisValue> axisValues = new ArrayList<AxisValue>();
        List<PointValue> values = new ArrayList<>();
        for (int j = 0; j < temperatures.size(); ++j) {
            PointValue pointValue =
                    new PointValue(j, (float) temperatures.get(j).getTemperature());
            pointValue.setLabel(temperatures.get(j).getTemperature()+"");
            values.add(pointValue);
            axisValues.add(new AxisValue(j).setLabel(sdf.format(temperatures.get
                    (j).getTime())));
        }

        Line line = new Line(values);
        line.setColor(ChartUtils.pickColor());
        line.setShape(ValueShape.CIRCLE);
        line.setCubic(true);
        line.setFilled(false);
        line.setHasLabels(true);
        line.setHasLabelsOnlyForSelected(true);
        line.setHasLines(true);
        line.setHasPoints(true);

        List<Line> lines = new ArrayList<>();
        lines.add(line);

        LineChartData data = new LineChartData(lines);

        Axis axisX = new Axis(axisValues).setHasLines(true);
        Axis axisY = new Axis().setHasLines(true);

        axisX.setName("时间");
        axisY.setName("温度");
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);
        data.setBaseValue(0);
        chart.setLineChartData(data);
        chart.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
        Viewport currentViewport = chart.getMaximumViewport();
        currentViewport.top = 40f;
        currentViewport.bottom = 25f;

        chart.setMaximumViewport(currentViewport);
        chart.setCurrentViewport(currentViewport);
        chart.setValueSelectionEnabled(true);
    }
}
