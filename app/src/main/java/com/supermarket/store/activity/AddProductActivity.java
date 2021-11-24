package com.supermarket.store.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.supermarket.store.R;
import com.supermarket.store.imagepicker.ImageCompressionListener;
import com.supermarket.store.imagepicker.ImagePicker;
import com.supermarket.store.model.CatlistItem;
import com.supermarket.store.model.PincodelistItem;
import com.supermarket.store.model.ResponseCP;
import com.supermarket.store.model.RestResponse;
import com.supermarket.store.model.Store;
import com.supermarket.store.retrofit.APIClient;
import com.supermarket.store.retrofit.GetResult;
import com.supermarket.store.utils.CustPrograssbar;
import com.supermarket.store.utils.FileUtils;
import com.supermarket.store.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.supermarket.store.utils.FileUtils.isLocal;
import static com.supermarket.store.utils.Utiles.updatestatus;

public class AddProductActivity extends AppCompatActivity implements GetResult.MyListener {
    @BindView(R.id.spinner_category)
    Spinner spinnerCategory;

    @BindView(R.id.spinner_status)
    Spinner spinnerStatus;

    @BindView(R.id.spinner_pincode)
    Spinner spinnerPincode;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.ed_producttitle)
    TextInputEditText edProducttitle;
    @BindView(R.id.ed_productdesc)
    TextInputEditText edProductdesc;

    @BindView(R.id.ed_price)
    EditText edPrice;

    @BindView(R.id.ed_type)
    EditText edType;

    @BindView(R.id.ed_discount)
    EditText edDiscount;

    @BindView(R.id.lvl_atrribut)
    LinearLayout lvlAtrribut;


    private ImagePicker imagePicker;
    ArrayList<String> arrayListImage = new ArrayList<>();
    CustPrograssbar custPrograssbar;
    SessionManager sessionManager;
    Store user;
    String selectCategory;
    String selectPincode;
    String selectStatus;
    List<CatlistItem> catlist = new ArrayList<>();
    List<PincodelistItem> pincodelist = new ArrayList<>();

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
        setContentView(R.layout.activity_add_product);
        ButterKnife.bind(this);
        custPrograssbar = new CustPrograssbar();
        sessionManager = new SessionManager(this);
        user = sessionManager.getUserDetails("");
        requestStoragePermission();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add Product");
        getSupportActionBar().setElevation(0f);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        LinearLayoutManager mLayoutManager1 = new LinearLayoutManager(this);
        mLayoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        imagePicker = new ImagePicker();
        List<String> statusList = new ArrayList<>();
        statusList.add("Publish");
        statusList.add("Unpublish");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(dataAdapter);
        getRequiredlist();


        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectCategory = catlist.get(position).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerPincode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectPincode = pincodelist.get(position).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectStatus = "1";
                } else {
                    selectStatus = "0";
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }


    private void getRequiredlist() {
        custPrograssbar.prograssCreate(this);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("sid", user.getId());
            JsonParser jsonParser = new JsonParser();
            Call<JsonObject> call = APIClient.getInterface().getRequiredlist((JsonObject) jsonParser.parse(jsonObject.toString()));
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
                ResponseCP responseCP = gson.fromJson(result.toString(), ResponseCP.class);
                if (responseCP.getResult().equalsIgnoreCase("true")) {
                    List<String> categoryList = new ArrayList<>();
                    catlist = responseCP.getResultData().getCatlist();
                    for (int i = 0; i < responseCP.getResultData().getCatlist().size(); i++) {
                        categoryList.add(responseCP.getResultData().getCatlist().get(i).getCatname());
                    }
                    List<String> pincodeList = new ArrayList<>();
                    pincodelist = responseCP.getResultData().getPincodelist();
                    for (int i = 0; i < responseCP.getResultData().getPincodelist().size(); i++) {
                        pincodeList.add(responseCP.getResultData().getPincodelist().get(i).getPincode());
                    }

                    ArrayAdapter<String> dataAdapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
                    dataAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategory.setAdapter(dataAdapter1);

                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, pincodeList);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerPincode.setAdapter(dataAdapter);
                }
            }
        } catch (Exception e) {
            e.toString();
        }
    }

    public void postImage(ArrayList<String> urilist) {
        ImageAdp imageAdp = new ImageAdp( urilist);
        recyclerView.setAdapter(imageAdp);

    }

    public class ImageAdp extends RecyclerView.Adapter<ImageAdp.MyViewHolder> {
        private ArrayList<String> arrayList;


        public class MyViewHolder extends RecyclerView.ViewHolder {

            public ImageView remove;
            public ImageView thumbnail;

            public MyViewHolder(View view) {
                super(view);

                thumbnail = view.findViewById(R.id.image_pic);
                remove = view.findViewById(R.id.image_remove);
            }
        }

        public ImageAdp(ArrayList<String> arrayList) {
            this.arrayList = arrayList;

        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.imageview_layout, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {


            Glide.with(AddProductActivity.this)
                    .load(arrayList.get(position))
                    .into(holder.thumbnail);
            holder.remove.setOnClickListener(v -> {

                if (!arrayList.isEmpty()) {
                    arrayList.remove(position);
                    notifyDataSetChanged();
                }

            });
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }
    }

    @OnClick({R.id.btn_browese, R.id.txt_login, R.id.txt_add})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_browese:
                imagePicker.withActivity(AddProductActivity.this).chooseFromGallery(true).chooseFromCamera(false).withCompression(true).start();
                break;
            case R.id.txt_login:

                if (velidation() && !arrayListImage.isEmpty()) {
                    uploadMultiFile(arrayListImage);
                }
                break;
            case R.id.txt_add:
                if (velidationAttribut()) {
                    Model model = new Model();
                    model.setPrice("" + edPrice.getText().toString());
                    model.setType("" + edType.getText().toString());
                    model.setDiscount("" + edDiscount.getText().toString());
                    modelList.add(model);
                    edType.setText("");
                    edPrice.setText("");
                    edDiscount.setText("");
                    bottonProductlistr(modelList);
                }
                break;
            default:
                break;
        }
    }

    List<Model> modelList = new ArrayList<>();

    public void bottonProductlistr(List<Model> list) {

        lvlAtrribut.removeAllViews();
        for (int i = 0; i < list.size(); i++) {
            Model listdatum = list.get(i);
            LayoutInflater inflater = LayoutInflater.from(AddProductActivity.this);
            View view = inflater.inflate(R.layout.temp_item, null);
            TextView txtPrice = view.findViewById(R.id.txt_price);
            TextView txtType = view.findViewById(R.id.txt_type);
            TextView txtDisc = view.findViewById(R.id.txt_disc);
            ImageView imgRemove = view.findViewById(R.id.img_remove);
            txtPrice.setText("" + listdatum.getPrice());
            txtType.setText("" + listdatum.getType());
            txtDisc.setText("" + listdatum.getDiscount());
            int finalI = i;
            imgRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    modelList.remove(finalI);
                    bottonProductlistr(modelList);
                }
            });
            lvlAtrribut.addView(view);
        }

    }


    private void uploadMultiFile(ArrayList<String> filePaths) {
        custPrograssbar.prograssCreate(AddProductActivity.this);
        List<MultipartBody.Part> parts = new ArrayList<>();

        if (filePaths != null) {
            // create part for file (photo, video, ...)
            for (int i = 0; i < filePaths.size(); i++) {
                parts.add(prepareFilePart("image" + i, filePaths.get(i)));
            }
        }

        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < modelList.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("price", modelList.get(i).getPrice());
                jsonObject.put("type", modelList.get(i).getType());
                jsonObject.put("discount", modelList.get(i).getDiscount());
                jsonArray.put(i, jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.e("Json", "-->" + jsonArray.toString());

// create a map of data to pass along
        RequestBody sid = createPartFromString(user.getId());
        RequestBody cid = createPartFromString(selectCategory);
        RequestBody pid = createPartFromString(selectPincode);
        RequestBody status = createPartFromString(selectStatus);
        RequestBody productData = createPartFromString(jsonArray.toString());
        RequestBody title = createPartFromString(edProducttitle.getText().toString());
        RequestBody description = createPartFromString("" + edProductdesc.getText().toString());
        RequestBody size = createPartFromString("" + parts.size());

// finally, execute the request
        Call<JsonObject> call = APIClient.getInterface().addProduct(sid, cid, pid, status, productData, title, description, size, parts);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                custPrograssbar.closePrograssBar();
                Gson gson = new Gson();
                RestResponse restResponse = gson.fromJson(response.body(), RestResponse.class);
                Toast.makeText(AddProductActivity.this, restResponse.getResponseMsg(), Toast.LENGTH_SHORT).show();
                if (restResponse.getResult().equalsIgnoreCase("true")) {
                    updatestatus = true;
                    arrayListImage.clear();
                    finish();
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                custPrograssbar.closePrograssBar();

            }
        });
    }

    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(MediaType.parse(FileUtils.MIME_TYPE_TEXT), descriptionString);
    }

    @NonNull
    private MultipartBody.Part prepareFilePart(String partName, String fileUri) {
        // use the FileUtils to get the actual file by uri
        File file = getFile(fileUri);

        // create RequestBody instance from file
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    public static File getFile(String path) {
        if (path == null) {
            return null;
        }

        if (isLocal(path)) {
            return new File(path);
        }
        return null;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1234);
    }

    public boolean velidation() {
        if (TextUtils.isEmpty(edProducttitle.getText().toString())) {
            edProducttitle.setError("Enter Product Title");
            return false;
        }

        if (TextUtils.isEmpty(edProductdesc.getText().toString())) {
            edProductdesc.setError("Enter Product Description");
            return false;
        }
        return true;
    }

    public boolean velidationAttribut() {
        if (TextUtils.isEmpty(edPrice.getText().toString())) {
            edPrice.setError("Enter Product Price");
            return false;
        }

        if (TextUtils.isEmpty(edType.getText().toString())) {
            edType.setError("Enter Product Type");
            return false;
        }
        if (TextUtils.isEmpty(edDiscount.getText().toString())) {
            edDiscount.setError("Enter Product Discount");
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1234) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.e("OOOn", "Done");
            } else {
                setResult(RESULT_CANCELED);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImagePicker.SELECT_IMAGE && resultCode == RESULT_OK) {

            imagePicker.addOnCompressListener(new ImageCompressionListener() {
                @Override
                public void onStart() {
                }

                @Override
                public void onCompressed(String filePath) {
                    if (filePath != null) {
                        //return filepath
                        if (arrayListImage.size() <= 2) {
                            arrayListImage.add(filePath);
                            postImage(arrayListImage);
                        }
                    }
                }
            });
            String filePath = imagePicker.getImageFilePath(data);
            if (filePath != null) {
                //return filepath
                if (arrayListImage.size() <= 2) {
                    arrayListImage.add(filePath);
                    postImage(arrayListImage);
                }
            }

        }
    }

    public class Model {
        String price;
        String type;
        String discount;

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDiscount() {
            return discount;
        }

        public void setDiscount(String discount) {
            this.discount = discount;
        }
    }

}