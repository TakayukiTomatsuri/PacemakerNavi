package com.example.user1.pacemakernavi;

import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by user1 on 2017/04/22.
 */

//ナビゲーション画面画面の下半分。行程や速度などを表示したい...
public class NavigationInformationFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    TextView instructionField; //行程の指示(xx交差点で右)の表示部
    JSONArray jsonSteps; //行程(ルート検索で帰って来るJSONでいうsteps)
    int stepsIndex = 0; //行程のインデックス

    private GoogleApiClient locationClient = null;
    ArrayList<Geofence> mGeofenceList = new ArrayList<>(); //行程のエンドポイントに到達したかどうかを判断するためのジオフェンスたち

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stepsIndex = 0;

        //GoogleApiClientの作成
        locationClient = new GoogleApiClient.Builder(getActivity().getApplicationContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    //行程のエンドポイントに到達したかどうかを判定するジオフェンスを追加する
    public void addGeofences(JSONObject route) {
        final int radius = 50;

        try {
            jsonSteps = route.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
            //ジオフェンスは一度に100個以上は登録できないので、一応お知らせする
            if (jsonSteps.length() >= 100) {
                Log.d("NaviInfoFragment", "Too many steps in route!");
                return;
            }
            //setpsのエンドポイントの位置を全て登録していく
            for (int i = 0; i < jsonSteps.length(); i++) {
                JSONObject endLocation = jsonSteps.getJSONObject(i).getJSONObject("end_location");
                //Geofenceの作成
                mGeofenceList.add(new Geofence.Builder()
                        .setRequestId("ID")
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                        .setCircularRegion(endLocation.getDouble("lat"), endLocation.getDouble("lng"), radius)
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .build());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //呼ぶたびに行程を一個ずつ進める。何番目の行程、とか指定した方がいいかもしれない。
    public void changeSteps() {
        if (stepsIndex >= jsonSteps.length()) {
            Log.d("NaviInfoFragment", "Steps is over. You have reached the destination.");
            return;
        }

        try {
            //行程の指示の表示を
            instructionField.setText(jsonSteps.getJSONObject(stepsIndex).getString("html_instructions"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        stepsIndex++;

    }

    // Fragmentで表示するViewを作成するメソッド
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // viewの作成
        return inflater.inflate(R.layout.fragment_navigation_information, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        instructionField = (TextView) getActivity().findViewById(R.id.instruction);

    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Toast.makeText(getActivity(), "onConnectionFailed", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // PendingIntent の生成
        //(GeoFenceに入る/出るなどすると、コントローラ側のアクティビティ=NavigationControlActivityにインテントを生成する。ジオフェンスに何かあればそっちのonNewIntentコールバックメソッドが呼ばれる。)
        Intent intent = new Intent(getActivity().getApplicationContext(), NavigationControlActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        //GeofencingApiにジオフェンスを加える
        LocationServices.GeofencingApi.addGeofences(locationClient, mGeofenceList,
                pendingIntent);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


}
