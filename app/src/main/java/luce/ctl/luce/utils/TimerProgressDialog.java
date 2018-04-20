package luce.ctl.luce.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;


public class TimerProgressDialog extends ProgressDialog {
    private Handler dialogHandler;
    private StringBuffer strBuffer = null;
    /**
     * 提示对话框，定时消失
     * @param context
     * @param duration 持续时间
     * @param title    标题
     * @param message  提示内容
     */
    public TimerProgressDialog(Context context, final long duration, final long num_single, String title, String message){
        super(context);
        dialogHandler = new Handler();
        strBuffer = new StringBuffer();
        strBuffer.append(message);
        setCanceledOnTouchOutside(false);
        setTitle(title);
        setMessage(message);
        setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        setProgress(0);
        setMax((int)(duration/num_single));
        setProgressNumberFormat("%1d/%2d");
        new Thread() {
            @Override
            public void run() {
                int secs = (int) ((int)duration/num_single);
                int progress = 0;
                try {
                    while (progress<secs) {
                        setProgress(progress);
                        Thread.sleep(1000);
                        progress++;
                    }
                    dismiss();
                } catch (Exception e) {
                    dismiss();
                }
            }
        }.start();
    }
    public TimerProgressDialog(Context context, final long duration, String title, String message){
        super(context);
        dialogHandler = new Handler();
        strBuffer = new StringBuffer();
        strBuffer.append(message);
        setCanceledOnTouchOutside(false);
        setTitle(title);
        setMessage(message);
        setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        setProgress(0);
        setMax((int)(duration/1000));
        setProgressNumberFormat("%1d/%2d");
        new Thread() {
            @Override
            public void run() {
                int secs = (int) ((int)duration/1000);
                int progress = 0;
                try {
                    while (progress<secs) {
                        setProgress(progress);
                        Thread.sleep(1000);
                        progress++;
                    }
                    dismiss();
                } catch (Exception e) {
                    dismiss();
                }
            }
        }.start();
    }
    public void updateMsg(final String msg){
        dialogHandler.post(new Runnable() {
            @Override
            public void run() {

                    strBuffer.append("\n"+msg);
                    setMessage(strBuffer.toString());

            }
        });
    }
}
