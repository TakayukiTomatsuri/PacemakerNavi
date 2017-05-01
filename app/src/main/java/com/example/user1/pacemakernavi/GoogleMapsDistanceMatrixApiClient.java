package com.example.user1.pacemakernavi;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by user1 on 2017/05/01.
 */

//GoogleMapsDistanceMatrixApiで、距離と時間をフェッチしてくるやつ
//インスタンスはつくらないで、GoogleMapsDistanceMatrixApiClient.fetchData(origin, destination, this);みたいにして呼び出す。
//呼び出した側(this)にGoogleMapsDistanceMatrixApiListnerインターフェースを実装し、コールバックメソッドを呼ばれる準備をする必要がある。
public class GoogleMapsDistanceMatrixApiClient {

    public interface GoogleMapsDistanceMatrixApiListner {
        //押されたボタンを渡す。buttonView.getIdでどのボタンが押されたのか識別する
        void onResultOfGoogleMapsDistanceMatrixApi(String result);

    }

    public static void fetchData(LatLng origin, LatLng destination, GoogleMapsDistanceMatrixApiClient.GoogleMapsDistanceMatrixApiListner caller) {
        if (origin == null || destination == null || caller == null) {
            Log.e("DistanceMatrixClient", "ORIGN or DEST or CALLER is null!");
            return;
        }

        //下の匿名クラスに渡すためにfinalで宣言したやつに入れ直す(回避の仕方がわかりません)
        final GoogleMapsDistanceMatrixApiClient.GoogleMapsDistanceMatrixApiListner methodCaller = caller;

        //非同期でGoogleDistanceMatrixAPIから情報をもらう
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... strUrl) {
                String data = "";
                InputStream iStream = null;
                HttpURLConnection urlConnection = null;
                URL url = null;

                try {
                    url = new URL(strUrl[0]);

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
                    Log.d("GMDistanceMatrixClient", data.toString());
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Log.d("GMDistanceMatrixClient", data.toString());
                return data;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                try {
                    //デバッグのため情報表示
                    JSONObject distanceResult = new JSONObject(result);
                    JSONObject element = distanceResult.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(0);
                    JSONObject distanceJson = element.getJSONObject("distance");
                    JSONObject durationJson = element.getJSONObject("duration");
                    Log.i("GMDistanceMatrixClient", "DistanceMatrixAPI: DISTANCE: " + distanceJson.getString("text") + " DURATION: " + durationJson.getString("text"));

                    //呼び出し元のコールバックメソッドを呼び出して、結果を渡す
                    methodCaller.onResultOfGoogleMapsDistanceMatrixApi(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute(generateUrl(origin, destination));

    }

    //リクエストのためのURL生成
    private static String generateUrl(LatLng origin, LatLng destination) {
        return "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=" + origin.latitude + "," + origin.longitude + "&destinations=" + destination.latitude + "," + destination.longitude + "&key=AIzaSyDU1GHY5SXQT7-3rVsQBkZBpOUKw2vdx58";
    }

}
