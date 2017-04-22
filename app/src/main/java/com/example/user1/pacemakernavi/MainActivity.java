package com.example.user1.pacemakernavi;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;


/**
 * Created by user1 on 2017/04/17.
 */

//目的地設定などをします
public class MainActivity extends Activity implements  PlacePickerFragment.PlacePickerFragmentListener, SettingMenuFragment.SettingMenuFragmentListener {

    private Place destination = null;   //セットされた目的地
    private Place origin = null;    //セットされた出発地
    private boolean isChoosingDistination = true;   //現在、目的地or出発地のどちらをセットしている段階か

    //セッティングメニューはアプリの中で常に一つ
    SettingMenuFragment settingMenuFragment = new SettingMenuFragment();
    //PlacePickerは選択のたび生成/破棄されるみたいなのでここで生成しない
    //PlacePickerFragment placePickerFragment = new PlacePickerFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        // コードからFragmentを追加

        // Fragmentの追加や削除といった変更を行う際は、Transactionを利用します
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        transaction.add(R.id.container, settingMenuFragment).addToBackStack(null);

        // 最後にcommitしないと反映されない!
        transaction.commit();
    }

    //PlacePicker上で目的地が選択された場合に呼ばれるコールバックメソッド(使い方あってる...?)
    public void onPlacePickerFragmentChosen(Place chosenPlace){
        Log.i("AAA", "PlacePicked!");
        //SettingMenu画面の目的地/出発地情報を変更します
        if(isChoosingDistination){
            destination = chosenPlace;
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
            if(origin == null || destination == null){
                Toast.makeText(this, "DEST or ORIGIN is null!", Toast.LENGTH_SHORT).show();

                //return;
            }
            Log.i("AAA", "CALL START NAVI");
            //マップを表示する
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            NavigationMapFragment navigationMapFragment = new NavigationMapFragment();
            //目的地とか設定(このタイミングでやっていいの？ NavigationMapFragmentのonMapReadyが呼ばれるまえに設定しなくてはならない)
            navigationMapFragment.destination = destination;
            navigationMapFragment.origin = origin;
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
