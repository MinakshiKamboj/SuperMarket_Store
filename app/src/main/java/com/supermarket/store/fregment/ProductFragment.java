package com.supermarket.store.fregment;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.supermarket.store.R;
import com.supermarket.store.activity.AddProductActivity;
import com.supermarket.store.activity.OrderPendingDetailsActivity;
import com.supermarket.store.activity.ProductActivity;
import com.supermarket.store.model.PendingOrderItem;
import com.supermarket.store.model.Product;
import com.supermarket.store.model.ProductDataItem;
import com.supermarket.store.model.Store;
import com.supermarket.store.retrofit.APIClient;
import com.supermarket.store.retrofit.GetResult;
import com.supermarket.store.utils.CustPrograssbar;
import com.supermarket.store.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;

import static com.supermarket.store.retrofit.APIClient.baseUrl;
import static com.supermarket.store.utils.SessionManager.curruncy;
import static com.supermarket.store.utils.Utiles.updatestatus;


public class ProductFragment extends Fragment implements GetResult.MyListener, OrderPendingDetailsActivity.PenddingFragment {
    @BindView(R.id.txt_itmecount)
    TextView txtItmecount;
    @BindView(R.id.recycle_pending)
    RecyclerView recyclePending;

    @BindView(R.id.fab)
    com.google.android.material.floatingactionbutton.FloatingActionButton floatingActionButton;


    CustPrograssbar custPrograssbar;
    SessionManager sessionManager;
    Store user;
    @BindView(R.id.txtNodata)
    TextView txtNodata;
    List<ProductDataItem> pendinglistMain = new ArrayList<>();
    ProductAdepter myOrderAdepter;

    public ProductFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_product, container, false);
        ButterKnife.bind(this, view);
        OrderPendingDetailsActivity.listener = this;
        custPrograssbar = new CustPrograssbar();
        sessionManager = new SessionManager(getActivity());
        user = sessionManager.getUserDetails("");
        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(getActivity());
        recyclePending.setLayoutManager(recyclerLayoutManager);
        myOrderAdepter = new ProductAdepter(pendinglistMain);
        recyclePending.setAdapter(myOrderAdepter);
        getProduct();
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AddProductActivity.class));
            }
        });
        return view;
    }

    private void getProduct() {
        custPrograssbar.prograssCreate(getActivity());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("sid", user.getId());
            JsonParser jsonParser = new JsonParser();
            Call<JsonObject> call = APIClient.getInterface().getTotalProduct((JsonObject) jsonParser.parse(jsonObject.toString()));
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
                Product product = gson.fromJson(result.toString(), Product.class);
                if (product.getResult().equalsIgnoreCase("true")) {
                    txtItmecount.setText(product.getProductData().size() + " Products");
                    if (product.getProductData().isEmpty()) {
                        txtNodata.setVisibility(View.VISIBLE);
                        recyclePending.setVisibility(View.GONE);
                    } else {
                        pendinglistMain = product.getProductData();
                        myOrderAdepter = new ProductAdepter(pendinglistMain);
                        recyclePending.setAdapter(myOrderAdepter);
                    }
                } else {
                    txtNodata.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClickItem(String s, PendingOrderItem orderItem) {

    }

    public class ProductAdepter extends RecyclerView.Adapter<ProductAdepter.ViewHolder> {
        private List<ProductDataItem> productDataItems;

        public ProductAdepter(List<ProductDataItem> pendinglist) {
            this.productDataItems = pendinglist;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent,
                                             int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.product_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder,
                                     int position) {
            Log.e("position", "" + position);
            ProductDataItem productItem = productDataItems.get(position);
            holder.txtTitle.setText("" + productItem.getProductName());
            holder.txtCategory.setText("" + productItem.getProductCategory());
            if (Double.parseDouble(productItem.getProductInfo().get(0).getProductDiscount()) == 0) {
                holder.lvlOffer.setVisibility(View.GONE);
                holder.txtDscount.setVisibility(View.GONE);
            } else {
                holder.lvlOffer.setVisibility(View.VISIBLE);
                holder.txtDscount.setVisibility(View.VISIBLE);
            }
            double res = (Double.parseDouble(productItem.getProductInfo().get(0).getProductPrice()) / 100.0f) * Double.parseDouble(productItem.getProductInfo().get(0).getProductDiscount());
            res = Double.parseDouble(productItem.getProductInfo().get(0).getProductPrice()) - res;
            holder.txtPrice.setText(sessionManager.getStringData(curruncy) + new DecimalFormat("##.##").format(res));
            holder.txtDscount.setText(sessionManager.getStringData(curruncy) + productItem.getProductInfo().get(0).getProductPrice());
            holder.txtOffer.setText(productItem.getProductInfo().get(0).getProductDiscount() + "%\nOFF");
            holder.txtDscount.setPaintFlags(holder.txtDscount.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            Glide.with(getActivity()).load(baseUrl + productItem.getProductImage().get(0)).placeholder(R.drawable.slider).into(holder.imgIcon);
            holder.rltDetail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getActivity(), ProductActivity.class).putExtra("MyClass", productItem).putParcelableArrayListExtra("MyList", productItem.getProductInfo()));
                }
            });
        }

        @Override
        public int getItemCount() {
            return productDataItems.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.img_icon)
            ImageView imgIcon;
            @BindView(R.id.txt_title)
            TextView txtTitle;
            @BindView(R.id.txt_dscount)
            TextView txtDscount;
            @BindView(R.id.txt_price)
            TextView txtPrice;
            @BindView(R.id.img_delete)
            ImageView imgDelete;
            @BindView(R.id.txt_offer)
            TextView txtOffer;
            @BindView(R.id.txt_ptype)
            TextView txtPtype;
            @BindView(R.id.txt_category)
            TextView txtCategory;
            @BindView(R.id.lvl_offer)
            LinearLayout lvlOffer;
            @BindView(R.id.rlt_detail)
            RelativeLayout rltDetail;

            public ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (updatestatus) {
            updatestatus = false;
            getProduct();
        }
    }
}
