package com.example.user1.pacemakernavi;

import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by user1 on 2017/04/22.
 */

//ナビゲーション画面画面の下半分。行程や速度などを表示したい...
public class NavigationInformationFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GhostRendererOnMapService.GhostLocationListner {
    TextView instructionField; //行程の指示(xx交差点で右)の表示部
    JSONArray jsonSteps; //行程(ルート検索で帰って来るJSONでいうsteps)
    int stepsIndex = 0; //行程のインデックス

    private GoogleApiClient locationClient = null;
    private float ghostSpeed = 0;   //速度の表示に使うだけの値
    ArrayList<Geofence> mGeofenceList = new ArrayList<>(); //行程のエンドポイントに到達したかどうかを判断するためのジオフェンスたち

    PolylineOptions footprint = new PolylineOptions();
    long lastUpdateofFootprint = 0;
    int routeDistance = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stepsIndex = 0;

        //位置情報更新を通知してもらえるようにする
        FusedLocationClientSingleton.getInstance().addListner(this);

        //GoogleApiClientの作成
        locationClient = new GoogleApiClient.Builder(getActivity().getApplicationContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    //行程のエンドポイントに到達したかどうかを判定するジオフェンスを追加する
    public void addGeofences(JSONObject route) {
        final int radius = 20;

        try {
            jsonSteps = route.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
            //総行程の距離をもらっとく(ここでは使わないのに。スパゲッティコード！！！)
            routeDistance = route.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").getInt("value");
            //ジオフェンスは一度に100個以上は登録できないので、一応お知らせする
            if (jsonSteps.length() >= 100) {
                Log.e("NaviInfoFragment", "Too many steps in route!");
                return;
            }
            //setpsのエンドポイントの位置を全て登録していく
            for (int i = 0; i < jsonSteps.length(); i++) {
                Log.d("NaviInfoFragment", jsonSteps.getJSONObject(stepsIndex).getString("html_instructions"));
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
            Log.e("NaviInfoFragment", "Steps is over. You have reached the destination.");
            return;
        }

        try {
            //行程の指示の表示を更新
            instructionField.setText(jsonSteps.getJSONObject(stepsIndex).getString("html_instructions"));
            //ゴーストの速度(ただの数字の表示用)を更新
            JSONObject jsonDuration = jsonSteps.getJSONObject(stepsIndex).getJSONObject("duration");
            JSONObject jsonDistance = jsonSteps.getJSONObject(stepsIndex).getJSONObject("distance");
            //単位はm/s。
            ghostSpeed = (float) jsonDistance.getInt("value") / jsonDuration.getInt("value");

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
        //xx交差点を右、とかいう指示を表示するテキストフィールド
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

    //画面に表示する、進む速度を更新
    public void setUserSpeed(float speed) {
        //速度評価の許容範囲
        //TODO: 移動手段や速度によって許容範囲を変える。(歩きなら0.5程度だが自転車など速い移動手段を使うならもっと広げるべき)
        float tolerance = 0.5f;

        TextView userSpeedInfo = (TextView) getActivity().findViewById(R.id.speed);
        String text = "YOUR: " + speed + "m/s    GHOST: " + ghostSpeed + "m/s\n";

        //速度によって、ペース通りかどうか評価を変える。
        if (speed > ghostSpeed + tolerance) text += "TOO FAST! SLOW DOWN";
        else if (speed >= ghostSpeed - tolerance) text += "GOOD PACE";
        else text += "HURRY UP! YOU ARE LATE";

        userSpeedInfo.setText(text);
    }

    //足跡のアップデート
    public void updateFootprint(Location currentLocation) {
        long now = System.currentTimeMillis();
        //1分に一回、更新する
        if (now - lastUpdateofFootprint > 1 * 60 * 1000) {
            footprint.add(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
            lastUpdateofFootprint = now;
        }
    }

    //ポリラインの長さを計算する
    protected float calculatePolylineLength(PolylineOptions points) {
        float totalDistance = 0;

        for (int i = 1; i < points.getPoints().size(); i++) {
            Location currLocation = new Location("this");
            currLocation.setLatitude(points.getPoints().get(i).latitude);
            currLocation.setLongitude(points.getPoints().get(i).longitude);

            Location lastLocation = new Location("this");
            lastLocation.setLatitude(points.getPoints().get(i - 1).latitude);
            lastLocation.setLongitude(points.getPoints().get(i - 1).longitude);

            totalDistance += lastLocation.distanceTo(currLocation);

        }
        return totalDistance;
    }

    //ゴーストが移動したら呼ばれる
    @Override
    public void onGhostLocationChange(PolylineOptions ghostFootprint) {
        float ghostProgress = calculatePolylineLength(ghostFootprint);
        DecimalFormat df1 = new DecimalFormat("0.00");
        ((ProgressBar) getActivity().findViewById(R.id.GhostProgressBar)).setProgress((int) (ghostProgress / routeDistance * 100));
        ((TextView) getActivity().findViewById(R.id.ProgressValueOfGhost)).setText(df1.format(ghostProgress / 1000) + " km");
        Log.d("NaviInfo", "Gfootprint" + calculatePolylineLength(ghostFootprint) + "  routeDist" + routeDistance + "   = " + (calculatePolylineLength(ghostFootprint) / routeDistance * 100));
    }

    //移動したら進んでる速度を更新
    @Override
    public void onLocationChanged(Location location) {
        setUserSpeed(location.getSpeed());
    }
}
