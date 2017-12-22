package com.wdtx.littlebell.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.wdtx.littlebell.R;
import com.wdtx.littlebell.model.Article;
import com.wdtx.littlebell.other.BitmapCache;
import com.wdtx.littlebell.util.VolleyCallBack;
import com.wdtx.littlebell.util.VolleyUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wdtx.littlebell.other.Constant.SERVER_HOST;

public class FoundFragment extends Fragment implements VolleyCallBack<List<Article>>{

    private List<Article> mArticles = new ArrayList<>();

    private MyAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_found, container, false);

        ListView lvFound = (ListView) view.findViewById(R.id.id_lv_found);
        adapter = new MyAdapter();
        lvFound.setAdapter(adapter);
        lvFound.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WebActivity.startActivity(getActivity(),
                        adapter.getItem(position).getUrl());
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        requestArticles(0, 10);
    }

    public void requestArticles(int offset, int count) {
        Map<String,String> param = new HashMap<>();
        param.put("offset",offset+"");
        param.put("count",count+"");
        VolleyUtil.getInstance().requestGet("/articles",param,this);
    }

    @Override
    public void onSuccess(List<Article> data) {
        mArticles = data;
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onFail(int code, String msg) {
    }

    @Override
    public void onError(VolleyError error) {
    }

    private class MyAdapter extends BaseAdapter {

        LayoutInflater mInflater = getActivity().getLayoutInflater();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        ImageLoader loader = new ImageLoader(
                VolleyUtil.getRequestQueue(), new BitmapCache());

        @Override
        public int getCount() {
            return mArticles.size();
        }

        @Override
        public Article getItem(int position) {
            return mArticles.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(
                        R.layout.fragment_found_list, parent, false);
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.id_tv_title);
                holder.time = (TextView) convertView.findViewById(R.id.id_tv_time);
                holder.image = (NetworkImageView) convertView.findViewById(R.id.id_iv_image);
                holder.image.setDefaultImageResId(R.drawable.img_default);
                holder.image.setErrorImageResId(R.drawable.img_broken);
                holder.summary = (TextView) convertView.findViewById(R.id.id_tv_summary);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Article article = getItem(position);

            holder.title.setText(article.getTitle());
            holder.time.setText(sdf.format(article.getTime()));
            holder.summary.setText(article.getSummary());
            holder.image.setImageUrl(
                    SERVER_HOST + "/upload/" + article.getImage(),loader);
            return convertView;
        }
    }

    private final class ViewHolder {
        TextView title;
        TextView time;
        NetworkImageView image;
        TextView summary;
    }

}
