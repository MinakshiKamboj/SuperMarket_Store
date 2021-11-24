package com.supermarket.store.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.supermarket.store.R;
import com.supermarket.store.model.OrderDetails;
import com.supermarket.store.model.OrderProductDataItem;
import com.supermarket.store.model.PendingOrderItem;
import com.supermarket.store.model.RestResponse;
import com.supermarket.store.model.Rider;
import com.supermarket.store.model.RiderDataItem;
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
import butterknife.OnClick;
import retrofit2.Call;

import static com.supermarket.store.retrofit.APIClient.baseUrl;
import static com.supermarket.store.utils.SessionManager.curruncy;

public class OrderPendingDetailsActivity extends AppCompatActivity implements GetResult.MyListener {

    @BindView(R.id.txt_orderid)
    TextView txtOrderid;

    @BindView(R.id.img_more)
    ImageView imgMore;
    @BindView(R.id.txt_total)
    TextView txtTotal;

    @BindView(R.id.txt_qty)
    TextView txtQty;

    @BindView(R.id.txt_stotal)
    TextView txtStotal;

    @BindView(R.id.txt_deliverycharge)
    TextView txtDeliverycharge;


    @BindView(R.id.txt_address)
    TextView txtAddress;

    @BindView(R.id.txt_email)
    TextView txtEmail;
    @BindView(R.id.txt_paymode)
    TextView txtPaymode;
    @BindView(R.id.txt_name)
    TextView txtName;


    @BindView(R.id.lvl_makedecision)
    LinearLayout lvlMakedecision;

    @BindView(R.id.lvl_assing)
    LinearLayout lvlAssing;

    @BindView(R.id.txt_mobile)
    TextView txtMobile;

    @BindView(R.id.txt_note)
    TextView txtNote;

    @BindView(R.id.crd_additional)
    CardView crdAdditional;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                finish();
                return true;

