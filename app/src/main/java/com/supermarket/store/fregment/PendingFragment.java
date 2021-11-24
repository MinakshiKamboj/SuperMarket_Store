package com.supermarket.store.fregment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.supermarket.store.R;
import com.supermarket.store.activity.OrderPendingDetailsActivity;
import com.supermarket.store.model.OrderHistoryItem;
import com.supermarket.store.model.Pending;
import com.supermarket.store.model.Store;
import com.supermarket.store.retrofit.APIClient;
import com.supermarket.store.retrofit.GetResult;
import com.supermarket.store.utils.CustPrograssbar;
import com.supermarket.store.utils.SessionManager;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionButton;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionHelper;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionLayout;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RFACLabelItem;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RapidFloatingActionContentLabelList;
import com.wangjie.rapidfloatingactionbutton.util.RFABTextUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;

import static com.supermarket.store.utils.SessionManager.curruncy;


public class PendingFragment extends Fragment implements GetResult.MyListener, RapidFloatingActionContentLabelList.OnRapidFloatingActionContentLabelListListener {
    @BindView(R.id.txt_itmecount)
    TextView txtItmecount;
    @BindView(R.id.recycle_pending)
    RecyclerView recyclePending;
    @BindView(R.id.activity_main_rfab)
    RapidFloatingActionButton rfaButton;
    @BindView(R.id.activity_main_rfal)
    RapidFloatingActionLayout rfaLayout;
    @BindView(R.id.txt_titel)
    TextView txtTitel;
    public PendingFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    CustPrograssbar custPrograssbar;
    SessionManager sessionManager;
    Store user;
    List<OrderHistoryItem> pendinglistMain = new ArrayList<>();
    PendingAdepter myOrderAdepter;
    private RapidFloatingActionHelper rfabHelper;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pendding, container, false);
        ButterKnife.bind(this, view);
        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(getActivity());
        recyclePending.setLayoutManager(recyclerLayoutManager);
        custPrograssbar = new CustPrograssbar();
        sessionManager = new SessionManager(getActivity());
        user = sessionManager.getUserDetails("");
        txtTitel.setText("Pending Order");
        getPendingOrder("Pending");
        setFloting();
        return view;
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setFloting() {
        RapidFloatingActionContentLabelList rfaContent = new RapidFloatingActionContentLabelList(getActivity());
        rfaContent.setOnRapidFloatingActionContentLabelListListener(this);
        List<RFACLabelItem> items = new ArrayList<>();
        items.add(new RFACLabelItem<Integer>()
                .setLabel("Pending")
                .setResId(R.drawable.ic_check)
                .setIconNormalColor(Color.parseColor("#FF6F6F"))
                .setIconPressedColor(R.color.colorGreen)
                .setLabelColor(Color.WHITE)
                .setLabelBackgroundDrawable(getActivity().getDrawable(R.drawable.button_round))
                .setWrapper(0)
        );
        items.add(new RFACLabelItem<Integer>()
                .setLabel("Complete")
                .setResId(R.drawable.ic_check)
                .setIconNormalColor(Color.parseColor("#FF6F6F"))
                .setIconPressedColor(R.color.colorGreen)
                .setLabelColor(Color.WHITE)
                .setLabelBackgroundDrawable(getActivity().getDrawable(R.drawable.button_round))
                .setWrapper(1)
        );
        items.add(new RFACLabelItem<Integer>()
                .setLabel("Cancle")
                .setResId(R.drawable.ic_check)
                .setIconNormalColor(Color.parseColor("#FF6F6F"))
                .setIconPressedColor(R.color.colorGreen)
                .setLabelColor(Color.WHITE)
                .setLabelBackgroundDrawable(getActivity().getDrawable(R.drawable.button_round))
                .setWrapper(2)
        );
        rfaContent
                .setItems(items)
                .setIconShadowRadius(RFABTextUtil.dip2px(getActivity(), 2))
                .setIconShadowColor(R.color.colorGreen)
                .setIconShadowDy(RFABTextUtil.dip2px(getActivity(), 1))
        ;
        rfabHelper = new RapidFloatingActionHelper(
                getActivity(),
                rfaLayout,
                rfaButton,
                rfaContent
        ).build();
    }

    private void getPendingOrder(String status) {
        custPrograssbar.prograssCreate(getActivity());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("sid", user.getId());
            jsonObject.put("status", status);
            JsonParser jsonParser = new JsonParser();

            Call<JsonObject> call = APIClient.getInterface().getPending((JsonObject) jsonParser.parse(jsonObject.toString()));
            GetResult getResult = new GetResult();
            getResult.setMyListener(this);
            getResult.callForLogin(call, "1");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void callback(JsonObject result, String callNo) {
        try {
            custPrograssbar.closePrograssBar();
            if (callNo.equalsIgnoreCase("1")) {

                Gson gson = new Gson();
                Pending pending = gson.fromJson(result.toString(), Pending.class);
                if (pending.getResult().equalsIgnoreCase("true")) {
                    txtItmecount.setText(pending.getOrderHistory().size() + " Orders");
                    if (!pending.getOrderHistory().isEmpty()) {
                        pendinglistMain = pending.getOrderHistory();
                        myOrderAdepter = new PendingAdepter(pendinglistMain);
                        recyclePending.setAdapter(myOrderAdepter);
                    }
                }
            }

        } catch (Exception e) {
            e.toString();
        }
    }

    @Override
    public void onRFACItemLabelClick(int position, RFACLabelItem item) {
        txtTitel.setText(item.getLabel() + " Order");
        getPendingOrder(item.getLabel());
        rfabHelper.toggleContent();
    }

    @Override
    public void onRFACItemIconClick(int position, RFACLabelItem item) {
        txtTitel.setText(item.getLabel() + " Order");
        getPendingOrder(item.getLabel());
        rfabHelper.toggleContent();
    }

    public class PendingAdepter extends RecyclerView.Adapter<PendingAdepter.ViewHolder> {
        private List<OrderHistoryItem> pendinglist;

        public PendingAdepter(List<OrderHistoryItem> pendinglist) {
            this.pendinglist = pendinglist;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent,
                                             int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.pending_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder,
                                     int position) {
            Log.e("position", "" + position);
            OrderHistoryItem order = pendinglist.get(position);
            holder.txtOderid.setText("OrderID #" + order.getId());
            holder.txtStuts.setText(" " + order.getStatus() + " ");
            holder.txtDateandstatus.setText(order.getOrderDate());
            holder.txtAddress.setText("" + order.getCustAdd());
            holder.txtPricetotla.setText(sessionManager.getStringData(curruncy) + " " + order.getTotal());
            holder.lvlClick.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    startActivity(new Intent(getActivity(), OrderPendingDetailsActivity.class).putExtra("OrderID", order.getId()));
                }
            });
        }

        @Override
        public int getItemCount() {
            return pendinglist.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.txt_oderid)
            TextView txtOderid;
            @BindView(R.id.txt_pricetotla)
            TextView txtPricetotla;
            @BindView(R.id.txt_dateandstatus)
            TextView txtDateandstatus;
            @BindView(R.id.txt_address)
            TextView txtAddress;
            @BindView(R.id.txt_stuts)
            TextView txtStuts;

            @BindView(R.id.lvl_click)
            LinearLayout lvlClick;
            @BindView(R.id.img_right)
            ImageView imgRight;

            public ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }
    }
}
