package luce.ctl.luce;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import luce.ctl.luce.BaiduMap.BaiduMapUtil;
import luce.ctl.luce.BaiduMap.ConvexHull;
import luce.ctl.luce.adapter.LacDataAdapter;
import luce.ctl.luce.okhttp.InterfaceUrl;
import luce.ctl.luce.okhttp.OkHttpManager;
import luce.ctl.luce.ui.LuCeDataList;
import luce.ctl.luce.ui.LuceCellInfo;
import luce.ctl.luce.utils.AlrDialog_Show;
import luce.ctl.luce.utils.ArrayUtils;
import luce.ctl.luce.utils.GPSUtils;
import luce.ctl.luce.utils.Gps2BaiDu;
import luce.ctl.luce.utils.NumCheck;
import luce.ctl.luce.utils.Point;
import luce.ctl.luce.utils.TimerProgressDialog;
import luce.ctl.luce.utils.TipHelper;
import luce.ctl.luce.utils.dataCode;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private Context context;
    private MapView mMapView = null;
    private BaiduMap mBaiduMap=null;

    private Spinner mncSpinner;
    private Spinner modeSpinner;
    private String[] modes={"中国移动","中国联通","中国电信"};
    private String zhishi_mode="GSM";
    private String[] zhishis={"GSM","LTE","WCDMA","CDMA","TD-SCDMA","WIFI"};
    private ArrayAdapter mncAdapter;
    private ArrayAdapter zhishiAdapter;

    private TextView lac_text;
    private TextView cell_text;
    private LinearLayout nid_liner;
    private EditText lac_edit;
    private EditText cell_edit;
    private EditText bid_edit;
    private CheckBox hex_mode;
    private Button qure_Btn;
    private Button clear_Btn;
    private Button set_ip;
    private Button area_btn;
    private LinearLayout linearLayout;
    private ListView listView;
    private LacDataAdapter lacDataAdapter;
    private List<LuceCellInfo> luceCellInfos=new ArrayList<>();

    int mode_index=0;
    public static boolean hex_flg = false;//十六进制
    String mcc="460";
    static String mnc="00";
    int mode=0;//网络类型 0移动 1联通 2电信
    int net=0;//制式 0:2g 、1:3g 、2:4g

    String lac="";
    static String cellid="";
    String sid="";
    String nid="";
    String bid="";
    BaiduMapUtil baiduMapUtil;
    private PowerManager.WakeLock wakeLock = null;
    public static List<OverlayOptions> showList=new ArrayList<>();
    public List<OverlayOptions> list_curent_overlayoptions=new ArrayList<>();//现在地图上显示的图层
    private String ip="";
