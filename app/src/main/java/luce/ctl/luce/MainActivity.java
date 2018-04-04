package luce.ctl.luce;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import luce.ctl.luce.BaiduMap.BaiduMapUtil;

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

    int mode_index=0;
    BaiduMapUtil baiduMapUtil;
    public static List<OverlayOptions> showList=new ArrayList<>();
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
        initMap();
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

            }
        });
        clear_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mBaiduMap!=null)
                {
                    mBaiduMap.clear();
//                    showList.clear();
//                    luceCellInfos.clear();
//                    lacDataAdapter.notifyDataSetChanged();
                }
            }
        });
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

    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
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
