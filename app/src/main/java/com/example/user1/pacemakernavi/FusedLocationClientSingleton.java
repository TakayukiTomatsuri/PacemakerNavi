package com.example.user1.pacemakernavi;

import android.app.Application;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user1 on 2017/05/01.
 */

//Applicationの拡張クラスなことに注意
//このアプリ全体での位置情報更新を行わせるためにシングルトンパターン。(=インスタンスが一つしか存在しない)
public class FusedLocationClientSingleton extends Application implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static FusedLocationClientSingleton sInstance = null;   //唯一のインスタンスを保持する
    private LocationRequest locationRequest;    //ロケーション更新要請の情報みたいなもの
    private FusedLocationProviderApi fusedLocationProviderApi;

    // LocationClient の代わりにGoogleApiClientを使います
    private GoogleApiClient mGoogleApiClient;

    private boolean mResolvingError = false;    //GoogleApiClientのコネクション確立失敗時のフラグ

    private List<LocationListener> listeners = new ArrayList<LocationListener>(10); //位置情報更新を受けたいリスナーたち

    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;

        // LocationRequest を生成して精度、インターバルを設定
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(16);

        fusedLocationProviderApi = LocationServices.FusedLocationApi;

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        startFusedLocation();
    }

    //シングルトンパターンでは、インスタンスはこのクラスメソッドを介して行う
    public static FusedLocationClientSingleton getInstance() {
        if (sInstance == null) {
            Log.e("FusedLocationClient", "Singleton instance is not generated.");
        }
        return sInstance;
    }

    private void startFusedLocation() {
        Log.d("FusedLocationSingleton", "onStart");

        // Connect the client.
        if (!mResolvingError) {
            // Connect the client.
            mGoogleApiClient.connect();
        } else {
        }
    }

    //位置情報取得を止めたいときはこれを呼ぶ
    //注意点として、Applicationの拡張クラスなので、終了するまでバックグラウンドでも位置情報を更新し続けると思う(ActivityやFragmentならリソース不足などで中断もありうる)
    private void stopFusedLocation() {
        // Disconnecting the client invalidates it.
        mGoogleApiClient.disconnect();
    }


    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("LocationClient", "ConnectionFailed!");
        if (mResolvingError) {
            // Already attempting to resolve an error.
            Log.d("", "Already attempting to resolve an error");

            return;
        } else if (connectionResult.hasResolution()) {

        } else {
            mResolvingError = true;
        }
    }

    //位置が変わったら呼ばれるメソッド
    @Override
    public void onLocationChanged(Location location) {
        Log.d("FusedLocationClient", "onLocationChanged!");
        //登録してある全部のリスナーについて、コールバックメソッドを呼んで位置情報を渡してあげる
        for (LocationListener listener : listeners) {
            listener.onLocationChanged(location);
        }
    }

    //リスナーの追加はこれ
    public void addListner(LocationListener listener) {
        listeners.add(listener);
    }


}
