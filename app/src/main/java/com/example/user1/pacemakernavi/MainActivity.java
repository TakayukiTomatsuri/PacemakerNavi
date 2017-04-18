package com.example.user1.pacemakernavi;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.app.Fragment;

import com.google.android.gms.location.places.Place;


/**
 * Created by user1 on 2017/04/17.
 */

//このアプリの全てを制御するActivity。
//各画面のレイアウトと処理はFragmentで持ちます。
//各画面(=Fragment)の切り替えや、仲を取り持つのがこのActivity。
public class MainActivity extends Activity implements  PlacePickerFragment.PlacePickerFragmentListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        // コードからFragmentを追加

        // Fragmentを作成します
        NavigationMapFragment navigationMapFragment = new NavigationMapFragment();


        // Fragmentの追加や削除といった変更を行う際は、Transactionを利用します
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        // 新しく追加を行うのでaddを使用します
        // 他にも、メソッドにはreplace removeがあります
        // メソッドの1つ目の引数は対象のViewGroupのID、2つ目の引数は追加するfragment
        //addToBackStackは前の画面に戻る際に必要

        //なぜかこいつらが先だとダメ。
//        transaction.add(R.id.container, navigationMapFragment).addToBackStack(null);

        //transaction.commit();

        //目的地選択のPlacePickerを最前面に表示
        PlacePickerFragment placePickerFragment = new PlacePickerFragment();
        //なぜかこいつらが先だとダメ。(たぶん、PlacePickerFragmentのなかでさらにPlacePickerのフラグメントを作成しているから？)
        //たぶん、ネストされたplacepickerフラグメントから抜ければメニューが表示されるとおもう
//        transaction.add(R.id.container, placePickerFragment).addToBackStack(null);

        SettingMenuFragment settingMenuFragment = new SettingMenuFragment();
        transaction.add(R.id.container, settingMenuFragment).addToBackStack(null);

        // 最後にcommitを使用することで変更を反映します
        transaction.commit();
    }

    //PlacePicker上で目的地が選択された場合に呼ばれるコールバックメソッド(...?)
    public void onPlacePickerFragmentChosen(Place chosenPlace){

    }


}
