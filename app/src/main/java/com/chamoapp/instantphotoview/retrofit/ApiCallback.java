package com.chamoapp.instantphotoview.retrofit;

import android.util.Log;

import retrofit2.adapter.rxjava.HttpException;
import rx.Subscriber;

/**
 * Created by Koo on 2016. 11. 1..
 */

public abstract class ApiCallback<M> extends Subscriber<M> {
    private final static String TAG = "ApiCallback";
    public abstract void onSuccess(M model);
    public abstract void onFailure(String msg);
    public abstract void onFinish();

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
        if (e instanceof HttpException) {
            HttpException httpException = (HttpException) e;
            int code = httpException.code();
            String msg = httpException.getMessage();

            Log.d(TAG, String.format("code=%s, message=%s", code, msg));

            if (code == 500) {
                msg = "500 Internal Server Error";
            } else if (code == 404) {
                msg = "404 Page Not Found";
            }
            onFailure(msg);

        } else {
            onFailure(e.getMessage());
        }
        onFinish();
    }

    @Override
    public void onNext(M model) {
        onSuccess(model);

    }

    @Override
    public void onCompleted() {
        onFinish();
    }
}
