package luce.ctl.luce;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;

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
import luce.ctl.luce.ui.LuceCellInfo;
import luce.ctl.luce.utils.AlrDialog_Show;
import luce.ctl.luce.utils.ArrayUtils;
import luce.ctl.luce.utils.GPSUtils;
import luce.ctl.luce.utils.NumCheck;
import luce.ctl.luce.utils.Point;
import luce.ctl.luce.utils.TimerProgressDialog;
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
    private String[] modes={"中国移动","中国联通","中国电信2G","中国电信4G"};
    private ArrayAdapter mncAdapter;

    private TextView lac_text;
    private TextView cell_text;
    private LinearLayout bid_liner;
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
    private OkHttpManager okHttpManager;
    public static List<OverlayOptions> showList=new ArrayList<>();
    public static List<Overlay> overlays=new ArrayList<>();
    private static String ip="";
    private Handler handler;
    private List<Object> jsonList=new ArrayList<>();
    private List<Point> list_neibor_latlon=new ArrayList<>();//用来保存查询到的每个基站的其中一个数据
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.main);

        context=this;
        okHttpManager=OkHttpManager.getInstance();
        initView();
        bindEvent();
    }


    private void initView() {
        mMapView=(MapView) findViewById(R.id.bmapView);
        initModeSpinner();
        lac_text=(TextView)findViewById(R.id.lac_text_id);
        cell_text=(TextView)findViewById(R.id.cell_text_id);
        bid_liner=(LinearLayout)findViewById(R.id.bid_liner);
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
    }

    private void bindEvent() {
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
                ip="192.168.1.112";
                if (file.exists()){
                    byte[] bytes=read(file);
//                    ip=bytes.toString();
                    ip="192.168.1.112";
                }
//                ip=SharedPreferencesHandler.getDataFromPref(context,"luce_ip","");
                if(!(ip.equals("")||ip=="")){
                    InterfaceUrl url=new InterfaceUrl(ip);
                    if(start_flg==true)
                    {
                        if (find()){
                            AlertDialog.Builder builder=new AlertDialog.Builder(context)
                                    .setTitle("提示")
                                    .setMessage("您刚刚已经查询过该基站，可在左侧列表中查看")
                                    .setPositiveButton("确定",null);
                            builder.show();
                        }else {
                            new Thread(new Runnable() {
                                public void run() {
                                    Request_Gps();
                                }
                            }).start();
                            TimerProgressDialog timerDialog = new TimerProgressDialog(context,10000,"开始读取数据","正在读取数据请稍等");
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
                builder.show();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final double[] latlon= GPSUtils.wgs2bd(Double.valueOf(list_neibor_latlon.get(i).getLat()),Double.valueOf(list_neibor_latlon.get(i).getLon()));
                LatLng cenpt = new LatLng(latlon[0],latlon[1]);
                //定义地图状态
                MapStatus mMapStatus = new MapStatus.Builder()
                        .target(cenpt)
                        .zoom(15)
                        .build();
                //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化

                MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
                //改变地图状态
                mBaiduMap.setMapStatus(mMapStatusUpdate);
                for (int j=0;j<overlays.size();j++){
                    if (!(j==i)){
                        overlays.get(j).setVisible(false);
                    }
                }
                mMapView.refreshDrawableState();
            }
        });
        area_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder=new AlertDialog.Builder(context)
                        .setTitle("提示")
                        .setMessage("请在地图上选择一个或两个区域标记点")
                        .setPositiveButton("确定",null);
                builder.show();
            }
        });
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
    private AdapterView.OnItemSelectedListener dModeSelectLis = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            // btsCtrl.setDetectMode(position);
            mode_index=position;
            if(mode_index==2)
            {
                lac_text.setText("SID");
                cell_text.setText("NID");
                bid_liner.setVisibility(View.VISIBLE);
            }
            else
            {
                lac_text.setText("LAC");
                cell_text.setText("CELL");
                bid_liner.setVisibility(View.GONE);

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
        nid = cell_edit.getText().toString().trim();
        bid = bid_edit.getText().toString().trim();
        if(mode_index== dataCode.cdma)
        {
//            lac_text.setText("SID:");
            if(net==2)
            {
                mnc = "11";
            }
            else
            {
                mnc = "03";
            }
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
    public byte[] read(File fileName){
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
        return bos.toByteArray();
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
            parameters[1]=bid;
            parameters[2]=nid;
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
        Looper looper = Looper.getMainLooper(); //主线程的Looper对象
        handler = new MyHandler(looper);

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
        builder.url("http://"+ip+"/collect/DataDLServlet");
        builder.post(requestBody);
        Call call  = okHttpClient.newCall(builder.build());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
//                Log.e("OKHttp","11111");
                Message msg = handler .obtainMessage(1,0,4,null);
                handler .sendMessage(msg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONArray array=new JSONArray();
                    List<String> list_rssi=new ArrayList<>();
                    List<LatLng> latLngs=new ArrayList<>();
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
                                byte[] alllength = ArrayUtils.subArray(bArr, 0, 4);
                                datalength = (alllength[3] & 0xff) * 256 * 256 * 256
                                        + (alllength[2] & 0xff) * 256 * 256
                                        + (alllength[1] & 0xff) * 256
                                        + (alllength[0] & 0xff);
                                msgBuffer = ArrayUtils.subArray(msgBuffer, 4, msgBuffer.length - 4);
//                                pointer_package += 4;
                                headread = true;
                            }
                            if (headread) {
                                //头长度已读
                                if (msgBuffer.length >= 4 && sublen < 0) {
                                    byte[] sublength = ArrayUtils.subArray(bArr, 0, 4);
                                    sublen = (sublength[3] & 0xff) * 256 * 256 * 256
                                            + (sublength[2] & 0xff) * 256 * 256
                                            + (sublength[1] & 0xff) * 256
                                            + (sublength[0] & 0xff);
                                    msgBuffer = ArrayUtils.subArray(msgBuffer, 4, msgBuffer.length - 4);
//                                    pointer_package += 4;
                                }
                                if (msgBuffer.length >= sublen&&sublen>0) {
                                    i++;
                                    byte[] subdata = ArrayUtils.subArray(msgBuffer, 0, sublen);
                                    msgBuffer = ArrayUtils.subArray(msgBuffer, sublen, msgBuffer.length - sublen);
                                    sublen = -1;
                                    //数据处理
                                    String json=new String(subdata);
                                    JSONObject object=new JSONObject(json);

                                    Message msg = handler .obtainMessage(datalength,i,1,object);
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
    private LatLng end_latLng=null;
    private int[] color={0xffff0000,0xff00ff00,0xff0000ff,0xffffff00,0xff00ffff,0xffff00ff};
    class MyHandler extends Handler{

        public MyHandler(Looper looper){
            super(looper);
        }

        public void handleMessage(Message msg){
            super.handleMessage(msg);
            if (msg.arg2==4){
                Toast.makeText(context,"服务器连接失败，请稍后再试",Toast.LENGTH_SHORT).show();
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
                    for (int i=0;i<list.size();i++){
                        int rssi_rgb=(Integer.valueOf(list.get(i).getRssi())-min_rssi)*100/(max_rssi-min_rssi);
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
                            overlays.add(mBaiduMap.addOverlay(polygonOption));
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
            }
        }
    }
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        ip="";
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
