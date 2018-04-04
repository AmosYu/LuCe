package luce.ctl.luce.BaiduMap;

import android.content.Context;

import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

/**
 * Created by admin on 2016/8/9.
 */
public class LocationService {


    private LocationClient client = null;
    private LocationClientOption mOption,DIYoption;
    private Object objLock = new Object();
    /**
     *
     * @param locationContext
     */
    public LocationService(Context locationContext){
        synchronized (objLock) {
            if(client == null){
                client = new LocationClient(locationContext);
                client.setLocOption(getDefaultLocationClientOption());
            }
        }
    }

    /***
     *
     * @param listener
     * @return
     */

    public boolean registerListener(BDLocationListener listener){
        boolean isSuccess = false;
        if(listener != null){
            client.registerLocationListener(listener);
            isSuccess = true;
        }
        return  isSuccess;
    }

    public void unregisterListener(BDLocationListener listener){
        if(listener != null){
            client.unRegisterLocationListener(listener);
        }
    }

    /***
     *
     * @param option
     * @return isSuccessSetOption
     */
    public boolean setLocationOption(LocationClientOption option){
        boolean isSuccess = false;
        if(option != null){
            if(client.isStarted())
                client.stop();
            DIYoption = option;
            client.setLocOption(option);
        }
        return isSuccess;
    }

    public LocationClientOption getOption(){
        return DIYoption;
    }
    /***
     *
     * @return DefaultLocationClientOption
     */
    public LocationClientOption getDefaultLocationClientOption(){
        if(mOption == null){
            mOption = new LocationClientOption();
            mOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
            mOption.setCoorType("bd09ll");
            mOption.setScanSpan(1000);
            mOption.setIsNeedAddress(true);
            mOption.setIsNeedLocationDescribe(true);
            mOption.setNeedDeviceDirect(false);
            mOption.setLocationNotify(false);
            mOption.setIgnoreKillProcess(true);
            mOption.setIsNeedLocationDescribe(true);
            mOption.setIsNeedLocationPoiList(true);
            mOption.SetIgnoreCacheException(false);
            mOption.setPriority(LocationClientOption.GpsFirst);
            mOption.setOpenGps(true);
        }
        return mOption;
    }

    public void start(){
        synchronized (objLock) {
            if(client != null && !client.isStarted()){
                client.start();
            }
        }
    }

    public void ReGet(){
        synchronized (objLock) {
            if(client != null && client.isStarted()){
                client.requestLocation();
            }
        }
    }
    public void stop(){
        synchronized (objLock) {
            if(client != null && client.isStarted()){
                client.stop();
                client=null;
            }
        }
    }

}

