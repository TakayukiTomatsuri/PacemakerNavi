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

public class FusedLocationClientSingleton extends Application implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    //シングルトン！
    private static FusedLocationClientSingleton sInstance = null;
    private LocationRequest locationRequest;
    private FusedLocationProviderApi fusedLocationProviderApi;

    // LocationClient の代わりにGoogleApiClientを使います
    private GoogleApiClient mGoogleApiClient;

    private boolean mResolvingError = false;

    private List<LocationListener> listeners = new ArrayList<LocationListener>(10);

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("FusedLocationSingleton", "onCerate");
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

    public static FusedLocationClientSingleton getInstance() {
        Log.d("FusedLocationClient", "getInstance");
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
        //登録してある全部のリスナーについて、コールバックメソッドを呼んであげる
        for (LocationListener listener : listeners) {
            listener.onLocationChanged(location);
        }
    }

    public void addListner(LocationListener listener) {
        listeners.add(listener);
    }


}
