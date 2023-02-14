package com.example.esp32dht.Tools;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class ToastMaker {
    private final Context context;
    public ToastMaker(Context context) {
        this.context = context;
    }

    public void msg(){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(context, "This part runs", Toast.LENGTH_SHORT);
                toast.show();
/*                ViewGroup group = (ViewGroup) toast.getView();
                TextView messageTextView = (TextView) group.getChildAt(0);
                Typeface typeface = ResourcesCompat.getFont(context, R.font.iranian_sans);
                messageTextView.setTypeface(typeface);
                toast.show();*/
            }
        });
    }
    public void msg(final String msg){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
                toast.show();
                /*ViewGroup group = (ViewGroup) toast.getView();
                TextView messageTextView = (TextView) group.getChildAt(0);
                Typeface typeface = ResourcesCompat.getFont(context, R.font.iranian_sans);
                messageTextView.setTypeface(typeface);
                toast.show();*/
            }
        });
    }
    public void msg(final float message){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(context, String.valueOf(message), Toast.LENGTH_SHORT);
                toast.show();
/*                ViewGroup group = (ViewGroup) toast.getView();
                TextView messageTextView = (TextView) group.getChildAt(0);
                Typeface typeface = ResourcesCompat.getFont(context, R.font.iranian_sans);
                messageTextView.setTypeface(typeface);
                toast.show();*/
            }
        });
    }
}
