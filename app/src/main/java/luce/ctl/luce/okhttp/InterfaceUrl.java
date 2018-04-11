package luce.ctl.luce.okhttp;

/**
 * Created by CTL on 2018/4/8.
 */

public class InterfaceUrl {
    private static String ip="";
    public InterfaceUrl(String Ip) {
        this.ip=Ip;
    }
    public static final String BASEURL = "http://"+ip+"/collect/DataDLServlet";
}
