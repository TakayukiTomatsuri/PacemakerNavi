package com.example.user1.pacemakernavi;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.internal.PlaceEntity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
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
import java.util.Locale;

/**
 * Created by user1 on 2017/04/22.
 */


//ナビゲーション画面のコントローラとなるアクティビティです
public class NavigationControlActivity  extends Activity{
    LatLng destination;
    LatLng origin;
    NavigationMapFragment navigationMapFragment;
    NavigationInformationFragment navigationInformationFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_control);

        Intent intent = getIntent();

        destination = new LatLng(intent.getDoubleExtra("DestLat", 0.0), intent.getDoubleExtra("DestLng", 0.0));
        origin = new LatLng(intent.getDoubleExtra("OriginLat", 0.0), intent.getDoubleExtra("OriginLng", 0.0));



        FragmentManager fragmentManager  = getFragmentManager();
         navigationMapFragment =  (NavigationMapFragment) fragmentManager.findFragmentById(R.id.navigationMapFragment);
         navigationInformationFragment = (NavigationInformationFragment) fragmentManager.findFragmentById(R.id.navigationInformationFragment);

        navigationMapFragment.destLatLng = destination;
        navigationMapFragment.originLatLng = origin;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        navigationMapFragment.mMap.setMyLocationEnabled(true);
    }











}
