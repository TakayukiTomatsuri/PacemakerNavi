package com.example.user1.pacemakernavi;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.location.places.Place;


/**
 * Created by user1 on 2017/04/17.
 */

//このアプリの全てを制御するActivity。
//各画面のレイアウトと処理はFragmentで持ちます。
//各画面(=Fragment)の切り替えや、仲を取り持つのがこのActivity。
public class MainActivity extends Activity implements  PlacePickerFragment.PlacePickerFragmentListener, SettingMenuFragment.SettingMenuFragmentListener {

    private Place distination = null;   //セットされた目的地
    private Place origin = null;    //セットされた出発地
    private boolean isChoosingDistination = true;   //現在、目的地or出発地のどちらをセットしている段階か

    //セッティングメニューはアプリの中で常に一つ
    SettingMenuFragment settingMenuFragment = new SettingMenuFragment();
    // NavigationMapFragment navigationMapFragment = new NavigationMapFragment();
    //PlacePickerは選択のたび生成/破棄されるみたいなのでここで生成しない
    //PlacePickerFragment placePickerFragment = new PlacePickerFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        // コードからFragmentを追加

//        // Fragmentを作成します
//
        // Fragmentの追加や削除といった変更を行う際は、Transactionを利用します
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        // 新しく追加を行うのでaddを使用します
        // 他にも、メソッドにはreplace removeがあります
        // メソッドの1つ目の引数は対象のViewGroupのID、2つ目の引数は追加するfragment
        //addToBackStackは前の画面に戻る際に必要

        //なぜかこいつらが先だとダメ。
        NavigationMapFragment navigationMapFragment = new NavigationMapFragment();
        transaction.add(R.id.container, settingMenuFragment).addToBackStack(null);
        //transaction.add(R.id.container, navigationMapFragment).addToBackStack(null);

        //目的地選択のPlacePickerを最前面に表示
        //PlacePickerFragment placePickerFragment = new PlacePickerFragment();
        //なぜかこいつらが先だとダメ。(たぶん、PlacePickerFragmentのなかでさらにPlacePickerのフラグメントを作成しているから？)
        //たぶん、ネストされたplacepickerフラグメントから抜ければメニューが表示されるとおもう
//        transaction.add(R.id.container, placePickerFragment).addToBackStack(null);



        // 最後にcommitしないと反映されない!
        transaction.commit();
    }

    //PlacePicker上で目的地が選択された場合に呼ばれるコールバックメソッド(使い方あってる...?)
    public void onPlacePickerFragmentChosen(Place chosenPlace){
        Log.i("AAA", "PlacePicked!");
        //SettingMenu画面の目的地/出発地情報を変更します
        if(isChoosingDistination){
            distination = chosenPlace;
            settingMenuFragment.setDestinationInfo(chosenPlace);
        }
        else{
            origin = chosenPlace;
            settingMenuFragment.setOriginInfo(chosenPlace);
        }
    }

    //SettingMenuFragment上のボタンが押された時に呼ばれるコールバックメソッド
    public  void onClickSettingMenuButton(View buttonView){
        if(buttonView.getId()==R.id.startNavigation){
            Log.i("AAA", "CALL START NAVI");
            //マップを表示する
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            NavigationMapFragment navigationMapFragment = new NavigationMapFragment();
            transaction.replace(R.id.container, navigationMapFragment).addToBackStack(null);
            transaction.commit();
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


}