            case R.id.navigation_call:
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + txtMobile.getText().toString()));
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 1);
                }
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    List<OrderProductDataItem> productinfoArrayList;
    SessionManager sessionManager;

    CustPrograssbar custPrograssbar;
    Store store;
    String oID;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);
        ButterKnife.bind(this);
        sessionManager = new SessionManager(this);
        oID = getIntent().getStringExtra("OrderID");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Order Details");
        getSupportActionBar().setElevation(0f);
        requestPermissions(new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        store = sessionManager.getUserDetails("");
        custPrograssbar = new CustPrograssbar();
        getOrderItem();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.order, menu);
        return true;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @OnClick({R.id.txt_assing, R.id.img_more, R.id.txt_mobile, R.id.txt_makedecision})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.txt_assing:
                bottonAssingDeliveryboy();
                break;
            case R.id.txt_makedecision:
                bottonOrderMakeDecision();
                break;
            case R.id.img_more:
                bottonProductlistr(productinfoArrayList);
                break;
            case R.id.txt_mobile:
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + txtMobile.getText().toString()));
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 1);
                }
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private void getRiderID() {
        custPrograssbar.prograssCreate(this);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("aid", store.getAid());
            JsonParser jsonParser = new JsonParser();
            Call<JsonObject> call = APIClient.getInterface().getRiderlist((JsonObject) jsonParser.parse(jsonObject.toString()));
            GetResult getResult = new GetResult();
            getResult.setMyListener(this);
            getResult.callForLogin(call, "3");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getOrderItem() {
        custPrograssbar.prograssCreate(this);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("order_id", oID);
            jsonObject.put("sid", store.getId());
            JsonParser jsonParser = new JsonParser();
            Call<JsonObject> call = APIClient.getInterface().getOrderDetail((JsonObject) jsonParser.parse(jsonObject.toString()));
            GetResult getResult = new GetResult();
            getResult.setMyListener(this);
            getResult.callForLogin(call, "1");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void getMackDecision(String status, String comment) {
        custPrograssbar.prograssCreate(this);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("oid", oID);
            jsonObject.put("comment", comment);
            jsonObject.put("status", status);
            JsonParser jsonParser = new JsonParser();

            Call<JsonObject> call = APIClient.getInterface().getMackDecision((JsonObject) jsonParser.parse(jsonObject.toString()));
            GetResult getResult = new GetResult();
            getResult.setMyListener(this);
            getResult.callForLogin(call, "2");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void oderAssing(String rid) {
        custPrograssbar.prograssCreate(this);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("oid", oID);
            jsonObject.put("rid", rid);
            JsonParser jsonParser = new JsonParser();

            Call<JsonObject> call = APIClient.getInterface().getAssrider((JsonObject) jsonParser.parse(jsonObject.toString()));
            GetResult getResult = new GetResult();
            getResult.setMyListener(this);
            getResult.callForLogin(call, "4");
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
                OrderDetails response = gson.fromJson(result, OrderDetails.class);
                if (response.getResult().equalsIgnoreCase("true")) {
                    txtPaymode.setText("" + response.getOrderProductList().get(0).getPMethodName());
                    txtOrderid.setText("" + response.getOrderProductList().get(0).getOrderId());
                    txtQty.setText("" + response.getOrderProductList().get(0).getOrderProductData().size());
                    txtStotal.setText("" + response.getOrderProductList().get(0).getOrderSubTotal());
                    txtDeliverycharge.setText("" + response.getOrderProductList().get(0).getDeliveryCharge());
                    txtTotal.setText("" + response.getOrderProductList().get(0).getOrderTotal());
                    txtAddress.setText("" + response.getOrderProductList().get(0).getCustomerAddress());
                    txtMobile.setText("" + response.getOrderProductList().get(0).getCustomerMobile());
                    txtEmail.setText("" + response.getOrderProductList().get(0).getCustomerEmail());
                    txtName.setText("" + response.getOrderProductList().get(0).getCustomerName());
                    productinfoArrayList = response.getOrderProductList().get(0).getOrderProductData();
                    if (response.getOrderProductList().get(0).getOrderFlowId().equalsIgnoreCase("1") || response.getOrderProductList().get(0).getOrderFlowId().equalsIgnoreCase("5")) {
                        lvlAssing.setVisibility(View.VISIBLE);
                    } else if (response.getOrderProductList().get(0).getOrderFlowId().equalsIgnoreCase("0")) {
                        lvlMakedecision.setVisibility(View.VISIBLE);
                    }
                    if (response.getOrderProductList().get(0).getAdditionalNote().isEmpty()) {
                        crdAdditional.setVisibility(View.GONE);
                    } else {
                        crdAdditional.setVisibility(View.VISIBLE);
                        txtNote.setText("" + response.getOrderProductList().get(0).getAdditionalNote());
                    }
                    getRiderID();
                }
            } else if (callNo.equalsIgnoreCase("2")) {
                Gson gson = new Gson();
                RestResponse response = gson.fromJson(result, RestResponse.class);
                Toast.makeText(this, response.getResponseMsg(), Toast.LENGTH_SHORT).show();
                if (response.getResult().equalsIgnoreCase("true")) {
                    lvlAssing.setVisibility(View.VISIBLE);
                    lvlMakedecision.setVisibility(View.GONE);
                    finish();
                } else {
                    finish();
                }
            } else if (callNo.equalsIgnoreCase("3")) {
                Gson gson = new Gson();
                Rider rider = gson.fromJson(result.toString(), Rider.class);
                arrayList = rider.getRiderData();

            } else if (callNo.equalsIgnoreCase("4")) {
                lvlAssing.setVisibility(View.GONE);
                lvlMakedecision.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static PenddingFragment listener;


    public interface PenddingFragment {
        public void onClickItem(String s, PendingOrderItem orderItem);

    }

    public void bottonProductlistr(List<OrderProductDataItem> productinfoList) {
        BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.orderlist_layout, null);
        mBottomSheetDialog.setContentView(sheetView);
        LinearLayout linearLayout = sheetView.findViewById(R.id.lvl_data);
        mBottomSheetDialog.show();
        linearLayout.removeAllViews();
        for (int i = 0; i < productinfoList.size(); i++) {
            OrderProductDataItem listdatum = productinfoList.get(i);
            LayoutInflater inflater = LayoutInflater.from(OrderPendingDetailsActivity.this);
            View view = inflater.inflate(R.layout.pending_order_item, null);
            ImageView imgView = view.findViewById(R.id.imageView);
            TextView txtType = view.findViewById(R.id.txt_type);
            TextView txtTitle = view.findViewById(R.id.txt_title);
            TextView txtItems = view.findViewById(R.id.txt_items);
            TextView txtPrice = view.findViewById(R.id.txt_price);
            TextView txtPricedis = view.findViewById(R.id.txt_pricedis);
            Glide.with(OrderPendingDetailsActivity.this).load(baseUrl + listdatum.getProductImage()).placeholder(R.drawable.slider).into(imgView);
            txtTitle.setText("" + listdatum.getProductName());
            txtItems.setText(" X " + listdatum.getProductQuantity() + " Items");
            txtType.setText("" + listdatum.getProductVariation());
            if (Double.parseDouble(listdatum.getProductDiscount()) == 0) {
                txtPricedis.setVisibility(View.GONE);
                txtPrice.setText(sessionManager.getStringData(curruncy) + listdatum.getProductPrice());
            } else {
                double res = (Double.parseDouble(listdatum.getProductPrice()) / 100.0f) * Double.parseDouble(listdatum.getProductDiscount());
                res = Double.parseDouble(listdatum.getProductPrice()) - res;
                txtPrice.setText(sessionManager.getStringData(curruncy) + new DecimalFormat("##.##").format(res));
                txtPricedis.setPaintFlags(txtPricedis.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                txtPricedis.setText(sessionManager.getStringData(curruncy) + listdatum.getProductPrice());

            }
            linearLayout.addView(view);
        }

    }

    public void bottonOrderMakeDecision() {
        BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.order_makedecision, null);
        mBottomSheetDialog.setContentView(sheetView);
        EditText edComment = sheetView.findViewById(R.id.ed_comment);
        TextView txtAccept = sheetView.findViewById(R.id.txt_accept);
        TextView txtReject = sheetView.findViewById(R.id.txt_reject);
        txtAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.cancel();
                getMackDecision("1", edComment.getText().toString());
            }
        });
        txtReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.cancel();
                getMackDecision("2", edComment.getText().toString());
            }
        });
        mBottomSheetDialog.show();
    }

    List<RiderDataItem> arrayList = new ArrayList<>();

    public void bottonAssingDeliveryboy() {
        final int[] position = {0};
        List<String> templist = new ArrayList<>();
        for (int i = 0; i < arrayList.size(); i++) {
            templist.add(arrayList.get(i).getName());
        }
        BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.order_assingdeliveryboy, null);
        mBottomSheetDialog.setContentView(sheetView);
        TextView txtAssing = sheetView.findViewById(R.id.txt_assing);
        Spinner spinner = sheetView.findViewById(R.id.spinner);
        txtAssing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.cancel();
                oderAssing(arrayList.get(position[0]).getId());
            }
        });
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, templist);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                position[0] = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        mBottomSheetDialog.show();
    }
}
