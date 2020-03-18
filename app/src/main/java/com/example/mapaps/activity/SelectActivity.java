package com.example.mapaps.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.example.mapaps.R;
import com.example.mapaps.adapter.Common_Data;
import com.example.mapaps.adapter.GetGsonDataUnit;
import com.example.mapaps.adapter.ProviceBean;
import com.example.mapaps.adapter.SearchHistoryManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;
import com.zhy.adapter.recyclerview.wrapper.HeaderAndFooterWrapper;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.amap.api.services.core.AMapException.CODE_AMAP_SUCCESS;

public class SelectActivity extends AppCompatActivity implements TextWatcher, Inputtips.InputtipsListener,PoiSearch.OnPoiSearchListener, Common_Data {
    //POI参数
    private PoiSearch.Query query;
    private ArrayList<PoiItem> poiItems;
    private LatLonPoint user_loc=null;
    private LatLonPoint goal_loc=null;
    private Tip loc=null;
    private String city_code=null;
    private Intent loc_intent=null;
    private boolean start_flag=false;
    private boolean input_flag=true;

    //数据库
    private SearchHistoryManager searchHistoryManager;
    protected String DbTableName="SearchHistroy";

    //城市数据
    protected List<ProviceBean> proviceBeanList=new ArrayList<>();
    protected ArrayList<ArrayList<String>> cityBeanArrayList=new ArrayList<>();
    protected ArrayList<ArrayList<ArrayList<String>>> areaarrayList=new ArrayList<>();

    //顶部搜索框
    private EditText startText=null;
    private ImageView start_delete_btn=null;
    private EditText inputText=null;
    private ImageView delete_btn=null;
    private Button start_btn=null;
    private Button city_chosen_btn=null;
    private RecyclerView POI_list=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        Intent intent=getIntent();
        user_loc=new LatLonPoint(intent.getDoubleExtra("slat",0),intent.getDoubleExtra("slon",0));
        city_code=intent.getStringExtra("city_code");

        searchHistoryManager=new SearchHistoryManager(this,DbTableName);

        poiItems=new ArrayList<PoiItem>();
        loc=new Tip();

        POI_list=findViewById(R.id.search_list_view);
        POI_list.setLayoutManager(new LinearLayoutManager(this));

        getSearchRecord();

