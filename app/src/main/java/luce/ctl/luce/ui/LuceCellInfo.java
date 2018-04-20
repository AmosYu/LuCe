package luce.ctl.luce.ui;

import com.baidu.mapapi.model.LatLng;

import java.io.Serializable;

import luce.ctl.luce.utils.Gps2BaiDu;


/**
 * Created by yu on 2016/11/9.
 */

public class LuceCellInfo implements Serializable {
    /**
     * LAC或SID  cdma代表SID
     */
    private int lac_sid =0 ;
    /**
     * CI 或NID cdma中代表NID
     */
    private int ci_nid = 0;
    /**
     * cdma中有效，其它制式无效，默认为零
     */
    private int bid = 0;

    /**
     * 数据采集时间
     */
    private String time;
    private int size=0;

    /**
     * cdma中表示pn gsm中表示bsic LTE中表示PCI WCDMA和TD中表示 PSC
     */
    private int pn_bsic_pci_psc = 0;
    /**
     * 频点
     */
    private int arfcn = 0;
    /**
     * 经度,gps
     */
    private double longitude = 0.0;
    /**
     * 纬度 gps
     */
    private double latitude = 0.0;
    /**
     * 基站信号的场强
     */
    private int rssi = 0;
    /**
     * 代表运营商代码
     */
    private int plmn = 0;
    /**
     * 网络制式，详细请参考CellMode
     */
    private int mode = 0;

    /**
     * 从百度地图中获取到的地址
     */
    private String address ;
    /**
     * 用户提供标记
     */
    private String userRemark ;

    /**
     * 基站类型
     */
    private String btsType = "";

    private String cellType="";

    public String band = "";

    private int color=0;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public LatLng getBaiduPoint(){
        return Gps2BaiDu.gpsToBaidu(latitude, longitude);
    }

    public int getLac_sid() {
        return lac_sid;
    }

    public void setLac_sid(int lac_sid) {
        this.lac_sid = lac_sid;
    }

    public int getCi_nid() {
        return ci_nid;
    }

    public void setCi_nid(int ci_nid) {
        this.ci_nid = ci_nid;
    }

    public int getBid() {
        return bid;
    }

    public void setBid(int bid) {
        this.bid = bid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getPn_bsic_pci_psc() {
        return pn_bsic_pci_psc;
    }

    public void setPn_bsic_pci_psc(int pn_bsic_pci_psc) {
        this.pn_bsic_pci_psc = pn_bsic_pci_psc;
    }

    public int getArfcn() {
        return arfcn;
    }

    public void setArfcn(int arfcn) {
        this.arfcn = arfcn;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int getPlmn() {
        return plmn;
    }

    public void setPlmn(int plmn) {
        this.plmn = plmn;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setUserRemark(String userRemark) {
        this.userRemark = userRemark;
    }

    public String getUserRemark() {
        return userRemark;
    }

    public String getBtsType() {
        return btsType;
    }

    public void setBtsType(String btsType) {
        this.btsType = btsType;
    }

    public String getCellType() {
        return cellType;
    }

    public void setCellType(String cellType) {
        this.cellType = cellType;
    }

    public String getBand() {
        return band;
    }

    public void setBand(String band) {
        this.band = band;
    }
}
