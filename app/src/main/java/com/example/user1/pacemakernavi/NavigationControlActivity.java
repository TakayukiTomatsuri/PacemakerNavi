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
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
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
//位置情報の監視、及び、設定された目的地と出発地からルートを検索して、情報をNavigationMapFragmentと　NavigationInformationFragmentに渡します。
//FusedLocationなんとかが使われてるけど、現在使っていません。(ルートの案内にGeoFenceを使うように変更したため)
public class NavigationControlActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    final int REQUEST_LOCATION = 1;
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
        FragmentManager fragmentManager = getFragmentManager();
        navigationMapFragment = (NavigationMapFragment) fragmentManager.findFragmentById(R.id.navigationMapFragment);
        navigationInformationFragment = (NavigationInformationFragment) fragmentManager.findFragmentById(R.id.navigationInformationFragment);

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

        Log.d("NaviControlActivity", "mGoogleApiClient");

        //FusedLocation開始
        startFusedLocation();

        //ルートのセットを開始
        this.setRoute(destination, origin);
    }

    private void startFusedLocation() {
        Log.d("NaviControlActivity", "onStart");

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
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location currentLocation = fusedLocationProviderApi.getLastLocation(mGoogleApiClient);

        if (currentLocation != null && currentLocation.getTime() > 20000) {
            location = currentLocation;

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

    //位置が変わったら呼ばれるメソッド
    @Override
    public void onLocationChanged(Location location) {
        lastLocationTime = location.getTime() - lastLocationTime;
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
    //NavigationMapFragmentのなかでRuntimePermission(今回はACCESS_FINE_LOCATION)が必要なのでそちらで要求している。
    //Fragmentには書けずにActivityで実装する必要があるみたい
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("NavigationActivity", "ACCESS_FINE_LOCATION is permitted");

        //ユーザーがパーミッションを与えてくれない場合、許可するまで延々と要求し続ける。
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "PERMITT OR DIE!", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
            return;
        }
        //地図画面の現在地表示をオン
        navigationMapFragment.mMap.setMyLocationEnabled(true);
    }

    //インテントが投げられるとこれが呼ばれる
    //主に、NavigationInformationFragmentのほうで設定したGeoFenceに入ったときに来ることになる
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Log.d("onNewIntent", String.valueOf(intent));
        //ジオフェンスのイベントのタイプを判別する
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        int transitionType = event.getGeofenceTransition();
        if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
            //右左折などの指示を進める
            navigationInformationFragment.changeSteps();
        }
    }


    //-----------------
    //-----------------
    //-----目的地までのルートを取得するための機能たち----//

    //ルートを設定する
    private void setRoute(LatLng destination, LatLng origin) {
        //目的地/出発地が設定されてない場合
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

            try {
                //マップにルート情報を設定(NavigationMapFragmentにてMapの準備ができてない=oMapReadyがまだ呼ばれてない時に呼ぶとエラー)
                navigationMapFragment.setRoute(destination, origin, new JSONObject(result));
                //インフォーメーション部(ナビゲーション画面の下半分)に、行程(xx交差点で曲がる)を設定する
                navigationInformationFragment.addGeofences(new JSONObject(result));
                //右に曲がるとかそういう指示を表示させるための準備
                navigationInformationFragment.changeSteps();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //ゴーストを描画してくれるスレッドを起動
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
