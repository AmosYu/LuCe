package luce.ctl.luce.okhttp;

import android.content.Context;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by CTL on 2018/4/9.
 */

public abstract class LoadCallBack<T> extends BaseCallBack<T> {
    private Context context;

    public LoadCallBack(Context context) {
        this.context = context;
    }


    @Override
    protected void OnRequestBefore(Request request) {

    }

    @Override
    protected void onFailure(Call call, IOException e) {
    }

    @Override
    protected void onResponse(Response response) {
    }

    @Override
    protected void inProgress(int progress, long total, int id) {

    }
}