        startText=findViewById(R.id.start_edit_text);
        startText.addTextChangedListener(this);
        startText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(v.getId()==startText.getId()) {
                    start_flag=true;
                    input_flag=false;
                    try {
                        searchHistoryManager.addRecord(loc.getName(),loc.getAddress(),
                                loc.getPoint().getLatitude(),loc.getPoint().getLongitude());
                    }catch (Exception e){
                        Log.e("test51",e.getMessage());
                    }
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
                    input_flag=true;
                    start_flag=false;
                    try {
                        searchHistoryManager.addRecord(loc.getName(),loc.getAddress(),
                                loc.getPoint().getLatitude(),loc.getPoint().getLongitude());
                    }catch (Exception e){
                        Log.e("test61",e.getMessage());
                    }
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
                    //加入历史记录
                    if(!searchHistoryManager.isExist("name",loc_intent.getStringExtra("name"))){
                       addSearchRecord();
                    }

                    startActivity(loc_intent);
                    finish();
                }
                else {
                    Toast.makeText(SelectActivity.this,"请输入目的地",Toast.LENGTH_SHORT).show();
                }
            }
        });

        city_chosen_btn=findViewById(R.id.city_chosen);
        city_chosen_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPickerView();
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
        InputtipsQuery inputtipsQuery = new InputtipsQuery(key, code);
        //是否限制在当前城市
        if(!code.isEmpty()){
            inputtipsQuery.setCityLimit(true);
        }
        else {
            inputtipsQuery.setCityLimit(false);
        }
        //初始化监听器
        Inputtips inputtips=new Inputtips(SelectActivity.this, inputtipsQuery);
        inputtips.setInputtipsListener(this);

        inputtips.requestInputtipsAsyn();
    }

    //获取历史记录
    protected void getSearchRecord(){
        final List<Tip> list=searchHistoryManager.getAllTipsRecords();

        //todo 清除搜索历史功能失效
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
                        if (start_flag) {
                            startText.setText(title);
                            user_loc = new LatLonPoint(lat, lon);
                        } else if (input_flag) {
                            inputText.setText(title);
                            goal_loc = new LatLonPoint(lat, lon);
                        }

                        Bundle bundle = new Bundle();
                        try {
                            bundle.putString("name", title);
                            bundle.putString("detail", text);
                            bundle.putString("city_code", city_code);
                            bundle.putDouble("slon", user_loc.getLongitude());
                            bundle.putDouble("slat", user_loc.getLatitude());
                            bundle.putDouble("elon", goal_loc.getLongitude());
                            bundle.putDouble("elat", goal_loc.getLatitude());
                        }catch (Exception e){
                            Log.e("test",e.getMessage());
                        }
                        //回传地点信息
                        SelectActivity.this.loc_intent = new Intent(SelectActivity.this, Aps_Bottom_Activity.class);
                        SelectActivity.this.loc_intent.putExtras(bundle);
                    }
                });
            }
        });

        final HeaderAndFooterWrapper headerAndFooterWrapper=new HeaderAndFooterWrapper(POI_list.getAdapter());
        CardView foot_View=new CardView(this);
        final TextView textView=new TextView(this);
        foot_View.addView(textView);
        textView.setHint("清除历史记录");
        //todo 修改宽度后，控件销毁
        textView.setWidth(RecyclerView.LayoutParams.MATCH_PARENT);

        foot_View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchHistoryManager.deleteAllRecords();
                textView.setHint("没有历史记录");
                list.clear();
                headerAndFooterWrapper.notifyItemRangeRemoved(0,list.size());
                headerAndFooterWrapper.notifyDataSetChanged();
                Toast.makeText(SelectActivity.this,"已清除全部历史记录",Toast.LENGTH_SHORT).show();
            }
        });
        headerAndFooterWrapper.addFootView(foot_View);

        POI_list.setAdapter(headerAndFooterWrapper);
        headerAndFooterWrapper.notifyDataSetChanged();
    }

    //添加搜索历史
    protected void addSearchRecord(){
        Log.e("test","加入数据库数据：\n name："+loc.getName()+"\ndetail:"+loc.getAddress());
        try{
            searchHistoryManager.addRecord(loc.getName(),loc.getAddress(),
                    loc.getPoint().getLatitude(),loc.getPoint().getLongitude());
        }catch (Exception e){
            Log.e("test",e.getMessage());
            Toast.makeText(SelectActivity.this, (CharSequence) e.getCause(),Toast.LENGTH_SHORT).show();
        }

    }

    //获取城市编码
    private void getCity_code(String name){
        GeocodeSearch geocodeSearch=new GeocodeSearch(SelectActivity.this);
        geocodeSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
            @Override
            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {

            }

            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
                if(i==CODE_AMAP_SUCCESS){
                    city_code=geocodeResult.getGeocodeAddressList().get(0).getAdcode();
                }
            }
        });
        GeocodeQuery query=new GeocodeQuery(name,"");
        geocodeSearch.getFromLocationNameAsyn(query);
    }

    //从文件中获取json数据
    protected void transJson(){
        //读取
        String jsonstr=new GetGsonDataUnit().getJson(SelectActivity.this,"province.json");
        //解析
        Gson gson=new Gson();
        proviceBeanList=gson.fromJson(jsonstr, new TypeToken<List<ProviceBean>>(){}.getType());
        //分配数据
        for(int i=0;i<proviceBeanList.size();i++){
            ArrayList<String> citylist=new ArrayList<>();
            ArrayList<ArrayList<String>> arealist=new ArrayList<>();
            for(int j=0;j<proviceBeanList.get(i).getCity().size();j++){
                citylist.add(proviceBeanList.get(i).getCity().get(j).getName());
                ArrayList<String> city_area_list=new ArrayList<>();
                if(proviceBeanList.get(i).getCity().get(j).getArea()==null
                        ||proviceBeanList.get(i).getCity().get(j).getArea().size()==0){
                    city_area_list.add(" ");
                }
                else {
                    city_area_list.addAll(proviceBeanList.get(i).getCity().get(j).getArea());
                }
                arealist.add(city_area_list);
            }
            this.cityBeanArrayList.add(citylist);
            this.areaarrayList.add(arealist);
        }

    }

    //显示选择器
    private void showPickerView(){
        transJson();
        OptionsPickerView optionsPickerView=new OptionsPickerBuilder(SelectActivity.this, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                String tx=proviceBeanList.get(options1).getName()+
                        cityBeanArrayList.get(options1).get(options2)+
                        areaarrayList.get(options1).get(options2).get(options3);
                Toast.makeText(SelectActivity.this,tx,Toast.LENGTH_SHORT).show();
                getCity_code(tx);
                city_chosen_btn.setText(cityBeanArrayList.get(options1).get(options2));
            }
        })
                .setTitleText("选择城市")
                .setDividerColor(Color.BLACK)
                .setTextColorCenter(Color.BLACK)
                .setContentTextSize(20)
                .addOnCancelClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        city_code="";
                    }
                })
                .build();
        optionsPickerView.setPicker(proviceBeanList,cityBeanArrayList,areaarrayList);
        optionsPickerView.show();
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
                            loc.setName(poiItem.getTitle());
                            loc.setAddress(poiItem.getSnippet());
                            loc.setPostion(poiItem.getLatLonPoint());
                            final double lon = loc.getPoint().getLongitude();
                            final double lat = loc.getPoint().getLatitude();
                            //返回POI的名称
                            final String title = loc.getName();
                            //返回POI的地址
                            final String text = loc.getAddress();
                            Log.e("test11","地点："+title+"\n地名："+text+"\n坐标：（"+lon+","+lat+"）");
                            holder.setText(R.id.textView,"地点："+title+"\n地名："+text);
                            holder.setIsRecyclable(true);
                            holder.setOnClickListener(R.id.textView, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Bundle bundle=new Bundle();
                                    try {
                                        bundle.putString("name", title);
                                        bundle.putString("detail", text);
                                        bundle.putString("city_code", city_code);
                                        bundle.putDouble("slon", user_loc.getLongitude());
                                        bundle.putDouble("slat", user_loc.getLatitude());
                                        bundle.putDouble("elon",lon);
                                        bundle.putDouble("elat",lat);
                                    }catch (Exception e){
                                        Log.e("test",e.getMessage());
                                    }

                                    //回传地点信息
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
        //接收推荐列表
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
                        loc = tip;
                        final double lon = loc.getPoint().getLongitude();
                        final double lat = loc.getPoint().getLatitude();
                        //返回POI的名称
                        final String title = loc.getName();
                        //返回POI的地址
                        final String text = loc.getAddress();
                        Log.e("test12", "地点：" + title + "\n地名：" + text + "\n坐标：（" + lon + "," + lat + "）");
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
                                try {
                                    bundle.putString("name", title);
                                    bundle.putString("detail", text);
                                    bundle.putString("city_code", city_code);
                                    bundle.putDouble("slon", user_loc.getLongitude());
                                    bundle.putDouble("slat", user_loc.getLatitude());
                                    bundle.putDouble("elon", goal_loc.getLongitude());
                                    bundle.putDouble("elat", goal_loc.getLatitude());
                                }catch (Exception e){
                                    Log.e("test",e.getMessage());
                                }

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
}

