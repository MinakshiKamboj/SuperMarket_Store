package com.supermarket.store.activity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.supermarket.store.R;
import com.supermarket.store.model.ProductDataItem;
import com.supermarket.store.model.ProductInfoItem;
import com.supermarket.store.model.RestResponse;
import com.supermarket.store.retrofit.APIClient;
import com.supermarket.store.retrofit.GetResult;
import com.supermarket.store.utils.CustPrograssbar;
import com.supermarket.store.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;

import static com.supermarket.store.utils.Utiles.updatestatus;

public class ProductActivity extends AppCompatActivity implements GetResult.MyListener {
    @BindView(R.id.fab)
    FloatingActionButton floatingActionButton;
    @BindView(R.id.txt_title)
    TextView txtTitle;
    @BindView(R.id.txt_disc)
    TextView txtDisc;
    @BindView(R.id.viewPager)
    ViewPager viewPager;
    @BindView(R.id.tabview)
    TabLayout tabview;
    ArrayList<ProductInfoItem> productInfoItems;
    ProductDataItem dataItem;
    SessionManager sessionManager;
    CustPrograssbar custPrograssbar;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {// app icon in action bar clicked; go home
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Product Details");
        getSupportActionBar().setElevation(0f);
        custPrograssbar = new CustPrograssbar();
        sessionManager = new SessionManager(ProductActivity.this);
        floatingActionButton.setOnClickListener(view -> bottonAttributes());
        dataItem = (ProductDataItem) getIntent().getParcelableExtra("MyClass");
        productInfoItems = getIntent().getParcelableArrayListExtra("MyList");

        txtTitle.setText("" + dataItem.getProductName());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            txtDisc.setText(Html.fromHtml(dataItem.getShortDesc(), Html.FROM_HTML_MODE_LEGACY));
        } else {
            txtDisc.setText(Html.fromHtml(dataItem.getShortDesc()));
        }
        List<String> myList = new ArrayList<>();
        myList.addAll(dataItem.getProductImage());
        MyCustomPagerAdapter myCustomPagerAdapter = new MyCustomPagerAdapter(this, myList);
        viewPager.setAdapter(myCustomPagerAdapter);
        tabview.setupWithViewPager(viewPager, true);

    }

    public void bottonAttributes() {
        BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.producttitle_layout, null);
        mBottomSheetDialog.setContentView(sheetView);
        LinearLayout linearLayout = sheetView.findViewById(R.id.lvl_data);
        mBottomSheetDialog.show();
        linearLayout.removeAllViews();
        for (int i = 0; i < productInfoItems.size(); i++) {
            ProductInfoItem listdatum = productInfoItems.get(i);
            LayoutInflater inflater = LayoutInflater.from(ProductActivity.this);

            View view = inflater.inflate(R.layout.product_attributs_item, null);
            EditText txtPtype = view.findViewById(R.id.txt_ptype);
            EditText txtPrice = view.findViewById(R.id.txt_price);
            EditText txtDiscount = view.findViewById(R.id.txt_discount);
            Switch aswitch = view.findViewById(R.id.switch1);
            Button btnSave = view.findViewById(R.id.btnsave);
            aswitch.setChecked(!listdatum.getProductOutStock().equalsIgnoreCase("1"));
            txtPtype.setText("" + listdatum.getProductType());
            txtPrice.setText(listdatum.getProductPrice());
            txtDiscount.setText("" + listdatum.getProductDiscount());
            aswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        changeStock(listdatum.getAttributeId(), "0");
                    } else {
                        changeStock(listdatum.getAttributeId(), "1");
                    }
                    mBottomSheetDialog.cancel();
                }
            });
            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (TextUtils.isEmpty(txtPtype.getText().toString())) {
                        txtPtype.setError("Enter Product Type");
                        return;
                    }

                    if (TextUtils.isEmpty(txtPrice.getText().toString())) {
                        txtPrice.setError("Enter Product Price");
                        return;
                    }
                    if (TextUtils.isEmpty(txtDiscount.getText().toString())) {
                        txtDiscount.setError("Enter Product Discount");
                        return;
                    }

                    if (aswitch.isChecked()) {
                        updateattribut(listdatum.getAttributeId(), "0", txtPrice.getText().toString(), txtPtype.getText().toString(), txtDiscount.getText().toString());
                    } else {
                        updateattribut(listdatum.getAttributeId(), "1", txtPrice.getText().toString(), txtPtype.getText().toString(), txtDiscount.getText().toString());

                    }

                }
            });

            linearLayout.addView(view);
        }

    }

    private void productstatus(String id, String status) {
        custPrograssbar.prograssCreate(this);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("status", status);
            JsonParser jsonParser = new JsonParser();
            Call<JsonObject> call = APIClient.getInterface().ProductStatus((JsonObject) jsonParser.parse(jsonObject.toString()));
            GetResult getResult = new GetResult();
            getResult.setMyListener(this);
            getResult.callForLogin(call, "1");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void changeStock(String id, String status) {
        custPrograssbar.prograssCreate(this);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("status", status);
            JsonParser jsonParser = new JsonParser();
            Call<JsonObject> call = APIClient.getInterface().changestock((JsonObject) jsonParser.parse(jsonObject.toString()));
            GetResult getResult = new GetResult();
            getResult.setMyListener(this);
            getResult.callForLogin(call, "1");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateattribut(String id, String status, String price, String type, String discount) {
        custPrograssbar.prograssCreate(this);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("status", status);
            jsonObject.put("price", price);
            jsonObject.put("type", type);
            jsonObject.put("discount", discount);
            JsonParser jsonParser = new JsonParser();
            Call<JsonObject> call = APIClient.getInterface().changestock((JsonObject) jsonParser.parse(jsonObject.toString()));
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
                RestResponse response = gson.fromJson(result.toString(), RestResponse.class);

                if (response.getResult().equalsIgnoreCase("true")) {
                    Toast.makeText(ProductActivity.this, response.getResponseMsg(), Toast.LENGTH_LONG).show();
                    updatestatus = true;
                    finish();
                }
            }
        } catch (Exception e) {
            e.toString();
        }
    }

    public class MyCustomPagerAdapter extends PagerAdapter {
        Context context;
        List<String> imageList;
        LayoutInflater layoutInflater;

        public MyCustomPagerAdapter(Context context, List<String> bannerDatumList) {
            this.context = context;
            this.imageList = bannerDatumList;
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        @Override
        public int getCount() {
            return imageList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((LinearLayout) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View itemView = layoutInflater.inflate(R.layout.item_image, container, false);
            ImageView imageView = (ImageView) itemView.findViewById(R.id.imageView);
            Glide.with(ProductActivity.this).load(APIClient.baseUrl + "/" + imageList.get(position)).placeholder(R.drawable.slider).into(imageView);
            container.addView(itemView);

            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((LinearLayout) object);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.product, menu);
        MenuItem item = menu.findItem(R.id.myswitch);
        item.setActionView(R.layout.switch_layout);
        Switch mySwitch = item.getActionView().findViewById(R.id.switchForActionBar);
        mySwitch.setChecked(dataItem.getProductStatus() != 0);
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something based on isChecked
                int a = 0;
                if (isChecked) {
                    a = 1;
                } else {
                    a = 0;
                }
                productstatus(dataItem.getId(), String.valueOf(a));
            }
        });

        return true;
    }
}