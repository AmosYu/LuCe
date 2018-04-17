package luce.ctl.luce.ui;

import java.util.ArrayList;
import java.util.List;

import luce.ctl.luce.utils.Point;

/**
 * Created by CTL on 2018/4/13.
 */

public class LuCeDataList {
    private String lac_sid="";
    private String ci_nid="";
    private String bid="";

    private int color=0;
    private List<Point> list=new ArrayList<>();

    public String getLac_sid() {
        return lac_sid;
    }

    public void setLac_sid(String lac_sid) {
        this.lac_sid = lac_sid;
    }

    public String getCi_nid() {
        return ci_nid;
    }

    public void setCi_nid(String ci_nid) {
        this.ci_nid = ci_nid;
    }

    public String getBid() {
        return bid;
    }

    public void setBid(String bid) {
        this.bid = bid;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public List<Point> getList() {
        return list;
    }

    public void setList(List<Point> list) {
        this.list = list;
    }
}
