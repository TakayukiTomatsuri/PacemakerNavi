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
 * Created by user1 on 2017/04/22.a
 */


//ナビゲーション画面のコントローラとなるアクティビティです
//位置情報の監視、及び、設定された目的地と出発地からルートを検索して、情報をNavigationMapFragmentと　NavigationInformationFragmentに渡します。
public class NavigationControlActivity extends Activity implements GoogleMapsDirectionApiClient.GoogleMapsDirectionApiReceiver {
    final int REQUEST_LOCATION = 1; //パーミッションかなにかリクエストするときにどこのリクエストだったかの識別のためのrequestCode。コールバックメソッド内で、どっから帰ってきたのかの識別に使うっぽい
    LatLng destination; //目的地、MainActivityから渡される
    LatLng origin;  //出発地、MainActivityから渡される
    NavigationMapFragment navigationMapFragment;    //配下のFragment、地図表示担当
    NavigationInformationFragment navigationInformationFragment;    //配下のFragment、画面下半分で情報表示担当
    private int targetTimePercent = 0;  //通常の何パーセントの時間で目標に到達するか(設定画面で設定したもの)

    //GoogleDirectionAPIのレスポンスが返ってきたら呼ばれるコールバックメソッド
    @Override
    public void onResultOfGoogleMapsDirectionApi(String result) {
        try {
            //マップにルート情報を設定(NavigationMapFragmentにてMapの準備ができてない=onMapReadyがまだ呼ばれてない時に呼ぶとエラー)
            navigationMapFragment.setRoute(destination, origin, new JSONObject(result));
            //インフォーメーション部(ナビゲーション画面の下半分)に、行程(xx交差点で曲がる)を設定する
            navigationInformationFragment.addGeofences(new JSONObject(result));
            //右に曲がるとかそういう指示を表示させるための準備
            navigationInformationFragment.changeSteps();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //ゴーストを描画してくれるスレッドを起動
        Log.d("NaviMapActivity", "START GHOST");
        try {
            GhostRendererOnMapService ghost = new GhostRendererOnMapService(navigationMapFragment.mMap, navigationInformationFragment);
            //開始させるときに、ゴーストのスピードも指定する(通常の何パーセントの時間で進むか)
            ghost.execute(result, new Integer(targetTimePercent).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_control);

        Intent intent = getIntent();

        //intentで受け取った目的地と出発地をセットする
        destination = new LatLng(intent.getDoubleExtra("DestLat", 0.0), intent.getDoubleExtra("DestLng", 0.0));
        origin = new LatLng(intent.getDoubleExtra("OriginLat", 0.0), intent.getDoubleExtra("OriginLng", 0.0));

        targetTimePercent = intent.getIntExtra("TargetTimePercent", 0);
        Log.d("NavigationActivity", "TargetTimePercent: " + targetTimePercent);

        //画面に配置されてるフラグメントを取得
        FragmentManager fragmentManager = getFragmentManager();
        navigationMapFragment = (NavigationMapFragment) fragmentManager.findFragmentById(R.id.navigationMapFragment);
        navigationInformationFragment = (NavigationInformationFragment) fragmentManager.findFragmentById(R.id.navigationInformationFragment);

        //ルートのセットを開始
        this.setRoute(destination, origin);
    }


    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
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

        Log.i("onNewIntent", String.valueOf(intent));
        //ジオフェンスのイベントのタイプを判別する
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        int transitionType = event.getGeofenceTransition();
        if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
            //右左折などの指示を進める
            navigationInformationFragment.changeSteps();
        }
    }

    //ルートを設定する
    private void setRoute(LatLng destination, LatLng origin) {
        //目的地/出発地が設定されてない場合
        if (destination == null || origin == null) {
            Log.w("NaviMapFragment", "DEST or ORIGIN is not set.");
            //return;
        }

        LatLng originLatLng = origin;
        LatLng destLatLng = destination;

        //DirectionAPIで経路情報取得(取得した後、コールバックメソッドとしてonResultOfGoogleMapsDirectionApiが呼ばれる)
        GoogleMapsDirectionApiClient.fetchData(origin, destination, this);
    }

}
