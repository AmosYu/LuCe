package luce.ctl.luce.okhttp;

/**
 * Created by CTL on 2018/4/8.
 */

public class InterfaceUrl {
    private String ip="";
    public InterfaceUrl(String Ip) {
        this.ip=Ip;
    }
    public String BASEURL = "";

    public String getBASEURL() {
        String url="http://"+ip+"/collect/DataDLServlet";
        BASEURL=url;
        return BASEURL;
    }
}
