package com.supermarket.store.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.supermarket.store.R;
import com.supermarket.store.model.PendingOrderItem;
import com.supermarket.store.model.Productinfo;
import com.supermarket.store.model.Store;
import com.supermarket.store.utils.CustPrograssbar;
import com.supermarket.store.utils.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.DecimalFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.supermarket.store.retrofit.APIClient.baseUrl;
import static com.supermarket.store.utils.SessionManager.curruncy;

public class OrderDeliverDetailsActivity extends AppCompatActivity {

    @BindView(R.id.txt_orderid)
    TextView txtOrderid;
    @BindView(R.id.txt_total)
    TextView txtTotal;

    @BindView(R.id.txt_qty)
    TextView txtQty;
    @BindView(R.id.txt_address)
    TextView txtAddress;
    @BindView(R.id.txt_email)
    TextView txtEmail;
    @BindView(R.id.txt_paymode)
    TextView txtPaymode;
    @BindView(R.id.txt_name)
    TextView txtName;


    @BindView(R.id.img_sing)
    ImageView imgSing;


    ArrayList<Productinfo> productinfoArrayList;
    PendingOrderItem order;
    SessionManager sessionManager;

    CustPrograssbar custPrograssbar;
    Store user;


    @BindView(R.id.img_more)
    ImageView imgMore;
    @BindView(R.id.txt_mobile)
    TextView txtMobile;


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_deliver_details);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Order Delivered Details");
        getSupportActionBar().setElevation(0f);
        sessionManager = new SessionManager(this);
        user = sessionManager.getUserDetails("");
        custPrograssbar = new CustPrograssbar();
        order = (PendingOrderItem) getIntent().getParcelableExtra("MyClass");
        productinfoArrayList = getIntent().getParcelableArrayListExtra("MyList");

        txtOrderid.setText("" + order.getOrderid());
        txtTotal.setText(sessionManager.getStringData(curruncy) + " " + order.getTotal());
        txtOrderid.setText("" + order.getOrderid());
        txtQty.setText("" + productinfoArrayList.size());

        txtName.setText("" + order.getName());
        txtAddress.setText("" + order.getDelivery());
        txtEmail.setText("" + order.getEmail());
        txtMobile.setText("" + order.getMobile());


        txtPaymode.setText("" + order.getPMethod());
        byte[] decodedString;

        if (order.getSign() != null) {
            decodedString = Base64.decode(order.getSign(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            imgSing.setImageBitmap(decodedByte);
        }

    }


    public void bottonProductlistr() {
        BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.orderlist_layout, null);
        mBottomSheetDialog.setContentView(sheetView);
        LinearLayout linearLayout = sheetView.findViewById(R.id.lvl_data);
        mBottomSheetDialog.show();
        linearLayout.removeAllViews();
        for (int i = 0; i < productinfoArrayList.size(); i++) {
            Productinfo listdatum = productinfoArrayList.get(i);
            LayoutInflater inflater = LayoutInflater.from(OrderDeliverDetailsActivity.this);

            View view = inflater.inflate(R.layout.pending_order_item, null);
            ImageView imgView = view.findViewById(R.id.imageView);
            TextView txtTitle = view.findViewById(R.id.txt_title);
            TextView txtItems = view.findViewById(R.id.txt_items);
            TextView txtPrice = view.findViewById(R.id.txt_price);
            TextView txtPricedis = view.findViewById(R.id.txt_pricedis);
            Glide.with(OrderDeliverDetailsActivity.this).load(baseUrl + listdatum.getProductImage()).placeholder(R.drawable.slider).into(imgView);
            txtTitle.setText("" + listdatum.getProductName());
            txtItems.setText(" X " + listdatum.getProductQty() + " Items");
            if (Double.parseDouble(listdatum.getDiscount()) == 0) {
                txtPricedis.setVisibility(View.GONE);
                txtPrice.setText(sessionManager.getStringData(curruncy) + listdatum.getProductPrice());
            } else {
                double res = (Double.parseDouble(listdatum.getProductPrice()) / 100.0f) * Double.parseDouble(listdatum.getDiscount());
                res = Double.parseDouble(listdatum.getProductPrice()) - res;
                txtPrice.setText(sessionManager.getStringData(curruncy) + new DecimalFormat("##.##").format(res));
                txtPricedis.setPaintFlags(txtPricedis.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                txtPricedis.setText(sessionManager.getStringData(curruncy) + listdatum.getProductPrice());
            }

            linearLayout.addView(view);
        }


    }

    @OnClick(R.id.img_more)
    public void onClick() {
        bottonProductlistr();
    }
}
