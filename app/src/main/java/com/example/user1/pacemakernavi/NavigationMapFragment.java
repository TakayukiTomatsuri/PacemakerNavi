package com.example.user1.pacemakernavi;

/**
 * Created by user1 on 2017/04/17.
 */

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import android.Manifest;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.PolylineOptions;

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

import static com.google.android.gms.wearable.DataMap.TAG;

//ナビゲーション画面用の地図画面。ナビゲーション画面の上半分をこれで構成します。
public class NavigationMapFragment extends Fragment implements OnMapReadyCallback {
    public GoogleMap mMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Fragmentで表示するViewを作成するメソッド
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // viewの作成
        return inflater.inflate(R.layout.fragment_navigation_map, container, false);
    }

    //MapのFragmentが入るべき場所をレイアウトから探すことは、レイアウトからビューが作成されたあとでなくてはならないのでonViewCreatedで実行する必要がある
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //APIレベル17以上でないとgetChildFragmentManager()が使えないが、これの配下にあるFragmentManagerを使わないとダメ。
        MapFragment mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    final int REQUEST_LOCATION =1;
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.getUiSettings().setZoomControlsEnabled(true);

        //アプリ起動時にMainActivityのほうで許可を要請する画面が出るので許可もらってるはずだが、もしもらってなかった時用。
        //現在地取得に必要なパーミッションを確認する。結果はActivityにおいてonRequestPermissionsResultというコールバックメソッドが呼ばれるので確認。
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //許可されてない場合は要請する
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
            //結果待ちのためにreturnが必須。
            //returnしないと次にすすんでしまい、パーミッションが必要な行動がパーミッション無しの状態で実行され、落ちる。
            //よってこういう直前にパーミッション要求する場合はonRequestPermissionsResultなどでもういっかいやり直さなくてはならない。面倒。
            //今回は、アプリ起動時にもパーミッションを確認するようにしてある。ので、こっちの画面の実装は適当。(一応、ちゃんと動くようには作ってある)
            return;
        }
        mMap.setMyLocationEnabled(true);
    }


    //パーミッションをリクエストしたとき、終わった後に呼ばれるメソッド。この中で結果を確認するが、FragmentでなくてActivityに書くもの。
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                                           int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        mMap.setMyLocationEnabled(true);
//    }


    //-----目的地までのルートを取得するための機能たち----//
    //ルートを設定し描画する。ルートは親のNavigationControlActivityがルートを検索してくれ、検索結果のJSONが渡される。
    public void setRoute(Place destination, Place origin, JSONObject routeResult) {
        //目的地/出発地が設定されてない
        if(destination == null || origin ==null ) {
            Log.i("NaviMapFragment", "DEST or ORIGIN is not set.");
            //return;
        }
        if(mMap == null) Log.i("MAP", "mMap == null!");

        Log.i("NaviMapFragment", "addMarker");
        mMap.addMarker(new MarkerOptions().position(destination.getLatLng()).title("DEST"));
        mMap.addMarker(new MarkerOptions().position(origin.getLatLng()).title("ORIGIN"));
        Log.i("NaviMapFragment", "addedMarker");

        ParserTask parserTask = new ParserTask();
        // Invokes the thread for parsing the JSON data
        parserTask.execute(routeResult.toString());

        //出発地にズームイン
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin.getLatLng(), 11));
    }

    //デバッグ用。上のメソッドの、PlaceではなくLatLngで目的地・出発地を渡す版
    public void setRoute(LatLng destination, LatLng origin, JSONObject routeResult) {
        //目的地/出発地が設定されてない
        if(destination == null || origin ==null ) {
            Log.i("NaviMapFragment", "DEST or ORIGIN is not set.");
            //return;
        }
        if(mMap == null) Log.i("MAP", "mMap == null!");

        Log.i("NaviMapFragment", "addMarker");
        mMap.addMarker(new MarkerOptions().position(destination).title("DEST"));
        mMap.addMarker(new MarkerOptions().position(origin).title("ORIGIN"));
        Log.i("NaviMapFragment", "addedMarker");

        ParserTask parserTask = new ParserTask();
        // Invokes the thread for parsing the JSON data
        parserTask.execute(routeResult.toString());

        //出発地にズームイン
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 11));
    }

    //以下は、　https://www.androidtutorialpoint.com/intermediate/google-maps-draw-path-two-points-using-google-directions-google-map-android-api-v2/
    //からのコピペ

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask",jsonData[0].toString());
                //追加---
                Log.d("ParserTask", "steps count:"+String.valueOf(jObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps").length()));
                //追加ここまで----
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);
                //追加---
                Log.d("ParserTask", "route polyline(==All of Route) point count "+String.valueOf(path.size()));
                //追加ここまで----
                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);

                Log.d("onPostExecute","onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                mMap.addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }
    }
}
