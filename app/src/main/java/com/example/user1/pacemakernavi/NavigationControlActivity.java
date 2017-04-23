package com.example.user1.pacemakernavi;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.internal.PlaceEntity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by user1 on 2017/04/22.
 */


//ナビゲーション画面のコントローラとなるアクティビティです
//位置情報の監視、及び、設定された目的地と出発地からルートを検索して、情報をNavigationMapFragmentとNavigationInformationFragmentに渡します。
public class NavigationControlActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    LatLng destination;
    LatLng origin;
    NavigationMapFragment navigationMapFragment;
    NavigationInformationFragment navigationInformationFragment;

    // LocationClient の代わりにGoogleApiClientを使います
    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError = false;

    private FusedLocationProviderApi fusedLocationProviderApi;

    private LocationRequest locationRequest;
    private Location location;
    private long lastLocationTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_control);

        Intent intent = getIntent();

        //intentで受け取った目的地と出発地をセットする
        destination = new LatLng(intent.getDoubleExtra("DestLat", 0.0), intent.getDoubleExtra("DestLng", 0.0));
        origin = new LatLng(intent.getDoubleExtra("OriginLat", 0.0), intent.getDoubleExtra("OriginLng", 0.0));


        //画面に配置されてるフラグメントを取得
        FragmentManager fragmentManager  = getFragmentManager();
         navigationMapFragment =  (NavigationMapFragment) fragmentManager.findFragmentById(R.id.navigationMapFragment);
         navigationInformationFragment = (NavigationInformationFragment) fragmentManager.findFragmentById(R.id.navigationInformationFragment);

        //マップ側に目的地と出発地をセット
        //TODO:マップ側で経路探索しているので、制御用アクティビティ側で経路探索を一括して行うように変更したほうがいい
//        navigationMapFragment.destLatLng = destination;
//        navigationMapFragment.originLatLng = origin;

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

        Log.d("LocationActivity", "mGoogleApiClient");

        //FusedLocation開始
        startFusedLocation();

        //ルートのセットを開始
        this.setRoute(destination, origin);

//        // 測位開始
//        Button buttonStart = (Button)findViewById(R.id.button_start);
//        buttonStart.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startFusedLocation();
//            }
//        });
//
//        // 測位終了
//        Button buttonStop = (Button)findViewById(R.id.button_stop);
//        buttonStop.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                stopFusedLocation();
//            }
//        });
    }

    private void startFusedLocation() {
        Log.d("LocationActivity", "onStart");

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
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
        stopFusedLocation();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("LocationActivity", "onConnected");

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location currentLocation = fusedLocationProviderApi.getLastLocation(mGoogleApiClient);

        if (currentLocation != null && currentLocation.getTime() > 20000) {
            location = currentLocation;

//            textLog += "---------- onConnected \n";
//            textLog += "Latitude=" + String.valueOf(location.getLatitude()) + "\n";
//            textLog += "Longitude=" + String.valueOf(location.getLongitude()) + "\n";
//            textLog += "Accuracy=" + String.valueOf(location.getAccuracy()) + "\n";
//            textLog += "Altitude=" + String.valueOf(location.getAltitude()) + "\n";
//            textLog += "Time=" + String.valueOf(location.getTime()) + "\n";
//            textLog += "Speed=" + String.valueOf(location.getSpeed()) + "\n";
//            textLog += "Bearing=" + String.valueOf(location.getBearing()) + "\n";
//            textView.setText(textLog);

        } else {
            // バックグラウンドから戻ってしまうと例外が発生する場合がある
            try {
                //
                fusedLocationProviderApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
                // Schedule a Thread to unregister location listeners
                Executors.newScheduledThreadPool(1).schedule(new Runnable() {
                    @Override
                    public void run() {
                        fusedLocationProviderApi.removeLocationUpdates(mGoogleApiClient, NavigationControlActivity.this);
                    }
                }, 60000, TimeUnit.MILLISECONDS);
//
//                textLog += "onConnected(), requestLocationUpdates \n";
//                textView.setText(textLog);

            } catch (Exception e) {
                e.printStackTrace();
                Toast toast = Toast.makeText(this, "例外が発生、位置情報のPermissionを許可していますか？", Toast.LENGTH_SHORT);
                toast.show();

                //MainActivityに戻す
                finish();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocationTime = location.getTime() - lastLocationTime;

//        textLog += "---------- onLocationChanged \n";
//        textLog += "Latitude=" + String.valueOf(location.getLatitude()) + "\n";
//        textLog += "Longitude=" + String.valueOf(location.getLongitude()) + "\n";
//        textLog += "Accuracy=" + String.valueOf(location.getAccuracy()) + "\n";
//        textLog += "Altitude=" + String.valueOf(location.getAltitude()) + "\n";
//        textLog += "Time=" + String.valueOf(location.getTime()) + "\n";
//        textLog += "Speed=" + String.valueOf(location.getSpeed()) + "\n";
//        textLog += "Bearing=" + String.valueOf(location.getBearing()) + "\n";
//        textLog += "time= " + String.valueOf(lastLocationTime) + " msec \n";
//        textView.setText(textLog);
    }


    @Override
    public void onConnectionSuspended(int i) {
//        textLog += "onConnectionSuspended() \n";
//        textView.setText(textLog);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
//        textLog += "onConnectionFailed()n";
//        textView.setText(textLog);

        if (mResolvingError) {
            // Already attempting to resolve an error.
            Log.d("", "Already attempting to resolve an error");

            return;
        } else if (connectionResult.hasResolution()) {

        } else {
            mResolvingError = true;
        }
    }

    //パーミッションをリクエストしたとき、終わった後に呼ばれるメソッド。ここで結果を確認する。
    //NavigationMapFragmentのなかでRuntimePermission(今回はACCESS_FINE_LOCATION)が必要だった。Activityに書くものなのでこちらに書いた。
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("NavigationActivity", "ACCESS_FINE_LOCATION is permitted");
        //地図画面の現在地表示をオン
        navigationMapFragment.mMap.setMyLocationEnabled(true);
    }


    //-----------------
    //-----------------
    //-----目的地までのルートを取得するための機能たち----//

    //ルートを設定する
    private void setRoute(LatLng destination, LatLng origin) {
        //目的地/出発地が設定されてない
        if (destination == null || origin == null) {
            Log.i("NaviMapFragment", "DEST or ORIGIN is not set.");
            //return;
        }

        LatLng originLatLng = origin;
        LatLng destLatLng = destination;

        // Getting URL to the Google Directions API
        String url = getUrl(originLatLng, destLatLng);
        Log.d("onMapClick", url.toString());
        NavigationControlActivity.FetchUrl FetchUrl = new NavigationControlActivity.FetchUrl();

        // Start downloading json data from Google Directions API
        FetchUrl.execute(url);
    }

    //https://www.androidtutorialpoint.com/intermediate/google-maps-draw-path-two-points-using-google-directions-google-map-android-api-v2/
    //からのコピペ

    //generate URL
    private String getUrl(LatLng origin, LatLng dest) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Sensor enabled
        String sensor = "sensor=false";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            //マップにルート情報を設定(NavigationMapFragmentにてMapの準備ができてない=oMapReadyがまだ呼ばれてない時に呼ぶとエラー)
            try {
                navigationMapFragment.setRoute(destination, origin, new JSONObject(result));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //ゴーストを描画してくれるスレッド(であってる?)を起動
            Log.d("NaviMap", "START GHOST");
            try {
                GhostRendererOnMapService ghost = new GhostRendererOnMapService(navigationMapFragment.mMap);
                ghost.execute(new JSONObject(result));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
