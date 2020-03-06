package com.example.mapaps.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.example.mapaps.R;
import com.example.mapaps.adapter.Common_Data;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.amap.api.services.core.AMapException.CODE_AMAP_SUCCESS;

public class SelectActivity extends AppCompatActivity implements TextWatcher, Inputtips.InputtipsListener,PoiSearch.OnPoiSearchListener, GeocodeSearch.OnGeocodeSearchListener, Common_Data {
    //POI参数
    private PoiSearch.Query query;
    private ArrayList<PoiItem> poiItems;
    private LatLonPoint user_loc=null;
    private LatLonPoint goal_loc=null;
    private String city_code=null;
    private InputtipsQuery inputtipsQuery;
    private Intent loc_intent=null;
    private GeocodeSearch geocodeSearch;
    private boolean start_flag=false;
    private boolean input_flag=true;

    //顶部搜索框
    private EditText startText=null;
    private ImageView start_delete_btn=null;
    private EditText inputText=null;
    private ImageView delete_btn=null;
    private Button start_btn=null;
    private RecyclerView POI_list=null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        Intent intent=getIntent();
        user_loc=new LatLonPoint(intent.getDoubleExtra("slat",0),intent.getDoubleExtra("slon",0));
        city_code=intent.getStringExtra("city_code");

        poiItems=new ArrayList<PoiItem>();

        geocodeSearch=new GeocodeSearch(this);
        geocodeSearch.setOnGeocodeSearchListener(this);

        POI_list=findViewById(R.id.search_list_view);
        POI_list.setLayoutManager(new LinearLayoutManager(this));

