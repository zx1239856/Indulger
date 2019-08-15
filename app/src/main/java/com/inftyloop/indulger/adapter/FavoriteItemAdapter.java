package com.inftyloop.indulger.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inftyloop.indulger.R;
import com.inftyloop.indulger.viewholder.BaseRecyclerViewHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FavoriteItemAdapter extends RecyclerView.Adapter<BaseRecyclerViewHolder> {
    List<Map<String, Object>> mData;

    public FavoriteItemAdapter(List<Map<String, Object>> data) {
        mData = data;
    }

    @Override
    public BaseRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BaseRecyclerViewHolder(parent, R.layout.favorite_item);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseRecyclerViewHolder viewHolder, int position) {
        final Map<String, Object> item = mData.get(position);

        ((TextView) viewHolder.findViewById(R.id.date)).setText((String) item.get("date"));
        ((ImageView) viewHolder.findViewById(R.id.img)).setImageResource((int) item.get("img"));
        ((TextView) viewHolder.findViewById(R.id.press)).setText((String) item.get("press"));
        ((TextView) viewHolder.findViewById(R.id.title)).setText((String) item.get("title"));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void add(int imageSource, String title, String press, String date) {
        Map<String, Object> map = new HashMap<>();
        map.put("img", imageSource);
        map.put("title", title);
        map.put("press", press);
        map.put("date", date);
        mData.add(map);
        notifyDataSetChanged();
    }

    public List<Map<String, Object>> getData() {
        return mData;
    }
}