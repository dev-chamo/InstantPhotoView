package com.chamoapp.instantphotoview;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.chamoapp.instantphotoview.data.Photo;
import com.chamoapp.instantphotoview.data.ResultData;
import com.chamoapp.instantphotoview.retrofit.ApiCallback;
import com.chamoapp.instantphotoview.retrofit.ApiStores;
import com.chamoapp.instantphotoview.retrofit.AppClient;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends AppCompatActivity implements PhotoClickListener, View.OnClickListener {

    private static final String TAG = "MainActivity";

    private Context mContext;

    private ApiStores mApiStores = AppClient.retrofit().create(ApiStores.class);

    private RelativeLayout mInstantView;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private CompositeSubscription mCompositeSubscription;
    private int mPage = 1;

    private List<Photo> mPhotoList;

    private WindowManager mWindowManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new GridLayoutManager(mContext, 3);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mPhotoList = new ArrayList<>();
        mAdapter = new PhotoViewListAdapter(mContext, mPhotoList);
        mRecyclerView.setAdapter(mAdapter);

        loadPhotos();
    }

    @Override
    protected void onDestroy() {
        onUnsubscribe();
        super.onDestroy();
    }

    private void loadPhotos() {
        showProgressDialog();
        addSubscription(mApiStores.loadRecentPhotos(mPage), new ApiCallback<ResultData>() {
            @Override
            public void onSuccess(ResultData resultData) {
                mPhotoList.addAll(resultData.getPhotos().getPhoto());
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String msg) {

            }

            @Override
            public void onFinish() {
                dismissProgressDialog();
            }
        });
    }

    private void addSubscription(Observable observable, Subscriber subscriber) {
        if (mCompositeSubscription == null) {
            mCompositeSubscription = new CompositeSubscription();
        }
        mCompositeSubscription.add(observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber));
    }


    private void onUnsubscribe() {
        if (mCompositeSubscription != null && mCompositeSubscription.hasSubscriptions()) {
            mCompositeSubscription.unsubscribe();
        }
    }


    private ProgressDialog progressDialog;

    private ProgressDialog showProgressDialog() {
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        return progressDialog;
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void applyBobbleAnim(View targetView) {
        AnimationSet bobbleAnimSet = new AnimationSet(true);
        ScaleAnimation expand = new ScaleAnimation(
                0.8f, 1.0f,
                0.8f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        expand.setDuration(300);

        bobbleAnimSet.addAnimation(expand);
        bobbleAnimSet.setFillAfter(true);
        bobbleAnimSet.setInterpolator(new OvershootInterpolator());

        targetView.startAnimation(bobbleAnimSet);
    }

    private boolean removeInstantView(){
        if(mInstantView != null){
            mInstantView.clearAnimation();
            mWindowManager.removeView(mInstantView);
            mInstantView = null;
            return true;
        }
        return false;
    }

    private long mBackPressedTime = 0;

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() < mBackPressedTime + 2000) {
            super.onBackPressed();

        } else {
            if (!removeInstantView()) {
                mBackPressedTime = System.currentTimeMillis();
                Toast.makeText(mContext, getResources().getString(R.string.back_pressed_message), Toast.LENGTH_LONG).show();
            }
        }
    }


    private Observable<Boolean> getInstantViewObservable(final View instantView, final Photo photo) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                final RequestListener<String, GlideDrawable> requestListener = new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        subscriber.onNext(true);
                        return false;
                    }
                };

                addSubscription(mApiStores.loadUserInfo(photo.getOwner()), new ApiCallback<ResultData>() {
                    @Override
                    public void onSuccess(ResultData resultData) {
                        ResultData.Person person = resultData.getPerson();
                        TextView userNameTv = (TextView) instantView.findViewById(R.id.photo_detail_user_name_tv);
                        userNameTv.setText(person.getRealname() != null ? person.getRealname() : person.getUsername());

                        ImageView coverIv = (ImageView) instantView.findViewById(R.id.photo_detail_cover_iv);
                        String url = String.format("http://farm%s.staticflickr.com/%s/%s_%s.jpg", photo.getFarm(), photo.getServer(), photo.getId(), photo.getSecret());

                        Glide.with(mContext)
                                .load(url)
                                .listener(requestListener)
                                .into(coverIv);

                        ImageView userProfileIv = (ImageView) instantView.findViewById(R.id.photo_detail_user_profile_iv);
                        String profileImageUrl = String.format("https://farm%s.staticflickr.com/%s/buddyicons/%s.jpg", person.getIconfarm(), person.getIconserver(), person.getNsid());

                        Glide.with(mContext)
                                .load(profileImageUrl)
                                .bitmapTransform(new CropCircleTransformation(mContext))
                                .listener(requestListener)
                                .into(userProfileIv);
                    }

                    @Override
                    public void onFailure(String msg) {

                    }

                    @Override
                    public void onFinish() {

                    }
                });
            }
        });
    }

    int mObserverSuccessCount = 0;
    int mObserverFailCount = 0;

    @Override
    public void onPhotoClick(Photo photo) {
        mInstantView = (RelativeLayout) LayoutInflater.from(mContext).inflate(R.layout.photo_detail_view, null);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.CENTER;
        mWindowManager.addView(mInstantView, params);

        mInstantView.setVisibility(View.INVISIBLE);

        View backView = mInstantView.findViewById(R.id.photo_detail_back_view);
        backView.setOnClickListener(this);

        getInstantViewObservable(mInstantView, photo)
                .subscribeOn(Schedulers.trampoline())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {
                        mInstantView.setVisibility(View.VISIBLE);

                        View frontView = mInstantView.findViewById(R.id.photo_detail_front_view);
                        applyBobbleAnim(frontView);

                        mObserverSuccessCount = 0;
                        mObserverFailCount = 0;
                    }

                    @Override
                    public void onError(Throwable e) {
                        removeInstantView();
                    }

                    @Override
                    public void onNext(Boolean result) {
                        if (result) {
                            mObserverSuccessCount++;
                        } else {
                            mObserverFailCount++;
                        }

                        if (mObserverSuccessCount + mObserverFailCount == 2) {
                            onCompleted();
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.photo_detail_back_view) {
            removeInstantView();
        }
    }
}