        startText=findViewById(R.id.start_edit_text);
        startText.addTextChangedListener(this);
        startText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(v.getId()==startText.getId()) {
                    Toast.makeText(SelectActivity.this,"set start",Toast.LENGTH_SHORT).show();
                    start_flag=true;
                    input_flag=false;
                }
            }
        });

        start_delete_btn=findViewById(R.id.start_edit_delete);
        start_delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startText.setText("");
                start_delete_btn.setVisibility(View.GONE);
            }
        });

        inputText=findViewById(R.id.search_edit_text);
        inputText.addTextChangedListener(this);
        inputText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(v.getId()==inputText.getId()) {
                    Toast.makeText(SelectActivity.this,"set input",Toast.LENGTH_SHORT).show();
                    input_flag=true;
                    start_flag=false;
                }
            }
        });

        delete_btn=findViewById(R.id.search_edit_delete);
        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputText.setText("");
                delete_btn.setVisibility(View.GONE);
            }
        });

        start_btn=(Button)findViewById(R.id.start_btn);
        start_btn.setClickable(false);
        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(loc_intent!=null) {
                    startActivity(loc_intent);
                    finish();
                }
                else {
                    Toast.makeText(SelectActivity.this,"请输入目的地",Toast.LENGTH_SHORT).show();
                }
            }
        });

        //设置定位权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            }
        }

    }

    //开始进行poi搜索
    protected void doSearchQuery(String key) {
        // 当前页面，从0开始计数
        int currentPage = 0;
        //不输入城市名称有些地方搜索不到
        // 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        String POI_SEARCH_TYPE = "汽车服务|汽车销售|" +
                "//汽车维修|摩托车服务|餐饮服务|购物服务|生活服务|体育休闲服务|医疗保健服务|" +
                "//住宿服务|风景名胜|商务住宅|政府机构及社会团体|科教文化服务|交通设施服务|" +
                "//金融保险服务|公司企业|道路附属设施|地名地址信息|公共设施";
        query = new PoiSearch.Query(key, POI_SEARCH_TYPE, "");
        // 设置每页最多返回多少条poiitem
        query.setPageSize(10);
        // 设置查询页码
        query.setPageNum(currentPage);
        //构造 PoiSearch 对象，并设置监听
        PoiSearch poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(this);
        //调用 PoiSearch 的 searchPOIAsyn() 方法发送请求。
        poiSearch.searchPOIAsyn();
    }

    //开始列举推荐列表
    protected void doInputTipsQuery(String key,String code){
        inputtipsQuery=new InputtipsQuery(key,code);
        //是否限制在当前城市
        if(!code.isEmpty()){
            inputtipsQuery.setCityLimit(true);
        }
        else {
            inputtipsQuery.setCityLimit(false);
        }
        //初始化监听器
        Inputtips inputtips=new Inputtips(SelectActivity.this,inputtipsQuery);
        inputtips.setInputtipsListener(this);

        inputtips.requestInputtipsAsyn();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String keyWord = String.valueOf(s);
        boolean start_flag=startText.getText().length()>0;
        boolean search_flag=inputText.getText().length()>0;
        if(!"".equals(keyWord)){
            if(start_flag) {
                start_delete_btn.setVisibility(View.VISIBLE);
            }
            else if(search_flag) {
                delete_btn.setVisibility(View.VISIBLE);
            }
            if(search_flag){
                start_btn.setClickable(true);
                start_btn.setFocusable(true);
            }
            doInputTipsQuery(keyWord,this.city_code);
        }
        else {
            if(!start_flag) {
                start_delete_btn.setVisibility(View.GONE);
            }
            else if(!search_flag) {
                delete_btn.setVisibility(View.GONE);
            }
            if(!search_flag){
                start_btn.setClickable(false);
                start_btn.setFocusable(false);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        //处理搜索结果
        if (i == CODE_AMAP_SUCCESS) {
            if (poiResult != null && poiResult.getQuery() != null) {// 搜索poi的结果
                Log.e("test","搜索的code为====" + i + ", result数量==" + poiResult.getPois().size());
                if (poiResult.getQuery().equals(query)) {// 是否是同一次搜索
                    // poi返回的结果
                    Log.e("test","搜索的code为===="+i+", result数量=="+ poiResult.getPois().size());
                    // 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息
                    List<SuggestionCity> suggestionCities = poiResult.getSearchSuggestionCitys();
                    //如果搜索关键字明显为误输入，则可通过result.getSearchSuggestionKeywords()方法得到搜索关键词建议。
                    List<String> suggestionKeywords =  poiResult.getSearchSuggestionKeywords();
                    //清理list
                    if (poiItems != null && poiItems.size() > 0) {
                        poiItems.clear();
                    }
                    // 取得第一页的poiitem数据，页数从数字0开始
                    poiItems = poiResult.getPois();

                    //解析获取到的PoiItem列表
                    POI_list.setAdapter(new CommonAdapter<PoiItem>(this,R.layout.search_list_item,poiItems) {
                        @Override
                        protected void convert(ViewHolder holder, PoiItem poiItem, int position) {
                            //获取经纬度对象
                            LatLonPoint llp = poiItem.getLatLonPoint();
                            final double lon = llp.getLongitude();
                            final double lat = llp.getLatitude();
                            //返回POI的名称
                            final String title = poiItem.getTitle();
                            //返回POI的地址
                            final String text = poiItem.getSnippet();
                            Log.e("test1","地点："+title+"\n地名："+text+"\n坐标：（"+lon+","+lat+"）");
                            holder.setText(R.id.textView,"地点："+title+"\n地名："+text);
                            holder.setIsRecyclable(true);
                            holder.setOnClickListener(R.id.textView, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Bundle bundle=new Bundle();
                                    bundle.putString("name",title);
                                    bundle.putString("detail",text);
                                    bundle.putString("city_code",city_code);
                                    bundle.putDouble("slon",user_loc.getLongitude());
                                    bundle.putDouble("slat",user_loc.getLatitude());
                                    bundle.putDouble("elon",lon);
                                    bundle.putDouble("elat",lat);

                                    //todo 回传地点信息
                                    SelectActivity.this.loc_intent=new Intent(SelectActivity.this,Aps_Bottom_Activity.class);
                                    SelectActivity.this.loc_intent.putExtras(bundle);

                                    inputText.setText(title);
                                }
                            });
                        }
                    });
                }
            } else {
                Log.e("test","没有搜索结果");
            }
        } else {
            Log.e("test","搜索出现错误");
        }
    }

    @Override
    public void onPoiItemSearched(com.amap.api.services.core.PoiItem poiItem, int i) {

    }

    @Override
    public void onGetInputtips(List<Tip> list, int i) {
        //todo 接收推荐列表
        if(i==CODE_AMAP_SUCCESS){
            if(!list.isEmpty()){
                Log.e("test","总计"+list.size()+"条记录\n"+"城市为："+city_code);
                //解析获取到的PoiItem列表
                POI_list.setAdapter(new CommonAdapter<Tip>(this,R.layout.search_list_item,list) {
                    @Override
                    protected void convert(final ViewHolder holder, Tip tip, int position) {
                        //判断地点是否存在
                        if (tip.getPoint() == null && tip.getPoiID() == null) {
                            doSearchQuery("");
                            return;
                        } else if (tip.getPoint() == null) {
                            return;
                        }
                        //获取经纬度对象
                        LatLonPoint llp = tip.getPoint();
                        final double lon = llp.getLongitude();
                        final double lat = llp.getLatitude();
                        //返回POI的名称
                        final String title = tip.getName();
                        //返回POI的地址
                        final String text = tip.getAddress();
                        Log.e("test1", "地点：" + title + "\n地名：" + text + "\n坐标：（" + lon + "," + lat + "）");
                        holder.setText(R.id.textView, "地点：" + title + "\n地名：" + text);
                        //holder.setIsRecyclable(true);
                        holder.setOnClickListener(R.id.textView, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(start_flag) {
                                    startText.setText(title);
                                    user_loc=new LatLonPoint(lat,lon);
                                }
                                else if(input_flag) {
                                    inputText.setText(title);
                                    goal_loc=new LatLonPoint(lat,lon);
                                }

                                Bundle bundle = new Bundle();
                                bundle.putString("name", title);
                                bundle.putString("detail", text);
                                bundle.putString("city_code", city_code);
                                bundle.putDouble("slon", user_loc.getLongitude());
                                bundle.putDouble("slat", user_loc.getLatitude());
                                bundle.putDouble("elon", goal_loc.getLongitude());
                                bundle.putDouble("elat", goal_loc.getLatitude());
                                //回传地点信息
                                SelectActivity.this.loc_intent=new Intent(SelectActivity.this,Aps_Bottom_Activity.class);
                                SelectActivity.this.loc_intent.putExtras(bundle);
                            }
                        });
                    }
                });
            }
        }
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {

    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }
}

