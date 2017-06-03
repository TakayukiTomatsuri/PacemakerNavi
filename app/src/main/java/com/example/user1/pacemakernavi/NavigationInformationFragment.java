package com.example.user1.pacemakernavi;

import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
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
            //総行程の距離をもらっとく(ここでは使わないのに。スパゲッティコード！！！)
            routeDistance = route.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").getInt("value");
            //routeプログレスバーの初期化。ここでやらなくてもいい。けど総行程を求めたついで
            ((ProgressBar) getActivity().findViewById(R.id.RouteProgressBar)).setProgress(100);
            DecimalFormat df1 = new DecimalFormat("0.00");
            ((TextView) getActivity().findViewById(R.id.ProgressValueOfRoute)).setText(df1.format((float) routeDistance / 1000) + "km");

            jsonSteps = route.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
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

        //関係ないのにデータの準備が整う順序の関係でここでやる!
        setRouteProgressBar();  //ルートプログレスバーの設定
    }

    //呼ぶたびに行程を一個ずつ進める。何番目の行程、とか指定した方がいいかもしれない。
    public void changeSteps() {
        if (stepsIndex >= jsonSteps.length()) {
            Log.e("NaviInfoFragment", "Steps is over. You have reached the destination.");
            return;
        }

        try {
            //行程の指示の表示を更新
            instructionField.setText(Html.fromHtml(jsonSteps.getJSONObject(stepsIndex).getString("html_instructions")));
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

    float speedAccumulator = 0.0f; /*ユーザーの平均スピード*/
    int lastStepsIndex = stepsIndex; /*前回、速度更新をしたときのstepのインデックス*/
    int speedLogIndex = 0; /*通算何回、速度更新をしたか*/
    ArrayList<Float> speedLog = new ArrayList<>(); /*過去500回ぶんのスピードのログ*/

    //画面に表示する、進む速度を更新
    public void setUserSpeed(float speed) {
        //速度評価の許容範囲
        //TODO: 移動手段や速度によって許容範囲を変える。(歩きなら0.5程度だが自転車など速い移動手段を使うならもっと広げるべき)
        float tolerance = 0.5f;
        String text = "";

        /*
            過去500回ぶんのデータから平均をとる
            (毎回500回の足し算を行わなくて良いように、単純移動平均を計算してるのでこういうメンドくさいことになる)
         */

        float averageSpeed = 0.0f;
        //500回以上、この平均速度計算が行われているなら
        if (speedLog.size() >= 500) {
            //speedAccumulatorが500回ぶんの速度の積算になるように、ちょうど500回まえに記録された速度を引き算する。
            speedAccumulator -= speedLog.get(speedLogIndex);
        }

        //speedAccumulatorには、過去500回ぶんの速度が積算される
        speedAccumulator += speed;
        //ログにも500回ぶんの速度が記録される
        speedLog.add(speedLogIndex, speed);
        //500回まえに記録されたspeedLogのインデックスを計算する
        speedLogIndex++;
        speedLogIndex %= 500;

        //500回以上、この平均速度計算が行われているなら
        if (speedLog.size() >= 500) {
            averageSpeed = speedAccumulator / 500;
        } else {
            averageSpeed = speedAccumulator / speedLogIndex;
        }

        //速度によって、ペース通りかどうか評価を変える。
        if (speed > ghostSpeed + tolerance) text += "↑";
        else if (speed >= ghostSpeed - tolerance) text += "=";
        else text += "↓";

        DecimalFormat df1 = new DecimalFormat("0");
        df1.setMaximumFractionDigits(2);
        df1.setMinimumFractionDigits(2);
        TextView userSpeedInfo = (TextView) getActivity().findViewById(R.id.speed);
        text += "YOUR: " + df1.format(speed) + "m/s" + "   AVE: " + df1.format(averageSpeed) + "m/s   GHOST: " + df1.format(ghostSpeed) + "m/s\n";

        userSpeedInfo.setText(text);
    }

    //ユーザー足跡のアップデート
    public void updateFootprint(Location currentLocation) {
        long now = System.currentTimeMillis();
        //1分に一回、更新する
        int intervalMinute = 1;
        if (now - lastUpdateofFootprint > intervalMinute * 60 * 1000) {
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
    public void onGhostLocationChanged(PolylineOptions ghostFootprint) {
        //主にゴーストのプログレスバーまわりの更新作業
        float ghostProgressDistance = calculatePolylineLength(ghostFootprint);
        DecimalFormat df1 = new DecimalFormat("0");
        df1.setMaximumFractionDigits(2);
        df1.setMinimumFractionDigits(2);
        ((ProgressBar) getActivity().findViewById(R.id.GhostProgressBar)).setProgress((int) (ghostProgressDistance / routeDistance * 100));
        ((TextView) getActivity().findViewById(R.id.ProgressValueOfGhost)).setText(df1.format(ghostProgressDistance / 1000) + " km");
        Log.d("NaviInfo", "Gfootprint" + calculatePolylineLength(ghostFootprint) + "  routeDist" + routeDistance + "   = " + (calculatePolylineLength(ghostFootprint) / routeDistance * 100));
    }


    @Override
    public void onLocationChanged(Location location) {
        //移動したら進んでる速度を更新
        setUserSpeed(location.getSpeed());

        //足跡を更新
        updateFootprint(location);

        //ユーザーのプログレスバーまわりの更新
        float userProgressDistance = calculatePolylineLength(footprint);
        DecimalFormat df1 = new DecimalFormat("0");
        df1.setMaximumFractionDigits(2);
        df1.setMinimumFractionDigits(2);
        ((ProgressBar) getActivity().findViewById(R.id.UserProgressBar)).setProgress((int) (userProgressDistance / routeDistance * 100));
        ((TextView) getActivity().findViewById(R.id.ProgressValueOfUser)).setText(df1.format(userProgressDistance / 1000) + " km");
        Log.d("NaviInfo", "Userfootprint" + calculatePolylineLength(footprint) + "  routeDist" + routeDistance + "   = " + (calculatePolylineLength(footprint) / routeDistance * 100));

        //指示の詳細を更新
        updateInstructionDetail(footprint);
    }

    public void updateInstructionDetail(PolylineOptions userFootprint) {

        //まずスタートから次の指示地点までの距離を計算する
        int ind_steps = 0;
        int nextStepsDist = 0;
        for (; ind_steps <= stepsIndex; ind_steps++) {
            try {
                nextStepsDist += jsonSteps.getJSONObject(ind_steps).getJSONObject("distance").getInt("value");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        DecimalFormat df1 = new DecimalFormat("0");
        df1.setMaximumFractionDigits(2);
        df1.setMinimumFractionDigits(2);
        //ちゃんとルートを辿っている場合、現在地から次の指示地点までの距離 = (スタートから次の指示地点までの距離) - (スタートから現在地までの距離)
        //しかしルートを外れると、現在地-指示地点間の距離を計算しているワケではないこの計算は狂うことに注意！
        float dist = nextStepsDist - calculatePolylineLength(userFootprint);
        ((TextView) getActivity().findViewById(R.id.instructionDetail)).setText(Html.fromHtml("あと <b>" + df1.format(dist / 1000) + "</b>km"));
    }

    //ルート表示用のプログレスバーを表示(標準のプログレスバーだとできないみたいなので画像合成してる)
    public void setRouteProgressBar() {
        try {
            //プログレスバーを描くための素材
            InputStream istream = getResources().getAssets().open("routeDot.bmp");
            Bitmap routeDotBitmap = BitmapFactory.decodeStream(istream);
            istream = getResources().getAssets().open("routeBar.bmp");
            Bitmap routeBarBitmap = BitmapFactory.decodeStream(istream);

            int width = routeBarBitmap.getWidth(); // 元ファイルの幅取得
            int height = routeBarBitmap.getHeight(); // 元ファイルの高さ取得
            Bitmap newbitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(newbitmap);  //元となるキャンバスの作成
            canvas.drawBitmap(routeBarBitmap, 0, 0, (Paint) null);

            //各経路ポイントの距離をみてルートプログレスバーに追加していく
            for (int ind = 0; ind < jsonSteps.length(); ind++) {
                try {
                    int dist = jsonSteps.getJSONObject(ind).getJSONObject("distance").getInt("value");
                    int newX = (int) (((double) dist / routeDistance) * width); //プログレスバー上の、経路ポイントの合成位置
                    canvas.drawBitmap(routeDotBitmap, newX, 0, (Paint) null); // 画像合成
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            ImageView imageView = (ImageView) getActivity().findViewById(R.id.userProgressBarImage);
            imageView.setImageBitmap(newbitmap);    //合成結果の画像の表示

            //imageView.setImageResource(R.drawable.dummy2);  //テストのためのダミー
        } catch (IOException e) {
            Log.d("Assets", "Error");
        }
    }

}
