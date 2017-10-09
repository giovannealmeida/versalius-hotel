package br.com.versalius.checkhotel.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import br.com.versalius.checkhotel.R;

/**
 * Created by jn18 on 13/01/2017.
 */
public class ProgressBarHelper {
    private ProgressBar progressBar;
    private Context context;
    private ViewGroup form;

    public ProgressBarHelper(Context context) {
        this.context = context;
    }

    public void createProgressSpinner(String title, String message, boolean indeterminate, boolean cancelable) {
        progressBar = new ProgressBar(context,null,android.R.attr.progressBarStyleLarge);
        progressBar.setIndeterminate(indeterminate);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(context, R.color.colorAccent), android.graphics.PorterDuff.Mode.MULTIPLY);

        form = (ViewGroup) ((AppCompatActivity) context).findViewById(R.id.form);
        form.setVisibility(View.GONE);

        ViewGroup layout = (ViewGroup) ((AppCompatActivity) context).findViewById(android.R.id.content).getRootView();
        RelativeLayout rl = new RelativeLayout(context);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        rl.addView(progressBar,params);
        rl.requestFocus();

        layout.addView(rl);
    }

//    public void createProgressBar(String title, String message, int maxProgress, boolean cancelable) {
//        progressBar = new ProgressBar(context);
//        progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//        progressBar.setTitle(title);
//        progressBar.setMessage(message);
//        progressBar.setProgress(0);
//        progressBar.setMax(maxProgress);
//        progressBar.setCancelable(cancelable);
//        progressBar.show();
//    }
//
//    public void incrementProgressBy(int inc) {
//        progressBar.incrementProgressBy(inc);
//    }

    public void dismiss() {
        progressBar.setVisibility(View.GONE);
        form.setVisibility(View.VISIBLE);
    }
}