//    private Handler handler;
    private List<Point> list_neibor_latlon=new ArrayList<>();//用来保存查询到的每个基站的其中一个数据,为了覆盖时使用经纬度
    private List<List<Point>> showList_latLngs=new ArrayList<>();//用于保存每个图层中的基站数据
    private List<List<String>> showList_rssi=new ArrayList<>();
    private int marker=0;
    private List<LatLng> marker_latlng=new ArrayList<>();//对地图上的两个标记点进行保存
    private List<LatLng> marker_latlng_data=new ArrayList<>();//对地图上的两个查询的数据
    private List<List<LatLng>> latlngs=new ArrayList<>();//用于存放展示图层的数据
    private Workhandler workhandler;
    private InterfaceUrl interfaceUrl;
    private List<LuCeDataList> luCeDataLists=new ArrayList<>();//用来存放区域查询得到的数据
    private boolean area=false;
    private OverlayOptions options=null;//区域显示的图层
    private TextView txt_list_shou;
    private TimerProgressDialog timerProgressDialog;
    private TimerProgressDialog timerProgressDialog1;
    private TimerProgressDialog timerDialog;
    private int first=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.main);

        context=this;
        initView();
        bindEvent();
    }


    private void initView() {
        myPermission();
        mMapView=(MapView) findViewById(R.id.bmapView);
        txt_list_shou=findViewById(R.id.txt_list_shou);
        initModeSpinner();
        initZhiShiSpinner();
        lac_text=(TextView)findViewById(R.id.lac_text_id);
        cell_text=(TextView)findViewById(R.id.cell_text_id);
       nid_liner=(LinearLayout)findViewById(R.id.nid_liner);
        hex_mode = (CheckBox)findViewById(R.id.Hex);
        lac_edit = (EditText)findViewById(R.id.lac_str);
        cell_edit = (EditText)findViewById(R.id.cellid_str);
        bid_edit = (EditText)findViewById(R.id.Bid_str);
        qure_Btn = (Button) findViewById(R.id.btn_qure);
        clear_Btn = (Button) findViewById(R.id.btn_clear);
        set_ip = (Button) findViewById(R.id.set_ip);
        area_btn=findViewById(R.id.btn_area_find);
        linearLayout=findViewById(R.id.linear);
        linearLayout.getBackground().setAlpha(150);
        initMap();
        listView=findViewById(R.id.neibor_list);
        lacDataAdapter=new LacDataAdapter(context,luceCellInfos,R.layout.cell_listview_item);
        listView.setAdapter(lacDataAdapter);
    }
    private void initMap() {
        mBaiduMap = mMapView.getMap();
        baiduMapUtil=new BaiduMapUtil(context,mBaiduMap);
        //普通地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        //marker的点击事件
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                final Button button = new Button(context);
                button.setBackgroundResource(R.drawable.popup);
                button.setTextColor(Color.BLACK);
                final LatLng ll = marker.getPosition();
                Bundle bundle=marker.getExtraInfo();
                final String info= (String) bundle.getSerializable("info");
                button.setText(info);
                InfoWindow mInfoWindow = new InfoWindow(button, ll, -47);
                mBaiduMap.showInfoWindow(mInfoWindow);
                return true;
            }
        });
        //百度地图的单击事件
        mBaiduMap.setOnMapLongClickListener(new BaiduMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                BitmapDescriptor bitmap=null;
                if (marker==0){
                    //构建Marker图标
                    bitmap = BitmapDescriptorFactory
                            .fromResource(R.drawable.icon_marka);
                    OverlayOptions option = new MarkerOptions()
                            .position(latLng)
                            .icon(bitmap);
                    mBaiduMap.addOverlay(option);
                    TipHelper.Vibrate(MainActivity.this,200);//点击震动
                    marker++;
                    LatLng latLng1=new LatLng(latLng.latitude-0.01,latLng.longitude-0.01);
                    LatLng latLng2=new LatLng(latLng.latitude+0.01,latLng.longitude+0.01);
                    marker_latlng_data.add(latLng1);
                    marker_latlng_data.add(latLng2);
                    marker_latlng.add(latLng);

                    options=null;
                    List<LatLng> latLngs=new ArrayList<>();
                    LatLng l1=marker_latlng_data.get(0);//(x1,y1)
                    LatLng l3=marker_latlng_data.get(1);//(x2,y2)
                    LatLng l2=new LatLng(l1.latitude,l3.longitude);
                    LatLng l4=new LatLng(l3.latitude,l1.longitude);
                    latLngs.add(l1);
                    latLngs.add(l2);
                    latLngs.add(l3);
                    latLngs.add(l4);
                    options = new PolygonOptions()
                            .points(latLngs)
                            .stroke(new Stroke(5, Color.GRAY))//color[showList.size()-1]
                            .fillColor(0);
                    mBaiduMap.addOverlay(options);
                }else if (marker==1){
                    mBaiduMap.clear();
                    options=null;
                    marker_latlng_data.clear();
                    bitmap = BitmapDescriptorFactory
                            .fromResource(R.drawable.icon_marka);
                    OverlayOptions option = new MarkerOptions()
                            .position(marker_latlng.get(0))
                            .icon(bitmap);
                    mBaiduMap.addOverlay(option);
                    bitmap = BitmapDescriptorFactory
                            .fromResource(R.drawable.icon_markb);
                    OverlayOptions option1 = new MarkerOptions()
                            .position(latLng)
                            .icon(bitmap);
                    mBaiduMap.addOverlay(option1);
                    TipHelper.Vibrate(MainActivity.this,200);
                    marker++;
                    marker_latlng.add(latLng);
                    for (int i=0;i<marker_latlng.size();i++){
                        marker_latlng_data.add(marker_latlng.get(i));
                    }

                    List<LatLng> latLngs=new ArrayList<>();
                    LatLng l1=marker_latlng.get(0);//(x1,y1)
                    LatLng l3=marker_latlng.get(1);//(x2,y2)
                    LatLng l2=new LatLng(l1.latitude,l3.longitude);
                    LatLng l4=new LatLng(l3.latitude,l1.longitude);
                    latLngs.add(l1);
                    latLngs.add(l2);
                    latLngs.add(l3);
                    latLngs.add(l4);
                    options = new PolygonOptions()
                            .points(latLngs)
                            .stroke(new Stroke(5, Color.GRAY))//color[showList.size()-1]
                            .fillColor(0);
                    mBaiduMap.addOverlay(options);
                }else if (marker>1){
                    AlertDialog.Builder builder=new AlertDialog.Builder(context)
                            .setTitle("提示")
                            .setMessage("最多只能添加两个标注点")
                            .setPositiveButton("确定",null);
                    builder.setCancelable(false);//不可以用返回键取消
                    builder.show();
                }
            }
        });
    }

    private void bindEvent() {
        txt_list_shou.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt=txt_list_shou.getText().toString();
                if (txt.equals("收起")){
                    listView.setVisibility(View.GONE);
                    txt_list_shou.setText("弹出");
                }else if (txt.equals("弹出")){
                    listView.setVisibility(View.VISIBLE);
                    txt_list_shou.setText("收起");
                }
            }
        });
        hex_mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked)
                {
                    //editext只显示数字和小数点
                    lac_edit.setInputType(InputType.TYPE_CLASS_NUMBER);
                    cell_edit.setInputType(InputType.TYPE_CLASS_NUMBER);
                    bid_edit.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
                else
                {
                    //editext显示文本
                    lac_edit.setInputType(InputType.TYPE_CLASS_TEXT);
                    cell_edit.setInputType(InputType.TYPE_CLASS_TEXT);
                    bid_edit.setInputType(InputType.TYPE_CLASS_TEXT);
                }
            }
        });
        qure_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean start_flg=false;
                start_flg=GetRequestParam();
                String filePath = Environment.getExternalStorageDirectory().getPath()+ File.separator+"LuCeIp.txt";
                File file=new File(filePath);
                if (file.exists()){
                    ip=read(file);
                }
                if(!(ip.equals("")||ip=="")){
                   interfaceUrl=new InterfaceUrl(ip);
                   timerDialog=null;
                    if(start_flg==true)
                    {
                        if (find()){
                            AlertDialog.Builder builder=new AlertDialog.Builder(context)
                                    .setTitle("提示")
                                    .setMessage("您刚刚已经查询过该基站，可在左侧列表中查看")
                                    .setPositiveButton("确定",null);
                            builder.show();
                        }else {
                            area=false;
                            options=null;
                            new Thread(new Runnable() {
                                public void run() {
                                    Request_Gps();
                                }
                            }).start();
                            timerDialog = new TimerProgressDialog(context,10000,"开始读取数据","正在读取数据请稍等");
                            timerDialog.show();
                        }
                    }
                }else {
                    AlertDialog.Builder builder=new AlertDialog.Builder(context);
                    builder.setTitle("提示");
                    builder.setMessage("请先写入IP");
                    builder.setPositiveButton("确定",null);
                    builder.show();
                }
            }
        });
        clear_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mBaiduMap!=null)
                {
                    mBaiduMap.clear();
                    showList.clear();
                    marker=0;
                    lac="";
                    cellid="";
                    sid="";
                    nid="";
                    bid="";
                    area=false;
                    options=null;
                    first=0;
                    timerProgressDialog=null;
                    timerProgressDialog1=null;
                    timerDialog=null;
                    marker_latlng.clear();
                    marker_latlng_data.clear();
                    list_curent_overlayoptions.clear();
                    list_neibor_latlon.clear();
                    showList_latLngs.clear();
                    showList_rssi.clear();
                    latlngs.clear();
                    luCeDataLists.clear();
                    luceCellInfos.clear();
                    lacDataAdapter.notifyDataSetChanged();
                }
            }
        });
        set_ip.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = getLayoutInflater();
                final View layout = inflater.inflate(R.layout.set_ip, null);
                final AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setTitle("提示");
                builder.setView(layout);
                final EditText ip=layout.findViewById(R.id.editText_ip);
                EditText other=layout.findViewById(R.id.editText_other);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String message=ip.getText().toString();
                        if (message.equals("")||message==""){
                            Toast.makeText(context,"请输入IP，输入框不能为空",Toast.LENGTH_SHORT);
                        }else {
                            writeMsgToFile(message);
//                            SharedPreferencesHandler.setDataToPref(context,"luce_ip",message);
                        }
                    }
                });
                builder.setNegativeButton("取消",null);
                builder.setCancelable(false);//不可以用返回键取消
                builder.show();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final int current_position=i;
                AlertDialog.Builder builder=new AlertDialog.Builder(context)
                        .setTitle("请选择")
                        .setMessage("请选择是覆盖显示还是单个显示,数据加载过程需要一定的时间，不要进行重复点击")
                        .setPositiveButton("覆盖", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                OverlayOptions options = null;
                                if (area){
                                    List<LatLng> lngs=latlngs.get(current_position);
                                    int color1=luceCellInfos.get(current_position).getColor();
                                    if (lngs.size()>=3){
                                        options = new PolygonOptions()
                                                .points(lngs)
                                                .stroke(new Stroke(5, color1))//color[showList.size()-1]
                                                .fillColor(0);
                                    }
                                }else {
                                    options=showList.get(current_position);
                                }
                                for (int m=0;m<list_curent_overlayoptions.size();m++){
                                    if (list_curent_overlayoptions.get(m)==options){//说明地图上现在有该图层
                                        final double[] latlon= GPSUtils.wgs2bd(Double.valueOf(list_neibor_latlon.get(current_position).getLat()),Double.valueOf(list_neibor_latlon.get(current_position).getLon()));
                                        LatLng cenpt = new LatLng(latlon[0],latlon[1]);
                                        //定义地图状态
                                        MapStatus mMapStatus = new MapStatus.Builder()
                                                .target(cenpt)
                                                .zoom(18)
                                                .build();
                                        //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化

                                        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
                                        //改变地图状态
                                        mBaiduMap.setMapStatus(mMapStatusUpdate);
                                        break;
                                    }else {//说明地图上现在没有该图层
                                        List<Point> current_list=showList_latLngs.get(current_position);
                                        int max_rssi=Integer.valueOf(showList_rssi.get(current_position).get(0));
                                        int min_rssi=Integer.valueOf(showList_rssi.get(current_position).get(0));
                                        for (int a=0;a<showList_rssi.get(current_position).size()-1;a++){
                                            int current=Integer.valueOf(showList_rssi.get(current_position).get(a));
                                            if (current>max_rssi){
                                                max_rssi=current;
                                            } else if (current<min_rssi){
                                                min_rssi=current;
                                            }
                                        }
                                        for (int b=0;b<current_list.size();b++){
                                            int rssi_rgb;
                                            if (Integer.valueOf(current_list.get(b).getRssi())-min_rssi==0||max_rssi-min_rssi==0){
                                                rssi_rgb=0;
                                            }else {
                                                rssi_rgb=(Integer.valueOf(current_list.get(b).getRssi())-min_rssi)*100/(max_rssi-min_rssi);
                                            }
                                            final double[] latlon1= GPSUtils.wgs2bd(Double.valueOf(current_list.get(b).getLat()),Double.valueOf(current_list.get(b).getLon()));
                                            if (!((int)latlon1[0]==0&&(int)latlon1[1]==0)){
                                                baiduMapUtil.addMarker(latlon1[0],latlon1[1],"基站信息："+current_list.get(b).getP1()+","+current_list.get(b).getP2()+","+current_list.get(b).getP3()+"\n"
                                                        +Double.valueOf(current_list.get(b).getRssi())+"",(100-rssi_rgb)*255/100 ,rssi_rgb*255/100,0f,1.0f);
                                            }
                                        }
                                        if (current_list.size()>3){
                                            if (options!=null){
                                                if (area){
                                                    mBaiduMap.addOverlay(options);
                                                    list_curent_overlayoptions.add(options);
                                                }else {
                                                    mBaiduMap.addOverlay(showList.get(current_position));
                                                    list_curent_overlayoptions.add(showList.get(current_position));
                                                }
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                        })
                        .setNegativeButton("单独显示", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int position) {
                                mBaiduMap.clear();
                                list_curent_overlayoptions.clear();
                                mBaiduMap.addOverlay(options);
                                try{
                                    OverlayOptions options1=null;
                                    if (area){
                                        List<LatLng> lats=latlngs.get(current_position);
                                        int color=luceCellInfos.get(current_position).getColor();
                                        if (lats.size()>=3){
                                            options1 = new PolygonOptions()
                                                    .points(lats)
                                                    .stroke(new Stroke(5, color))//color[showList.size()-1]
                                                    .fillColor(0);
                                        }
                                    }else {
                                        options1=showList.get(current_position);
                                    }
                                    List<Point> current_list=showList_latLngs.get(current_position);
                                    int max_rssi=Integer.valueOf(showList_rssi.get(current_position).get(0));
                                    int min_rssi=Integer.valueOf(showList_rssi.get(current_position).get(0));
                                    for (int i=0;i<showList_rssi.get(current_position).size()-1;i++){
                                        int current=Integer.valueOf(showList_rssi.get(current_position).get(i));
                                        if (current>max_rssi){
                                            max_rssi=current;
                                        } else if (current<min_rssi){
                                            min_rssi=current;
                                        }
                                    }
                                    for (int i=0;i<current_list.size();i++){
                                        int rssi_rgb;
                                        if (Integer.valueOf(current_list.get(i).getRssi())-min_rssi==0||max_rssi-min_rssi==0){
                                            rssi_rgb=0;
                                        }else {
                                            rssi_rgb=(Integer.valueOf(current_list.get(i).getRssi())-min_rssi)*100/(max_rssi-min_rssi);
                                        }
                                        final double[] latlon1= GPSUtils.wgs2bd(Double.valueOf(current_list.get(i).getLat()),Double.valueOf(current_list.get(i).getLon()));
                                        if (!((int)latlon1[0]==0&&(int)latlon1[1]==0)){
                                            baiduMapUtil.addMarker(latlon1[0],latlon1[1],"基站信息："+current_list.get(i).getP1()+","+current_list.get(i).getP2()+","+current_list.get(i).getP3()+"\n"
                                                    +Double.valueOf(current_list.get(i).getRssi())+"",(100-rssi_rgb)*255/100 ,rssi_rgb*255/100,0f,1.0f);
                                        }
                                    }
                                    if (current_list.size()>3){
                                        if (options1!=null){
                                            if (area){
                                                mBaiduMap.addOverlay(options1);
                                                list_curent_overlayoptions.add(options1);
                                            }else {
                                                OverlayOptions options=showList.get(current_position);
                                                mBaiduMap.addOverlay(options);
                                                list_curent_overlayoptions.add(options);
                                            }
                                        }
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
                builder.setNeutralButton("取消",null);
                builder.setCancelable(false);//不可以用返回键取消
                builder.show();
            }

        });
        area_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               getMncMcc();
                String filePath = Environment.getExternalStorageDirectory().getPath()+ File.separator+"LuCeIp.txt";
                File file=new File(filePath);
                if (file.exists()){
                    ip=read(file);
                }
                if(!(ip.equals("")||ip=="")){
                    if (marker_latlng_data.size()==0){
                        AlertDialog.Builder builder=new AlertDialog.Builder(context);
                        builder.setTitle("提示");
                        builder.setMessage("请先在地图上标示出要查询的区域点");
                        builder.setPositiveButton("确定",null);
                        builder.show();
                    }else {
                        timerProgressDialog1 = null;
                        timerProgressDialog = null;
                        interfaceUrl = new InterfaceUrl(ip);
                        area = true;
                        first = 0;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Looper.prepare();//必须在Looper.myLooper()之前
                                Looper looperTest = Looper.myLooper();
                                workhandler = new Workhandler(looperTest);//mWorkHandler属于新创建的线程
                                area_search(marker_latlng_data);
                                Looper.loop();
                            }
                        }).start();
                        timerProgressDialog1 = new TimerProgressDialog(context, 20000, "开始读取服务器数据", "正在读取数据请稍等");
                        timerProgressDialog1.show();
                    }
                }else {
                    AlertDialog.Builder builder=new AlertDialog.Builder(context);
                    builder.setTitle("提示");
                    builder.setMessage("请先写入IP");
                    builder.setPositiveButton("确定",null);
                    builder.show();
                }
            }
        });

    }
    private void area_search(List<LatLng> latLngs){
        Map<String, String> params = new HashMap<>();
        params.put("mcc", mcc );
        params.put("mnc", mnc );
        params.put("system",zhishi_mode);
        if (latLngs.size()==1){//标注一个点
            LatLng latLng= Gps2BaiDu.baiduToGps(latLngs.get(0).latitude,latLngs.get(0).longitude);
            params.put("lon1", String.valueOf(latLng.longitude));
            params.put("lat1", String.valueOf(latLng.latitude) );
        }else if (latLngs.size()==2){//添加了两个标注
            LatLng latLng1=Gps2BaiDu.baiduToGps(latLngs.get(0).latitude,latLngs.get(0).longitude);
            LatLng latLng2=Gps2BaiDu.baiduToGps(latLngs.get(1).latitude,latLngs.get(1).longitude);
            params.put("lon1", String.valueOf(latLng1.longitude));
            params.put("lat1", String.valueOf(latLng1.latitude) );
            params.put("lon2", String.valueOf(latLng2.longitude));
            params.put("lat2", String.valueOf(latLng2.latitude) );
        }
        OkHttpClient okHttpClient = new OkHttpClient();
        FormBody.Builder builder1 = new FormBody.Builder();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder1.add(entry.getKey(), entry.getValue());
            }
        }
