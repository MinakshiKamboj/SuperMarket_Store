package com.supermarket.store.adepter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.supermarket.store.R;
import com.supermarket.store.model.StoreReportDataItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.supermarket.store.retrofit.APIClient.baseUrl;

public class Homeadepter extends RecyclerView.Adapter<Homeadepter.ViewHolder> {
    List<StoreReportDataItem> dataItemList;
    Context mContext;

    public Homeadepter(List<StoreReportDataItem> dataItemList,Context context) {
        this.dataItemList = dataItemList;
        mContext=context;
    }

    @Override
    public Homeadepter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.home_item, parent, false);
        Homeadepter.ViewHolder viewHolder = new Homeadepter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(Homeadepter.ViewHolder holder,
                                 int position) {
        Log.e("position", "" + position);
        StoreReportDataItem item=dataItemList.get(position);
        holder.txtTitle.setText(""+item.getTitle());
        holder.txtVelues.setText(""+item.getReportData());
        Glide.with(mContext).load(baseUrl + item.getImgurl()).placeholder(R.drawable.ic_complet_order).into(holder.imgTop);


    }

    @Override
    public int getItemCount() {
        return dataItemList.size();
    }

    public void filterListed(ArrayList<StoreReportDataItem> mytempLists1) {
        this.dataItemList = mytempLists1;
        Log.d("list,",new Gson().toJson(mytempLists1));
        notifyDataSetChanged();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txt_title)
        TextView txtTitle;
        @BindView(R.id.txt_velues)
        TextView txtVelues;

        @BindView(R.id.img_top)
        ImageView imgTop;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}