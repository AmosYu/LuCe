package luce.ctl.luce.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import luce.ctl.luce.BaiduMap.BaiduMapUtil;
import luce.ctl.luce.MainActivity;
import luce.ctl.luce.R;
import luce.ctl.luce.ui.LuceCellInfo;


/**
 * Created by on 16/5/15.
 */
public class LacDataAdapter extends CommonAdapter<LuceCellInfo> {

    private List<LuceCellInfo> datas;
    public LacDataAdapter(Context context, List<LuceCellInfo> datas, int layoutId) {
        super(context, datas, layoutId);
        this.datas=datas;

    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = ViewHolder.get(mContext, convertView, parent, R.layout.cell_listview_item, position);
        LuceCellInfo cellInfo = datas.get(position);
        ((TextView)holder.getView(R.id.dilog_cell_lac)).setText(String.valueOf(cellInfo.getLac_sid()));
        ((TextView)holder.getView(R.id.dilog_cell_ci)).setText( String.valueOf(cellInfo.getCi_nid()));
        ((TextView)holder.getView(R.id.dilog_cell_bid)).setText( String.valueOf(cellInfo.getBid()));
        ((TextView)holder.getView(R.id.dilog_cell_lac)).setTextColor(datas.get(position).getColor());
        ((TextView)holder.getView(R.id.dilog_cell_ci)).setTextColor(datas.get(position).getColor());
        ((TextView)holder.getView(R.id.dilog_cell_bid)).setTextColor(datas.get(position).getColor());
//        if (MainActivity.showList.size()==0){
//            ((TextView)holder.getView(R.id.dilog_cell_lac)).setTextColor(0xff00ffff);
//            ((TextView)holder.getView(R.id.dilog_cell_ci)).setTextColor(0xff00ffff);
//            ((TextView)holder.getView(R.id.dilog_cell_bid)).setTextColor(0xff00ffff);
//        }else if (MainActivity.showList.size()==1||MainActivity.showList.size()>1){
//            if (MainActivity.showList.size()<7){
//                ((TextView)holder.getView(R.id.dilog_cell_lac)).setTextColor(color[MainActivity.showList.size()-1]);
//                ((TextView)holder.getView(R.id.dilog_cell_ci)).setTextColor(color[MainActivity.showList.size()-1]);
//                ((TextView)holder.getView(R.id.dilog_cell_bid)).setTextColor(color[MainActivity.showList.size()-1]);
//            }else {
//                ((TextView)holder.getView(R.id.dilog_cell_lac)).setTextColor(color[MainActivity.showList.size()-7]);
//                ((TextView)holder.getView(R.id.dilog_cell_ci)).setTextColor(color[MainActivity.showList.size()-7]);
//                ((TextView)holder.getView(R.id.dilog_cell_bid)).setTextColor(color[MainActivity.showList.size()-7]);
//            }
//        }
        return holder.getConvertView();
    }

    @Override
    public void convert(ViewHolder holder, LuceCellInfo cellInfo) {

        holder.setText(R.id.dilog_cell_lac, String.valueOf(cellInfo.getLac_sid()))
                .setText(R.id.dilog_cell_ci, String.valueOf(cellInfo.getCi_nid()))
                .setText(R.id.dilog_cell_bid, String.valueOf(cellInfo.getBid()));
    }
}