//        Looper looper = Looper.getMainLooper(); //主线程的Looper对象
//        workhandler = new Workhandler(looper);

        RequestBody requestBody =  builder1.build();
        Request.Builder builder = new Request.Builder();
        String URL=interfaceUrl.getBASEURL();
        builder.url(URL);
        builder.post(requestBody);
        Call call  = okHttpClient.newCall(builder.build());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Message msg = handler .obtainMessage(0,0,4,null);
                handler .sendMessage(msg);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if(response.isSuccessful()){
                        InputStream is = response.body().byteStream();
                        int datalength = 0;
                        int reccount = 0;
                        boolean headread = false;
                        int sublen = -1;

                        byte[] bArr = new byte[1024 *6];
                        byte[] msgBuffer = new byte[0];

                        int size = is.read(bArr, 0, (int) bArr.length);
                        msgBuffer = ArrayUtils.append(msgBuffer, ArrayUtils.subArray(bArr, 0,size));
                        int i=0;
                        while (size > 0 || msgBuffer.length > 0) {
//                            int pointer_package = 0;
                            if (size > 0) {
                                size = is.read(bArr, 0, (int) bArr.length);
                            }
                            if (size > 0) {
                                msgBuffer = ArrayUtils.append(msgBuffer, ArrayUtils.subArray(bArr,0, size));
                            }

                            if (!headread && msgBuffer.length >= 4) {
                                byte[] alllength = ArrayUtils.subArray(msgBuffer, 0, 4);
                                datalength = (alllength[3] & 0xff) * 256 * 256 * 256
                                        + (alllength[2] & 0xff) * 256 * 256
                                        + (alllength[1] & 0xff) * 256
                                        + (alllength[0] & 0xff);
                                msgBuffer = ArrayUtils.subArray(msgBuffer, 4, msgBuffer.length - 4);
//                                pointer_package += 4;
                                headread = true;
                                if (size==-1&&datalength==0){
                                    Message message=new Message();
                                    message.arg1=2;
                                    handler1.sendMessage(message);
                                    break;
                                }
                            }
                            if (headread) {
                                //头长度已读
                                if (msgBuffer.length >= 4 && sublen < 0) {
                                    byte[] sublength = ArrayUtils.subArray(msgBuffer, 0, 4);
                                    sublen = (sublength[3] & 0xff) * 256 * 256 * 256
                                            + (sublength[2] & 0xff) * 256 * 256
                                            + (sublength[1] & 0xff) * 256
                                            + (sublength[0] & 0xff);
                                    msgBuffer = ArrayUtils.subArray(msgBuffer, 4, msgBuffer.length - 4);
//                                    pointer_package += 4;
                                }else if (msgBuffer.length >= sublen&&sublen>0) {
                                    i++;
                                    byte[] subdata = ArrayUtils.subArray(msgBuffer, 0, sublen);
                                    msgBuffer = ArrayUtils.subArray(msgBuffer, sublen, msgBuffer.length - sublen);
                                    sublen = -1;
                                    //数据处理
                                    String json=new String(subdata);
                                    JSONObject object=new JSONObject(json);
                                    JSONArray array=object.getJSONArray("datalist");
                                    reccount+=array.length();

                                    Message message = Message.obtain();
                                    message.arg1=reccount;
                                    message.arg2=datalength;
                                    message.obj=object;
                                    workhandler.sendMessage(message);//此时是在另一个线程，通过直接访问新线程中的变量向 TestHandlerThread发送消息
                                }
                            }
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }
    private int one_package=0;
    private List<List<Point>> remove_0_after_list=new ArrayList<>();
    private final static Object object=new Object();
    public class Workhandler extends Handler {
        public Workhandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            synchronized (object) {
                int count_sum = msg.arg2;
                first++;
                if (msg.arg1 > 0) {
                    if (first == 1) {
                        Message message = new Message();
                        message.arg1 = count_sum;
                        message.arg2 = 3;
                        message.obj = msg.arg1;
                        handler1.sendMessage(message);
                    }
                    one_package = msg.arg1;
                    JSONObject object = (JSONObject) msg.obj;
                    try {
                        JSONArray array = object.getJSONArray("datalist");
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jsonObject = (JSONObject) array.opt(i);
                            Point point = new Point();
                            String lon = jsonObject.getString("longdot");
                            String lat = jsonObject.getString("latdot");
                            point.setLat(lat);
                            point.setLon(lon);
                            if (jsonObject.has("address")) {
                                if (!(jsonObject.getString("address").equals("") || jsonObject.getString("address") == null)) {
                                    point.setAcc(jsonObject.getString("address"));
                                }
                            }
                            if (jsonObject.has("power")) {
                                if (!(jsonObject.getString("power").equals("") || jsonObject.getString("power") == null)) {
                                    point.setRssi(jsonObject.getString("power"));
                                }
                            }
                            if (jsonObject.has("p1")) {
                                if (!(jsonObject.getString("p1").equals("") || jsonObject.getString("p1") == null)) {
                                    point.setP1(jsonObject.getString("p1"));
                                }
                            }
                            if (jsonObject.has("p2")) {
                                if (!(jsonObject.getString("p2").equals("") || jsonObject.getString("p2") == null)) {
                                    point.setP2(jsonObject.getString("p2"));
                                }
                            }
                            if (jsonObject.has("p3")) {
                                if (!(jsonObject.getString("p3").equals("") || jsonObject.getString("p3") == null)) {
                                    point.setP3(jsonObject.getString("p3"));
                                }
                            }
                            if (luCeDataLists.size() == 0) {
                                LuCeDataList luCeDataList = new LuCeDataList();
                                luCeDataList.setLac_sid(point.getP1());
                                luCeDataList.setCi_nid(point.getP2());
                                luCeDataList.setBid(point.getP3());
                                luCeDataList.setColor(0xff00ffff);
                                luCeDataList.getList().add(point);
                                luCeDataLists.add(luCeDataList);
                            } else {
                                boolean flag = false;
                                for (int k = 0; k < luCeDataLists.size(); k++) {
                                    if (point.getP1().equals(luCeDataLists.get(k).getLac_sid()) && point.getP2().equals(luCeDataLists.get(k).getCi_nid())) {
                                        LuCeDataList luCeDataList = luCeDataLists.get(k);
                                        luCeDataList.getList().add(point);
                                        flag = true;
                                        break;
                                    }
                                }
                                if (!flag) {
                                    LuCeDataList luCeDataList = new LuCeDataList();
                                    luCeDataList.setLac_sid(point.getP1());
                                    luCeDataList.setCi_nid(point.getP2());
                                    luCeDataList.setBid(point.getP3());
                                    if (luCeDataLists.size() == 1 || luCeDataLists.size() > 1) {
                                        setColor(luCeDataList,luCeDataLists);
//                                        if (luCeDataLists.size() < 7) {
//                                            luCeDataList.setColor(color[luCeDataLists.size() - 1]);
//                                        } else if (luCeDataLists.size() > 6 && luCeDataLists.size() < 13) {
//                                            luCeDataList.setColor(color[luCeDataLists.size() - 7]);
//                                        } else if (luCeDataLists.size() > 12 && luCeDataLists.size() < 19) {
//                                            luCeDataList.setColor(color[luCeDataLists.size() - 13]);
//                                        } else if (luCeDataLists.size() > 18 && luCeDataLists.size() < 25) {
//                                            luCeDataList.setColor(color[luCeDataLists.size() - 19]);
//                                        } else if (luCeDataLists.size() > 24 && luCeDataLists.size() < 31) {
//                                            luCeDataList.setColor(color[luCeDataLists.size() - 25]);
//                                        } else if (luCeDataLists.size() > 30 && luCeDataLists.size() < 37) {
//                                            luCeDataList.setColor(color[luCeDataLists.size() - 31]);
//                                        } else if (luCeDataLists.size() > 36 && luCeDataLists.size() < 43) {
//                                            luCeDataList.setColor(color[luCeDataLists.size() - 37]);
//                                        } else if (luCeDataLists.size() > 42 && luCeDataLists.size() < 49) {
//                                            luCeDataList.setColor(color[luCeDataLists.size() - 43]);
//                                        } else if (luCeDataLists.size() > 48 && luCeDataLists.size() < 55) {
//                                            luCeDataList.setColor(color[luCeDataLists.size() - 49]);
//                                        } else if (luCeDataLists.size() > 54 && luCeDataLists.size() < 61) {
//                                            luCeDataList.setColor(color[luCeDataLists.size() - 55]);
//                                        }
                                    }
                                    luCeDataList.getList().add(point);
                                    luCeDataLists.add(luCeDataList);
                                }
                            }
                        }
                        if (count_sum == one_package) {
                            workhandler.getLooper().quit();
                            //把luCeDataLists改成需要的数据
                            addData(luCeDataLists);
                            //把得到的所有数据转换成需要的数据格式
                            List<List<Point>> convex_point = new ArrayList<>();
                            for (int i = 0; i < remove_0_after_list.size(); i++) {
                                List<Point> list = remove_0_after_list.get(i);
                                ConvexHull convexHull = new ConvexHull(list);
                                List<Point> list_po = convexHull.calculateHull();
                                convex_point.add(list_po);
                            }
                            for (int w = 0; w < convex_point.size(); w++) {
                                List<Point> list_po = convex_point.get(w);
                                List<LatLng> latLngs = new ArrayList<>();
                                for (int j = 0; j < list_po.size(); j++) {
                                    Point point = list_po.get(j);
                                    final double[] latlon = GPSUtils.wgs2bd(Double.valueOf(point.getLat()), Double.valueOf(point.getLon()));
                                    LatLng latLng = new LatLng(latlon[0], latlon[1]);
                                    latLngs.add(latLng);
                                }
                                latlngs.add(latLngs);
                            }
                            convex_point.clear();//***************************************************
                            remove_0_after_list.clear();//***************************************************
                            //对每个小区下的数据集合进行筛选   把距离小于10的数据删掉
                            removeData(luCeDataLists);
                            for (int t = 0; t < luCeDataLists.size(); t++) {
                                showList_latLngs.add(luCeDataLists.get(t).getList());
                                List<String> list_rssi = new ArrayList<>();
                                for (int j = 0; j < luCeDataLists.get(t).getList().size(); j++) {
                                    list_rssi.add(luCeDataLists.get(t).getList().get(j).getRssi());
                                }
                                showList_rssi.add(list_rssi);
                            }
                            Message message_msg = new Message();
                            message_msg.arg1 = 1;
                            handler1.sendMessage(message_msg);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        }
    }
    }
    Handler handler1=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.arg1==1){
                lacDataAdapter.notifyDataSetChanged();
                if (timerProgressDialog!=null){
                    timerProgressDialog.dismiss();
                }
                AlertDialog.Builder builder=new AlertDialog.Builder(context)
                        .setTitle("提示")
                        .setMessage("区域查询数据已全部加载完毕！")
                        .setPositiveButton("确定",null);
                builder.show();
            }else if (msg.arg1==2){
                if (timerDialog!=null){
                    timerDialog.dismiss();
                }
                if (timerProgressDialog!=null){
                    timerProgressDialog.dismiss();
                }
                if (timerProgressDialog1!=null){
                    timerProgressDialog1.dismiss();
                }
                AlertDialog.Builder builder=new AlertDialog.Builder(context)
                        .setTitle("提示")
                        .setMessage("没有查询到数据")
                        .setPositiveButton("确定",null);
                builder.show();
            }
            else if (msg.arg2==3){
                if (timerProgressDialog1!=null){
                    timerProgressDialog1.dismiss();
                }
                timerProgressDialog = new TimerProgressDialog(context,3*msg.arg1,Integer.valueOf((Integer) msg.obj),"正在加载数据到本地","正在加载数据请稍等");
                timerProgressDialog.show();
            }
        }
    };

    private void removeData(List<LuCeDataList> luCeDataLists){
        for (int i1=0;i1<luCeDataLists.size();i1++){
            List<Point> list=luCeDataLists.get(i1).getList();
            if (list.size()>100){
                int size=list.size()/100;
                for (int i2=list.size()-1;i2<list.size()&&i2>=0;i2--){
                    if (!(i2%size==0)){
                        list.remove(i2);
//                        i2--;
                    }
                }
            }
        }
    }

    /**
     * 添加为需要的数据
     * @param luCeDataLists
     */
    private void addData(List<LuCeDataList> luCeDataLists){
        for (int t=0;t<luCeDataLists.size();t++){
            LuceCellInfo luceCellInfo=new LuceCellInfo();
            luceCellInfo.setLac_sid(Integer.valueOf(luCeDataLists.get(t).getLac_sid()));
            luceCellInfo.setCi_nid(Integer.valueOf(luCeDataLists.get(t).getCi_nid()));
            if (!(luCeDataLists.get(t).getBid()==null)){
                luceCellInfo.setBid(Integer.valueOf(luCeDataLists.get(t).getBid()));
            }
            luceCellInfo.setColor(luCeDataLists.get(t).getColor());
            luceCellInfo.setSize(luCeDataLists.get(t).getList().size());
            luceCellInfos.add(luceCellInfo);
        }
        for (int i=0;i<luCeDataLists.size();i++){
            LuCeDataList luCeDataList=luCeDataLists.get(i);
            List<Point> list=luCeDataList.getList();
            for (int q=0;q<list.size();q++){
                Point point= list.get(q);
                double lat=Double.valueOf(point.getLat());
                double lon=Double.valueOf(point.getLon());
                if ((int)lat==0&&(int)lon==0){
                    list.remove(q);
                    q--;
                }
            }
            remove_0_after_list.add(list);
            Point p=new Point();
            p.setP1(String.valueOf(luCeDataList.getLac_sid()));
            p.setP2(String.valueOf(luCeDataList.getCi_nid()));
            p.setP3(String.valueOf(luCeDataList.getBid()));
            list_neibor_latlon.add(p);
            for (int h=0;h<list_neibor_latlon.size();h++){
                if ((list.get(0).getP1().equals(list_neibor_latlon.get(h).getP1()))&&(list.get(0).getP2().equals(list_neibor_latlon.get(h).getP2()))){
                    Point point=list_neibor_latlon.get(h);
                    point.setLat(list.get(0).getLat());
                    point.setLon(list.get(0).getLon());
                }
            }
        }
    }
    private void setColor(LuCeDataList luCeDataList,List<LuCeDataList> luCeDataLists){
        if (luCeDataLists.size() < 7) {
            luCeDataList.setColor(color[luCeDataLists.size() - 1]);
        } else if (luCeDataLists.size() > 6 && luCeDataLists.size() < 13) {
            luCeDataList.setColor(color[luCeDataLists.size() - 7]);
        } else if (luCeDataLists.size() > 12 && luCeDataLists.size() < 19) {
            luCeDataList.setColor(color[luCeDataLists.size() - 13]);
        } else if (luCeDataLists.size() > 18 && luCeDataLists.size() < 25) {
            luCeDataList.setColor(color[luCeDataLists.size() - 19]);
        } else if (luCeDataLists.size() > 24 && luCeDataLists.size() < 31) {
            luCeDataList.setColor(color[luCeDataLists.size() - 25]);
        } else if (luCeDataLists.size() > 30 && luCeDataLists.size() < 37) {
            luCeDataList.setColor(color[luCeDataLists.size() - 31]);
        } else if (luCeDataLists.size() > 36 && luCeDataLists.size() < 43) {
            luCeDataList.setColor(color[luCeDataLists.size() - 37]);
        } else if (luCeDataLists.size() > 42 && luCeDataLists.size() < 49) {
            luCeDataList.setColor(color[luCeDataLists.size() - 43]);
        } else if (luCeDataLists.size() > 48 && luCeDataLists.size() < 55) {
            luCeDataList.setColor(color[luCeDataLists.size() - 49]);
        } else if (luCeDataLists.size() > 54 && luCeDataLists.size() < 61) {
            luCeDataList.setColor(color[luCeDataLists.size() - 55]);
        }else if (luCeDataLists.size() > 60 && luCeDataLists.size() < 67) {
            luCeDataList.setColor(color[luCeDataLists.size() - 61]);
        }else if (luCeDataLists.size() > 66 && luCeDataLists.size() < 73) {
            luCeDataList.setColor(color[luCeDataLists.size() - 67]);
        }else if (luCeDataLists.size() > 72 && luCeDataLists.size() < 79) {
            luCeDataList.setColor(color[luCeDataLists.size() - 73]);
        }
    }
    public boolean find(){
        boolean flag=false;
        for (int n=0;n<luceCellInfos.size();n++){
            if(mode== dataCode.cdma)
            {
                LuceCellInfo luceCellInfo=luceCellInfos.get(n);
                if (String.valueOf(luceCellInfo.getLac_sid()).equals(sid)&&String.valueOf(luceCellInfo.getCi_nid()).equals(nid)){
                    flag=true;
                    break;
                }
            }else {
                LuceCellInfo luceCellInfo=luceCellInfos.get(n);
                if (String.valueOf(luceCellInfo.getLac_sid()).equals(lac)&&String.valueOf(luceCellInfo.getCi_nid()).equals(cellid)){
                    flag=true;
                    break;
                }
            }
        }
        return flag;
    }
    private void initModeSpinner(){
        mncSpinner = (Spinner)findViewById(R.id.set_mnc_mode);
        mncAdapter = new ArrayAdapter<String>(context,android.R.layout.simple_spinner_item,modes);
        //设置下拉列表的风格
        mncAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        mncSpinner.setAdapter(mncAdapter);
        mncSpinner.setSelection(0,true);
        //添加事件Spinner事件监听
        mncSpinner.setOnItemSelectedListener(dModeSelectLis);
    }
    private void initZhiShiSpinner(){
        modeSpinner=findViewById(R.id.set_mode);
        zhishiAdapter=new ArrayAdapter(context,android.R.layout.simple_spinner_item,zhishis);
        zhishiAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        modeSpinner.setAdapter(zhishiAdapter);
        modeSpinner.setSelection(0,true);
        //添加事件Spinner事件监听
        modeSpinner.setOnItemSelectedListener(dZhiShiSelectLis);
    }
    private AdapterView.OnItemSelectedListener dZhiShiSelectLis = new AdapterView.OnItemSelectedListener()
    {//zhishis={"GSM","LTE","WCDMA","CDMA","TD-SCDMA","WIFI"};
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch (position){
                case 0:
                    zhishi_mode="GSM";
                    break;
                case 1:
                    zhishi_mode="LTE";
                    break;
                case 2:
                    zhishi_mode="WCDMA";
                    break;
                case 3:
                    zhishi_mode="CDMA";
                    break;
                case 4:
                    zhishi_mode="TD-SCDMA";
                    break;
                case 5:
                    zhishi_mode="WIFI";
                    break;

            }
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };
    private AdapterView.OnItemSelectedListener dModeSelectLis = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            // btsCtrl.setDetectMode(position);
            mode_index=position;
            if(mode_index==2)
            {
                lac_text.setText("SID");
                cell_text.setText("BID");
                nid_liner.setVisibility(View.VISIBLE);
            }
            else
            {
                lac_text.setText("LAC");
                cell_text.setText("CELL");
                nid_liner.setVisibility(View.GONE);

            }
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };
    private boolean GetRequestParam() {
        boolean start_flg = false;
        mcc = "460";
        mnc = "00";
        lac = lac_edit.getText().toString().trim();
        cellid = cell_edit.getText().toString().trim();
        sid = lac_edit.getText().toString().trim();
        bid = cell_edit.getText().toString().trim();
        nid = bid_edit.getText().toString().trim();
//        nid = cell_edit.getText().toString().trim();
//        bid = bid_edit.getText().toString().trim();
        if(mode_index== dataCode.cdma)
        {
            mnc = "11";
        }
        if (net == 0)
        {
            mode = mode_index;
            if (mode_index == dataCode.china_mobile) {
                mnc = "00";
            } else if (mode_index == dataCode.china_unicom) {
                mnc = "01";
            }
        }
        else if (net == 1)//3g
        {
            if (mode_index == dataCode.china_mobile) {
                mnc = "00";
                mode = dataCode.td_swcdma;
            } else if (mode_index == dataCode.china_unicom) {
                mode = dataCode.wcdma;
                mnc = "01";
            }
        }
        else if (net == 2)//4g
        {
            if (mode_index == dataCode.china_mobile) {
                mode = dataCode.tdd_lte;
                mnc = "00";
            } else if (mode_index == dataCode.china_unicom) {
                mode = dataCode.fdd_lte;
                mnc = "01";
            } else {
                mode = dataCode.fdd_lte;
                mnc = "01";
            }

        }

        if(NumCheck.isNumeric(mcc)==true)
        {
            if(NumCheck.isNumeric(mnc)==true)
            {
                if(hex_mode.isChecked())
                {
                    hex_flg = true;
                }
                else
                {
                    hex_flg = false;
                }
                if(mode==dataCode.cdma)//电信
                {
                    if("".equals(sid))
                    {
                        AlrDialog_Show.alertDialog(context,"提示","请设置SID!","确定");
                    }
                    else if("".equals(nid))
                    {
                        AlrDialog_Show.alertDialog(context,"提示","请设置NID!","确定");
                    }
                    else if("".equals(bid))
                    {
                        AlrDialog_Show.alertDialog(context,"提示","请设置BID!","确定");
                    }
                    else
                    {

                        if(hex_flg==true)//十六进制
                        {
                            boolean x_flg=false;
                            //检查x_flg是否为16进制  如果是则转换为10进制
                            x_flg=NumCheck.isTrueHexDigit(sid);
                            if(x_flg==true)
                            {
                                long value = Long.parseLong(sid,16);
                                sid= Long.toString(value);
                                x_flg=NumCheck.isTrueHexDigit(nid);
                                if(x_flg==true)
                                {
                                    value = Long.parseLong(nid,16);
                                    nid= Long.toString(value);
                                    x_flg=NumCheck.isTrueHexDigit(bid);
                                    if(x_flg==true)
                                    {
                                        value = Long.parseLong(bid,16);
                                        bid= Long.toString(value);
                                        start_flg=true;
                                    }
                                    else
                                    {
                                        AlrDialog_Show.alertDialog(context,"提示","请设置正确格式BID!","确定");
                                    }
                                }
                                else
                                {
                                    AlrDialog_Show.alertDialog(context,"提示","请设置正确格式NID!","确定");
                                }
                            }
                            else
                            {
                                AlrDialog_Show.alertDialog(context,"提示","请设置正确格式SID!","确定");
                            }
                        }
                        else//十进制
                        {
                            if(NumCheck.isNumeric(bid)==true)
                            {
                                if(NumCheck.isNumeric(nid)==true)
                                {
                                    if(NumCheck.isNumeric(sid)==true)
                                    {
                                        start_flg=true;
                                    }
                                    else
                                    {
                                        AlrDialog_Show.alertDialog(context,"提示","请设置正确格式SID!","确定");
                                    }
                                }
                                else
                                {
                                    AlrDialog_Show.alertDialog(context,"提示","请设置正确格式NID!","确定");
                                }
                            }
                            else
                            {
                                AlrDialog_Show.alertDialog(context,"提示","请设置正确格式BID!","确定");
                            }
                        }
                    }
                }
                else
                {
                    if("".equals(lac))
                    {
                        AlrDialog_Show.alertDialog(context,"提示","请设置LAC!","确定");
                    }
                    else if("".equals(cellid))
                    {
                        AlrDialog_Show.alertDialog(context,"提示","请设置CELLID!","确定");
                    }
                    else
                    {
                        if(hex_flg==true)
                        {
                            if(NumCheck.isTrueHexDigit(lac)==true)
                            {
                                long value = Long.parseLong(lac,16);
                                lac= Long.toString(value);
                                if(NumCheck.isTrueHexDigit(cellid)==true)
                                {
                                    value = Long.parseLong(cellid,16);
                                    cellid= Long.toString(value);
                                    start_flg=true;
                                }
                                else
                                {
                                    AlrDialog_Show.alertDialog(context,"提示","请设置正确格式CELLID!","确定");
                                }
                            }
                            else
                            {
                                AlrDialog_Show.alertDialog(context,"提示","请设置正确格式LAC!","确定");
                            }
                        }
                        else
                        {
                            if(NumCheck.isNumeric(lac)==true)
                            {
                                if(NumCheck.isNumeric(cellid)==true)
                                {
                                    start_flg=true;
                                }
                                else
                                {
                                    AlrDialog_Show.alertDialog(context,"提示","请设置正确格式CELLID!","确定");
                                }
                            }
                            else
                            {
                                AlrDialog_Show.alertDialog(context,"提示","请设置正确格式LAC!","确定");
                            }
                        }
                    }
                }
            }
            else
            {
                AlrDialog_Show.alertDialog(context,"提示","请设置正确格式mnc!","确定");
            }
        }
        else
        {
            AlrDialog_Show.alertDialog(context,"提示","请设置正确格式mcc!","确定");
        }
        Log.v("sid1", sid);
        Log.v("nid1", nid);
        Log.v("bid1", bid);
        return start_flg;
    }
    public String read(File fileName){
        FileInputStream is = null;
        ByteArrayOutputStream bos=null;
        try {
            is = new FileInputStream(fileName);
            bos = new ByteArrayOutputStream();
            byte[] array = new byte[1024];
            int len = -1;
            while((len = is.read(array)) != -1){
                bos.write(array,0,len);
            }
            bos.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bos.toString();
    }
    public void writeMsgToFile(String msg) {
        String filePath = Environment.getExternalStorageDirectory().getPath()+ File.separator+"LuCeIp.txt";
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(filePath),true);
            OutputStreamWriter fos = new OutputStreamWriter(fileOutputStream, "GBK");
            fos.write(msg);
            fos.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    String[] parameters=new String[3];
    LuceCellInfo luceCellInfo=new LuceCellInfo();
    private void Request_Gps()//查询经纬度
    {
        String[] get_msg=new String[2];
        get_msg[0]="";
        get_msg[1]="";
        for(int i=0;i<3;i++)
        {
            parameters[i]="";
        }
        if(mode== dataCode.cdma)
        {
            parameters[0]=sid;
            parameters[1]=nid;
            parameters[2]=bid;
            luceCellInfo.setLac_sid(Integer.valueOf(sid));
            luceCellInfo.setCi_nid(Integer.valueOf(nid));
            luceCellInfo.setBid(Integer.valueOf(bid));
        }
        else
        {
            parameters[0]=lac;
            parameters[1]=cellid;
            luceCellInfo.setLac_sid(Integer.valueOf(lac));
            luceCellInfo.setCi_nid(Integer.valueOf(cellid));
        }
        if (showList.size()==0){
            luceCellInfo.setColor(0xff00ffff);
        }else if (showList.size()==1||showList.size()>1){
            if (showList.size()<7){
                luceCellInfo.setColor(color[showList.size()-1]);
            }else {
                luceCellInfo.setColor(color[showList.size()-7]);
            }
        }

        Map<String, String> params = new HashMap<>();
        params.put("mcc", mcc );
        params.put("mnc", mnc );
        params.put("p1", parameters[0] );
        params.put("p2", parameters[1] );
        params.put("p3", parameters[2] );
        OkHttpClient okHttpClient = new OkHttpClient();
        FormBody.Builder builder1 = new FormBody.Builder();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder1.add(entry.getKey(), entry.getValue());
            }
        }
        RequestBody requestBody =  builder1.build();
        Request.Builder builder = new Request.Builder();
