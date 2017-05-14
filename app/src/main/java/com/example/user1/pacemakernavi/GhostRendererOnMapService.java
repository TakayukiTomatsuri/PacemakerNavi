package com.example.user1.pacemakernavi;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by user1 on 2017/04/22.
 */

//時間経過にしたがって、ゴーストの軌跡を伸ばしていく。
//伸ばすスピードは、ステップにかかる時間と、ステップを表すポリラインに含まれる点の数によって変えていく。(下のdoInBackground内のintervalTimeの定義を参照)
public class GhostRendererOnMapService extends AsyncTask<String, PolylineOptions, Void> {
    GoogleMap mMap = null;
    int targetTimePercent = 0;
    GhostLocationListner listner = null;


    public GhostRendererOnMapService(GoogleMap map, GhostLocationListner listner) {
        super();
        mMap = map;
        this.listner = listner;
    }

    public interface GhostLocationListner {
        public void onGhostLocationChange(PolylineOptions ghostFootprint);
    }

    // doInBackgroundの事前準備処理（UIスレッド）
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    // 別スレッド処理
    protected Void doInBackground(String... params) {
        targetTimePercent = Integer.parseInt(params[1]);
        PolylineOptions polyline = new PolylineOptions();
        try {
            JSONObject json = new JSONObject(params[0]);
            //stepsを抽出
            JSONArray jsonSteps = json.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");

            //全ステップに関して
            for (int stepsIndex = 0; stepsIndex < jsonSteps.length(); stepsIndex++) {
                int time = jsonSteps.getJSONObject(stepsIndex).getJSONObject("duration").getInt("value");
                String encodedPolyline = jsonSteps.getJSONObject(stepsIndex).getJSONObject("polyline").getString("points");

                //エンコードされたポリラインをStringからLatLngの配列にデコード
                List<LatLng> polylinePoints = DataParser.decodePoly(encodedPolyline);

                //更新までの間隔。(そのステップにかかる時間)/(そのステップに含まれるポリラインの点の数)をステップごとに変える。
                //起動時に渡された、「通常時の何パーセントの時間で到達するのか」の値で時間を短くしている。(targetTimePercentが80なら、0.08倍する、のように)
                int intervalTime = (int) ((time / polylinePoints.size() * 1000 * targetTimePercent) * 0.01);
                Log.i("GHOST", "interval: " + intervalTime);

                //このステップに含まれるポリライン上の全ての点に対して
                for (int pointsIndex = 0; pointsIndex < polylinePoints.size(); pointsIndex++) {
                    Log.i("GHOST", "addPoint  stepIndex: " + stepsIndex + " /" + jsonSteps.length() + " pointIndex: " + pointsIndex + " /" + polylinePoints.size());
                    //一定時間待ってポリラインに点を追加
                    Thread.sleep(intervalTime);
                    polyline.add(polylinePoints.get(pointsIndex));
                    //画面更新
                    publishProgress(polyline);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    // doInBackgroundの事後処理(UIスレッド)
    protected void onPostExecute(String... sss) {

    }

    // 進捗状況をUIに反映するための処理(UIスレッド)
    @Override
    protected void onProgressUpdate(PolylineOptions... polyline) {
        // ポリラインを描画する
        mMap.addPolyline(polyline[0]);
        listner.onGhostLocationChange(polyline[0]);
    }

}