package com.example.user1.pacemakernavi;

import android.os.AsyncTask;
import android.util.Log;

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

//経路探索のためのクラス
//インスタンスは作らず、GoogleMapsDirectionApiClient.fetchData(origin, destination, this);などとし、
//呼び出し型でGoogleMapsDirectionApiReceiverインターフェースを実装し、コールバックメソッドonResultOfGoogleMapsDirectionApiを通し結果を受け取る
public class GoogleMapsDirectionApiClient {
    public interface GoogleMapsDirectionApiReceiver {
        //結果を渡すためのコールバックメソッド
        void onResultOfGoogleMapsDirectionApi(String result);
    }

    public static void fetchData(LatLng origin, LatLng destination, GoogleMapsDirectionApiClient.GoogleMapsDirectionApiReceiver caller) {
        if (origin == null || destination == null || caller == null) {
            Log.e("DistanceMatrixClient", "ORIGN or DEST or CALLER is null!");
            return;
        }

        //下の匿名クラスに渡すためにfinalで宣言したやつに入れ直す(回避の仕方がわかりません)
        final GoogleMapsDirectionApiClient.GoogleMapsDirectionApiReceiver methodCaller = caller;

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
                    Log.d("downloadUrl", data.toString());
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Log.d("Background Task data", data.toString());
                return data;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                Log.i("DirectionAPIClient", result);

                //呼び出し元のコールバックメソッドを呼び出して、結果を渡す
                methodCaller.onResultOfGoogleMapsDirectionApi(result);
            }
        }.execute(generateUrl(origin, destination));

    }

    //リクエストのためのURL生成
    private static String generateUrl(LatLng origin, LatLng dest) {
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
}
