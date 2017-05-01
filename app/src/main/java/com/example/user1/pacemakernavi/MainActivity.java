package com.example.user1.pacemakernavi;

import android.*;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.vision.text.Text;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by user1 on 2017/04/17.
 */

//目的地設定などをします。
//メインとは名がついているけど、現状は起動時の設定画面をコントロールするだけのActivityです。
public class MainActivity extends Activity implements  PlacePickerFragment.PlacePickerFragmentListener, SettingMenuFragment.SettingMenuFragmentListener {

    private Place destination = null;   //セットされた目的地
    private Place origin = null;    //セットされた出発地
    private boolean isChoosingDistination = true;   //現在、目的地or出発地のどちらをセットしている段階か
    final int REQUEST_LOCATION = 1; //requestCode。コールバックメソッド内で、どっから帰ってきたのかの識別に使うっぽい
    SettingMenuFragment settingMenuFragment = new SettingMenuFragment();    //セッティングメニューはアプリの中で常に一つ
    //PlacePickerは選択のたび生成/破棄されるみたいなのでここで生成しない
    //PlacePickerFragment placePickerFragment = new PlacePickerFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        // コードからFragmentを追加

        //Android6 からは重要なパーミッションはインストール時には与えられない！実行時にお伺いを立ててイチイチ許可してもらう必要がある。
        //ちなみに、マップ画面に遷移するときに許可を求めてもいいが、このすぐ後に許可が必要な行動がきてしまうとonRequestPermissionsResultからやり直してあげたりしなくちゃならないので面倒っぽいのでこの画面で最初にやっておく
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
              ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }

        // Fragmentの追加や削除といった変更を行う際は、Transactionを利用します
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        transaction.add(R.id.container, settingMenuFragment).addToBackStack(null);

        // 最後にcommitしないと反映されない!
        transaction.commit();


    }

    @Override
    public void onStart() {
        super.onStart();
        //TODO: onCreateだと時間的に準備が整っていないのでこっちでやってるが、onStart()はライフサイクル的にはリスタートされた後にも呼ばれる。なるべく一度しか呼ばれないところに移すべき。
    }

    //PlacePicker上で目的地が選択された場合に呼ばれるコールバックメソッド(使い方あってる...?)
    public void onPlacePickerFragmentChosen(Place chosenPlace){
        Log.d("MainActivity", "PlacePicked!");
        //SettingMenu画面の目的地/出発地情報を変更します
        if(isChoosingDistination){
            destination = chosenPlace;
            settingMenuFragment.setDestinationInfo(chosenPlace);
        }
        else{
            origin = chosenPlace;
            settingMenuFragment.setOriginInfo(chosenPlace);
        }

        //時間と距離の表示
        if (origin != null && destination != null) {
            settingMenuFragment.setDirectionInformation(origin.getLatLng(), destination.getLatLng());
        }
    }

    //SettingMenuFragment上のボタンが押された時に呼ばれるコールバックメソッド
    public  void onClickSettingMenuButton(View buttonView){
        if(buttonView.getId()==R.id.startNavigation){
            Log.d("MainActivity", "CALL START NAVI");
            //ナビゲーション画面に移行する
            Intent intent = new Intent(MainActivity.this, NavigationControlActivity.class);

            //設定されてない時のデフォルトのと出発地
            if(origin == null || destination == null){
                Toast.makeText(this, "DEST or ORIGIN is null!", Toast.LENGTH_SHORT).show();

                intent.putExtra("DestLat", 36.37202);
                intent.putExtra("DestLng", 140.475858);
                intent.putExtra("OriginLat", 36.443232);
                intent.putExtra("OriginLng", 140.501526);
                intent.putExtra("TargetTimePercent", settingMenuFragment.targetTimeParcent);
                startActivity(intent);
                return;
            }

            //ナビゲーションに目的地と出発地の座標を渡す(オブジェクトのまま渡したいがちょっと面倒なので)
            intent.putExtra("DestLat", destination.getLatLng().latitude);
            intent.putExtra("DestLng", destination.getLatLng().longitude);
            intent.putExtra("OriginLat", origin.getLatLng().latitude);
            intent.putExtra("OriginLng", origin.getLatLng().longitude);
            intent.putExtra("TargetTimePercent", settingMenuFragment.targetTimeParcent);
            startActivity(intent);

            return;
        }

        //どのボタンが押されたかを調べ目的地or出発地のどちらを探したいのかを保存
        if(buttonView.getId() == R.id.chooseDestinationButton) isChoosingDistination = true;
        else if(buttonView.getId() == R.id.chooseOriginButton) isChoosingDistination = false;

        //PlacePickerを起動してユーザにプレイスを選択させる(PlacePickerはその都度生成され、プレイスが選択されたあとは破棄されるみたい)
        PlacePickerFragment placePickerFragment = new PlacePickerFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.container, placePickerFragment).addToBackStack(null);
        // 最後にcommitを使用することで変更を反映
        transaction.commit();

    }

    //Android6からはRuntimePermissionとかいって、重要なパーミッションは実行時に許可してもらう。それのリクエストをした時に、結果が判明したあとに呼ばれるコールバックメソッド
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }




}
