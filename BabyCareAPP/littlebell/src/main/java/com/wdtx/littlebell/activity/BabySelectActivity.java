package com.wdtx.littlebell.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.wdtx.littlebell.MyApplication;
import com.wdtx.littlebell.R;
import com.wdtx.littlebell.model.RelationQueryResult;
import com.wdtx.littlebell.model.User;
import com.wdtx.littlebell.other.BitmapCache;
import com.wdtx.littlebell.util.VolleyCallBack;
import com.wdtx.littlebell.util.VolleyUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wdtx.littlebell.other.Constant.KEY_INTEGER_BABY_ID;
import static com.wdtx.littlebell.other.Constant.KEY_INTEGER_USER_ID;
import static com.wdtx.littlebell.other.Constant.KEY_STRING_BABY_NAME;
import static com.wdtx.littlebell.other.Constant.PREFERENCES_BIND_BABY;
import static com.wdtx.littlebell.other.Constant.SERVER_HOST;

public class BabySelectActivity extends AppCompatActivity
        implements VolleyCallBack<List<RelationQueryResult>> {

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, BabySelectActivity.class);
        context.startActivity(intent);
    }

    private List<RelationQueryResult> list = new ArrayList<>();

    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member);

        Toolbar toolbar = (Toolbar) findViewById(R.id.id_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("家庭成员");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ListView listView = (ListView) findViewById(R.id.id_lv_member);
        adapter = new Adapter();
        listView.setAdapter(adapter);

        User user = MyApplication.instance.getUser();
        Map<String, String> map = new HashMap<>();
        map.put("userId", user.getId() + "");
        map.put("secretKey", user.getSecretKey());
        map.put("total", false + "");
        VolleyUtil.getInstance().requestPost("/relation", map, this);
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

    private class Adapter extends BaseAdapter {

        ImageLoader loader = new ImageLoader(
                VolleyUtil.getRequestQueue(), new BitmapCache());

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

            ViewHolder holder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.activity_member_list,
                        parent, false);
                holder = new ViewHolder();
                holder.imageView = (NetworkImageView) convertView.findViewById(R.id.id_iv_header);
                holder.text1 = (TextView) convertView.findViewById(R.id.id_tv_nickname);
                holder.text2 = (TextView) convertView.findViewById(R.id.id_tv_account);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.text1.setText(getItem(position).getUser().getNickname());
            holder.text2.setText(getItem(position).getUser().getAccount());
            holder.imageView.setImageUrl(
                    SERVER_HOST + "/upload/header/" + getItem(position).getUser()
                            .getHeader(), loader);

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences preferences = getSharedPreferences
                            (PREFERENCES_BIND_BABY, MODE_PRIVATE);
                    preferences
                            .edit()
                            .putInt(KEY_INTEGER_USER_ID,
                                    MyApplication.instance.getUser().getId())
                            .putInt(KEY_INTEGER_BABY_ID,
                                    getItem(position).getUser().getId())
                            .putString(KEY_STRING_BABY_NAME,
                                    "宝宝：" + getItem(position).getUser().getNickname())
                            .apply();
                    finish();
                }
            });
            return convertView;
        }
    }

    private class ViewHolder {
        NetworkImageView imageView;
        TextView text1;
        TextView text2;
    }
}
