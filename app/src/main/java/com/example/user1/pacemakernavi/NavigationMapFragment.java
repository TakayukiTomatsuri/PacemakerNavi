package com.example.user1.pacemakernavi;

/**
 * Created by user1 on 2017/04/17.
 */

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

//ナビゲーション用地図画面。
public class NavigationMapFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    public Place destination;
    public Place origin;

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

    //MapのFragmentが入るべき場所をレイアウトから探すには、レイアウトからビューが作成されたあとでなくてはならないのでonViewCreatedで実行する必要がある
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //APIレベル17以上でないとgetChildFragmentManager()が使えないが、これの配下にあるFragmentManagerを使わないとダメ。
        MapFragment mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.getUiSettings().setZoomControlsEnabled(true);



        //現在地表示をオンにしたいがパーミッション関係がよくわからないせいでできない
//        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            getActivity().requestPermissions(this, PERMISSIONS, RC_LOCATION_PERMISSIONS);
//            mMap.setMyLocationEnabled(true);
//
//            return;
//        }

        //setRoute(destination, origin);
        //DEBUG
        setRoute(new LatLng(36.37202, 140.475858), new LatLng(36.443232, 140.501526));
    }


    //-----目的地までのルートを取得するための機能たち----//
    //ルートを設定し描画する
    private void setRoute(Place destination, Place origin){
        //目的地/出発地が設定されてない
        if(destination == null || origin ==null ) {
            Log.i("NaviMapFragment", "DEST or ORIGIN is not set.");
            //return;
        }
        if(mMap == null) Log.i("MAP", "mMap == null!");

        Log.i("MAP", "addMarker");
        mMap.addMarker(new MarkerOptions().position(destination.getLatLng()).title("DEST"));
        mMap.addMarker(new MarkerOptions().position(origin.getLatLng()).title("ORIGIN"));
        Log.i("MAP", "addedMarker");
        LatLng originLatLng = origin.getLatLng();
        LatLng destLatLng = destination.getLatLng();

        // Getting URL to the Google Directions API
        String url = getUrl(originLatLng, destLatLng);
        Log.d("onMapClick", url.toString());
        FetchUrl FetchUrl = new FetchUrl();

        // Start downloading json data from Google Directions API
        FetchUrl.execute(url);
        //なぜかずれる！！！！
        //move map camera
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(originLatLng.latitude+300, originLatLng.longitude)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(originLatLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(3));
    }

    //デバッグ用
    private void setRoute(LatLng destination, LatLng origin){
        //目的地/出発地が設定されてない
        if(destination == null || origin ==null ) {
            Log.i("NaviMapFragment", "DEST or ORIGIN is not set.");
            //return;
        }
        if(mMap == null) Log.i("MAP", "mMap == null!");

        Log.i("MAP", "addMarker");

        Log.i("MAP", "addedMarker");
        LatLng originLatLng = origin;
        LatLng destLatLng = destination;

        // Getting URL to the Google Directions API
        String url = getUrl(originLatLng, destLatLng);
        Log.d("onMapClick", url.toString());
        FetchUrl FetchUrl = new FetchUrl();

        // Start downloading json data from Google Directions API
        FetchUrl.execute(url);
        //なぜかずれる！！！！
        //move map camera
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(originLatLng.latitude+300, originLatLng.longitude)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(originLatLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(3));
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

            ParserTask parserTask = new ParserTask();

            //追加ーーーーーー
            Log.d("NaviMap", "START GHOST");
            try {
                GhostRendererOnMapService ghost = new GhostRendererOnMapService(mMap);
                ghost.execute(new JSONObject(result));
            }catch (Exception e){
                e.printStackTrace();
            }
            //追加ここまでーーーー
            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }


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
