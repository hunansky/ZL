package com.wdtx.littlebell.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.wdtx.littlebell.MyApplication;
import com.wdtx.littlebell.R;
import com.wdtx.littlebell.model.RelationQueryResult;
import com.wdtx.littlebell.model.RelationState;
import com.wdtx.littlebell.model.User;
import com.wdtx.littlebell.other.BitmapCache;
import com.wdtx.littlebell.util.VolleyCallBack;
import com.wdtx.littlebell.util.VolleyUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wdtx.littlebell.other.Constant.SERVER_HOST;

public class ApplyActivity extends AppCompatActivity
        implements VolleyCallBack<List<RelationQueryResult>>,View.OnClickListener {

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, ApplyActivity.class);
        context.startActivity(intent);
    }

    private List<RelationQueryResult> list = new ArrayList<>();
    private RelationQueryResult query;

    private Adapter adapter;
    private MyAdapter myAdapter;
    private EditText etAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply);

        Toolbar toolbar = (Toolbar) findViewById(R.id.id_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("成员申请");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ListView result = (ListView) findViewById(R.id.id_lv_result);
        ListView recode = (ListView) findViewById(R.id.id_lv_recode);
        adapter = new Adapter();
        myAdapter = new MyAdapter();

        recode.setAdapter(adapter);
        result.setAdapter(myAdapter);

        User user = MyApplication.instance.getUser();
        Map<String, String> map = new HashMap<>();
        map.put("userId", user.getId() + "");
        map.put("secretKey", user.getSecretKey());
        map.put("total", true + "");
        VolleyUtil.getInstance().requestPost("/relation", map, this);

        etAccount = (EditText) findViewById(R.id.id_et_account);
        Button button = (Button) findViewById(R.id.id_btn_search);
        button.setOnClickListener(this);
    }

    @Override
    public void onSuccess(List<RelationQueryResult> data) {
        list = data;
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onFail(int code, String msg) {
        onError(null);
    }

    @Override
    public void onError(VolleyError error) {
        Toast.makeText(this, "网络异常", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        User user = MyApplication.instance.getUser();
        Map<String, String> map = new HashMap<>();
        map.put("userId", user.getId() + "");
        map.put("secretKey", user.getSecretKey());
        map.put("queryAccount",etAccount.getText().toString().trim());
        VolleyUtil.getInstance().requestPost("/relation/query", map, new VolleyCallBack<RelationQueryResult>() {
            @Override
            public void onSuccess(RelationQueryResult data) {
                query = data;
                if (query == null){
                    Toast.makeText(ApplyActivity.this, "未找到该用户", Toast.LENGTH_SHORT).show();
                }
                myAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFail(int code, String msg) {
                Toast.makeText(ApplyActivity.this, msg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(VolleyError error) {
                Toast.makeText(ApplyActivity.this, "网络异常", Toast
                        .LENGTH_SHORT).show();
            }
        });
    }

    ImageLoader loader = new ImageLoader(
            VolleyUtil.getRequestQueue(), new BitmapCache());

    private class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return query == null ? 0 : 1;
        }

        @Override
        public RelationQueryResult getItem(int position) {
            return query;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.activity_confirm_list,
                        parent, false);
                holder = new ViewHolder();
                holder.imageView = (NetworkImageView) convertView.findViewById(R.id.id_iv_header);
                holder.button = (Button) convertView.findViewById(R.id.id_btn_confirm);
                holder.text1 = (TextView) convertView.findViewById(R.id.id_tv_nickname);
                holder.text2 = (TextView) convertView.findViewById(R.id.id_tv_account);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.text1.setText(getItem(position).getUser().getNickname());
            holder.text2.setText(getItem(position).getUser().getAccount());
            if (getItem(position).getState() == RelationState.CONFIRMED){
                holder.button.setText("已通过");
                holder.button.setBackgroundColor(Color.WHITE);
                holder.button.setTextColor(Color.BLACK);
                holder.button.setEnabled(false);
            } else if (getItem(position).getState() == RelationState.WAITING_CONFIRM){
                holder.button.setText("等待同意");
                holder.button.setBackgroundColor(Color.WHITE);
                holder.button.setTextColor(Color.BLACK);
                holder.button.setEnabled(false);
            } else {
                holder.button.setBackgroundColor(0xff42bd41);
                holder.button.setTextColor(Color.WHITE);
                holder.button.setEnabled(true);
                holder.button.setText("申  请");
            }

            holder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    User user = MyApplication.instance.getUser();
                    Map<String,String> map = new HashMap<>();
                    map.put("babyId",getItem(position).getUser().getId()+"");
                    map.put("parentId",user.getId()+"");
                    VolleyUtil.getInstance().requestPost("/relation/new", map,
                            new VolleyCallBack<String>() {
                                @Override
                                public void onSuccess(String data) {
                                    holder.button.setText("等待同意");
                                    holder.button.setBackgroundColor(Color.WHITE);
                                    holder.button.setTextColor(Color.BLACK);
                                    holder.button.setEnabled(false);
                                }

                                @Override
                                public void onFail(int code, String msg) {
                                    onError(null);
                                }

                                @Override
                                public void onError(VolleyError error) {
                                    Toast.makeText(ApplyActivity.this, "网络异常", Toast
                                            .LENGTH_SHORT).show();
                                }
                            });
                }
            });

            holder.imageView.setImageUrl(
                    SERVER_HOST + "/upload/header/" + getItem(position).getUser()
                            .getHeader(), loader);
            return convertView;
        }
    }

    private class Adapter extends BaseAdapter {

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public RelationQueryResult getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.activity_confirm_list,
                        parent, false);
                holder = new ViewHolder();
                holder.imageView = (NetworkImageView) convertView.findViewById(R.id.id_iv_header);
                holder.button = (Button) convertView.findViewById(R.id.id_btn_confirm);
                holder.text1 = (TextView) convertView.findViewById(R.id.id_tv_nickname);
                holder.text2 = (TextView) convertView.findViewById(R.id.id_tv_account);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.text1.setText(getItem(position).getUser().getNickname());
            holder.text2.setText(getItem(position).getUser().getAccount());
            if (getItem(position).getState() == RelationState.CONFIRMED){
                holder.button.setBackgroundColor(Color.WHITE);
                holder.button.setTextColor(Color.BLACK);
                holder.button.setEnabled(false);
                holder.button.setText("已通过");
            } else if (getItem(position).getState() == RelationState.WAITING_CONFIRM){
                holder.button.setBackgroundColor(Color.WHITE);
                holder.button.setTextColor(Color.BLACK);
                holder.button.setEnabled(false);
                holder.button.setText("等待同意");
            }
            holder.imageView.setImageUrl(
                    SERVER_HOST + "/upload/header/" + getItem(position).getUser()
                            .getHeader(), loader);
            return convertView;
        }
    }

    private class ViewHolder {
        NetworkImageView imageView;
        Button button;
        TextView text1;
        TextView text2;
    }
}