//        builder.url(InterfaceUrl.BASEURL);
        String URL=interfaceUrl.getBASEURL();
        builder.url(URL);
        builder.post(requestBody);
        Call call  = okHttpClient.newCall(builder.build());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
//                Log.e("OKHttp","11111");
                Message msg = handler .obtainMessage(0,0,4,null);
                handler .sendMessage(msg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if(response.isSuccessful()){
                        InputStream is = response.body().byteStream();
                        int datalength = 0;
                        int reccount = 0;
                        boolean headread = false;
                        int sublen = -1;

                        byte[] bArr = new byte[1024 *6];
                        byte[] msgBuffer = new byte[0];

                        int size = is.read(bArr, 0, (int) bArr.length);
                        msgBuffer = ArrayUtils.append(msgBuffer, ArrayUtils.subArray(bArr, 0,size));
                        int i=0;
                        while (size > 0 || msgBuffer.length > 0) {
//                            int pointer_package = 0;
                            if (size > 0) {
                                size = is.read(bArr, 0, (int) bArr.length);
                            }
                            if (size > 0) {
                                msgBuffer = ArrayUtils.append(msgBuffer, ArrayUtils.subArray(bArr,0, size));
                            }

                            if (!headread && msgBuffer.length >= 4) {
                                byte[] alllength = ArrayUtils.subArray(msgBuffer, 0, 4);
                                datalength = (alllength[3] & 0xff) * 256 * 256 * 256
                                        + (alllength[2] & 0xff) * 256 * 256
                                        + (alllength[1] & 0xff) * 256
                                        + (alllength[0] & 0xff);
                                msgBuffer = ArrayUtils.subArray(msgBuffer, 4, msgBuffer.length - 4);
//                                pointer_package += 4;
                                headread = true;
                                if (size==-1&&datalength==0){
                                    Message message=new Message();
                                    message.arg1=2;
                                    handler1.sendMessage(message);
                                    break;
                                }
                            }
                            if (headread) {
                                //头长度已读
                                if (msgBuffer.length >= 4 && sublen < 0) {
                                    byte[] sublength = ArrayUtils.subArray(msgBuffer, 0, 4);
                                    sublen = (sublength[3] & 0xff) * 256 * 256 * 256
                                            + (sublength[2] & 0xff) * 256 * 256
                                            + (sublength[1] & 0xff) * 256
                                            + (sublength[0] & 0xff);
                                    msgBuffer = ArrayUtils.subArray(msgBuffer, 4, msgBuffer.length - 4);
//                                    pointer_package += 4;
                                }
                                else if (msgBuffer.length >= sublen&&sublen>0) {
                                    i++;
                                    byte[] subdata = ArrayUtils.subArray(msgBuffer, 0, sublen);
                                    msgBuffer = ArrayUtils.subArray(msgBuffer, sublen, msgBuffer.length - sublen);
                                    sublen = -1;
                                    //数据处理
                                    String json=new String(subdata);
                                    JSONObject object=new JSONObject(json);
                                    JSONArray array=object.getJSONArray("datalist");
                                    int count=array.length();


                                    Message msg = handler .obtainMessage(0,i,1,object);
                                    handler .sendMessage(msg);
                                }
                            }
                        }
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
//        Map<String, String> params = new HashMap<>();
//        params.put("mcc", mcc );
//        params.put("mnc", mnc );
//        params.put("p1", parameters[0] );
//        params.put("p2",  parameters[1] );
//        params.put("p3",  parameters[2] );
//        okHttpManager.postRequest(InterfaceUrl.BASEURL, new BaseCallBack() {
//            @Override
//            protected void OnRequestBefore(Request request) {
//                Log.e("ee","1234");
//            }
//
//            @Override
//            protected void onFailure(Call call, IOException e) {
//                Log.e("ee","1234");
//            }
//
//            @Override
//            protected void onSuccess(Call call, Response response, Object o) {
//                Log.e("ee","1234");
//            }
//
//            @Override
//            protected void onResponse(Response response) {
//                Log.e("ee","1234");
//            }
//
//            @Override
//            protected void onEror(Call call, int statusCode, Exception e) {
//                Log.e("ee","1234");
//            }
//
//            @Override
//            protected void inProgress(int progress, long total, int id) {
//                Log.e("ee","1234");
//            }
//        },params);
//        okHttpManager.getSingleLuce(mcc, mnc, parameters[0], parameters[1], parameters[2], new LoadCallBack(context) {
//            @Override
//            protected void onSuccess(Call call, Response response, Object o) {
//                Log.e("ee","1234");
//            }
//
//            @Override
//            protected void onEror(Call call, int statusCode, Exception e) {
//                Log.e("ee","1234");
//            }
//        });
    }
    private void getMncMcc(){
        if(mode_index== dataCode.cdma)
        {
            mnc = "11";
        }
        if (net == 0)
        {
            mode = mode_index;
            if (mode_index == dataCode.china_mobile) {
                mnc = "00";
            } else if (mode_index == dataCode.china_unicom) {
                mnc = "01";
            }
        }
        else if (net == 1)//3g
        {
            if (mode_index == dataCode.china_mobile) {
                mnc = "00";
                mode = dataCode.td_swcdma;
            } else if (mode_index == dataCode.china_unicom) {
                mode = dataCode.wcdma;
                mnc = "01";
            }
        }
        else if (net == 2)//4g
        {
            if (mode_index == dataCode.china_mobile) {
                mode = dataCode.tdd_lte;
                mnc = "00";
            } else if (mode_index == dataCode.china_unicom) {
                mode = dataCode.fdd_lte;
                mnc = "01";
            } else {
                mode = dataCode.fdd_lte;
                mnc = "01";
            }

        }
    }
    private LatLng end_latLng=null;
    private int[] color={0xffff0000,0xff00ff00,0xff0000ff,0xffffff00,0xff00ffff,0xffff00ff};
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.arg2==4){
                if (timerProgressDialog1!=null){
                    timerProgressDialog1.dismiss();
                }
                if (timerDialog!=null){
                    timerDialog.dismiss();
                }
                AlertDialog.Builder builder=new AlertDialog.Builder(context)
                        .setTitle("提示")
                        .setMessage("服务器连接失败，请稍后再试")
                        .setPositiveButton("确定",null);
                builder.show();
            }
            List<Point> list=new ArrayList<>();
            List<String> list_rssi=new ArrayList<>();
            List<LatLng> latLngs=new ArrayList<>();
            if (msg.arg1>0){
                JSONObject object= (JSONObject) msg.obj;
                try {
                    JSONArray array=object.getJSONArray("datalist");
                    for (int i=0;i<array.length();i++){
                        JSONObject jsonObject= (JSONObject) array.opt(i);
                        Point point=new Point();
                        String lon=jsonObject.getString("longdot");
                        String lat=jsonObject.getString("latdot");
                        point.setLat(lat);
                        point.setLon(lon);
                        final double[] latlon= GPSUtils.wgs2bd(Double.valueOf(lat),Double.valueOf(lon));
                        if (jsonObject.has("address")){
                            if (!(jsonObject.getString("address").equals("")||jsonObject.getString("address")==null)){
                                point.setAcc(jsonObject.getString("address"));
                            }
                        }
                        if (jsonObject.has("power")){
                            if (!(jsonObject.getString("power").equals("")||jsonObject.getString("power")==null)){
                                point.setRssi(jsonObject.getString("power"));
                            }
                        }
                        if (jsonObject.has("p1")){
                            if (!(jsonObject.getString("p1").equals("")||jsonObject.getString("p1")==null)){
                                point.setP1(jsonObject.getString("p1"));
                            }
                        }
                        if (jsonObject.has("p2")){
                            if (!(jsonObject.getString("p2").equals("")||jsonObject.getString("p2")==null)){
                                point.setP2(jsonObject.getString("p2"));
                            }
                        }
                        if (jsonObject.has("p3")){
                            if (!(jsonObject.getString("p3").equals("")||jsonObject.getString("p3")==null)){
                                point.setP3(jsonObject.getString("p3"));
                            }
                        }
                        double lat1=Double.valueOf(lat);
                        double lon1=Double.valueOf(lon);
                        if (jsonObject.has("acc")){
                            if (!((int)lat1==0&&(int)lon1==0)){
                                end_latLng=new LatLng(latlon[0],latlon[1]);
                                baiduMapUtil.addMarker(latlon[0],latlon[1],"基站信息："+parameters[0]+","+parameters[1]+","+parameters[2]+"\n"+"第三方数据",0f,0f,255f,1.0f);
                            }
                        }
                        else if (jsonObject.has("power")){
                            list_rssi.add(point.getRssi());
                            list.add(point);
                        }
                    }
                    if (list.size()>100){
                        int size=list.size()/100;
                        for (int i2=list.size()-1;i2<list.size()&&i2>=0;i2--){
                                if (!(i2 % size == 0)) {
                                    list.remove(i2);
                                    list_rssi.remove(i2);
                                }
                        }
                    }
                    //按强度渐变添加覆盖物
                    int max_rssi=Integer.valueOf(list_rssi.get(0));
                    int min_rssi=Integer.valueOf(list_rssi.get(0));
                    for (int i=0;i<list_rssi.size()-1;i++){
                        int current=Integer.valueOf(list_rssi.get(i));
                        if (current>max_rssi){
                            max_rssi=current;
                        } else if (current<min_rssi){
                            min_rssi=current;
                        }
                    }
                    Log.e("rssi",max_rssi+"   "+min_rssi+"");
                    showList_latLngs.add(list);
                    showList_rssi.add(list_rssi);
                    for (int i=0;i<list.size();i++){
                        int rssi_rgb;
                        if (Integer.valueOf(list.get(i).getRssi())-min_rssi==0||max_rssi-min_rssi==0){
                            rssi_rgb=0;
                        }else {
                            rssi_rgb=(Integer.valueOf(list.get(i).getRssi())-min_rssi)*100/(max_rssi-min_rssi);
                        }
                        final double[] latlon1= GPSUtils.wgs2bd(Double.valueOf(list.get(i).getLat()),Double.valueOf(list.get(i).getLon()));
                        if (!((int)latlon1[0]==0&&(int)latlon1[1]==0)){
                            baiduMapUtil.addMarker(latlon1[0],latlon1[1],"基站信息："+parameters[0]+","+parameters[1]+","+parameters[2]+"\n"
                                    +Double.valueOf(list.get(i).getRssi())+"",(100-rssi_rgb)*255/100 ,rssi_rgb*255/100,0f,1.0f);
                        }
                    }
                    for (int i=0;i<list.size();i++){
                        Point point= list.get(i);
                        double lat=Double.valueOf(point.getLat());
                        double lon=Double.valueOf(point.getLon());
                        if ((int)lat==0&&(int)lon==0){
                            list.remove(point);
                            i--;
                        }
                    }
                    ConvexHull convexHull=new ConvexHull(list);
                    List<Point> list_po=convexHull.calculateHull();
                    //垃框查询
                    if (list_po.size()>=3){
                        for (int i=0;i<list_po.size();i++){
                            Point point=list_po.get(i);
                            final double[] latlon= GPSUtils.wgs2bd(Double.valueOf(point.getLat()),Double.valueOf(point.getLon()));
                            LatLng latLng=new LatLng(latlon[0],latlon[1]);
                            latLngs.add(latLng);
                        }
                        if (showList.size()==0){
                            baiduMapUtil.draw_find(latLngs);
                        }else if (showList.size()==1||showList.size()>1){
                            OverlayOptions polygonOption=null;
                            if (showList.size()<7){
                                polygonOption = new PolygonOptions()
                                        .points(latLngs)
                                        .stroke(new Stroke(5, color[showList.size()-1]))//color[showList.size()-1]
                                        .fillColor(0);//0x80ffffff
                            }else {
                                polygonOption = new PolygonOptions()
                                        .points(latLngs)
                                        .stroke(new Stroke(5, color[showList.size()-7]))//color[showList.size()-7]
                                        .fillColor(0);
                            }
                            showList.add(polygonOption);
                            mBaiduMap.addOverlay(polygonOption);
                            baiduMapUtil.add_more_overlay(showList);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                luceCellInfos.add(luceCellInfo);
                Point p=new Point();
                p.setP1(String.valueOf(luceCellInfo.getLac_sid()));
                p.setP2(String.valueOf(luceCellInfo.getCi_nid()));
                p.setP3(String.valueOf(luceCellInfo.getBid()));
                list_neibor_latlon.add(p);
                for (int i=0;i<list_neibor_latlon.size();i++){
                    if ((list.get(2).getP1().equals(list_neibor_latlon.get(i).getP1()))&&(list.get(2).getP2().equals(list_neibor_latlon.get(i).getP2()))){
                        Point point=list_neibor_latlon.get(i);
                        point.setLat(list.get(2).getLat());
                        point.setLon(list.get(2).getLon());
                    }
                }
                lacDataAdapter.notifyDataSetChanged();
                luceCellInfo=new LuceCellInfo();
                if (timerDialog!=null){
                    timerDialog.dismiss();
                }
            }
        }
    };
    public void myPermission() {
        final int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        acquireWakeLock();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //放在onStart()方法里，保持屏幕常亮
    }
    /**
       * 获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
       */
    private void acquireWakeLock() {
        if (null == wakeLock) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                    | PowerManager.ON_AFTER_RELEASE, getClass()
                    .getCanonicalName());
            if (null != wakeLock) {
                wakeLock.acquire();
            }
        }
    }
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        showList.clear();
        marker=0;
        lac="";
        cellid="";
        sid="";
        nid="";
        bid="";
        area=false;
        options=null;
        marker_latlng.clear();
        marker_latlng_data.clear();
        list_curent_overlayoptions.clear();
        list_neibor_latlon.clear();
        showList_latLngs.clear();
        showList_rssi.clear();
        luceCellInfos.clear();
        lacDataAdapter.notifyDataSetChanged();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }
}
