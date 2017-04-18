package com.example.user1.pacemakernavi;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;

/**
 * Created by user1 on 2017/04/18.
 */

//メインメニューです
//目的地・出発地選択ボタンと、到着時刻・所要時間の設定などを行います。
public class SettingMenuFragment extends Fragment{

    private SettingMenuFragment.SettingMenuFragmentListener listener = null;
    //このPlacePickerのイベント?のリスナー。このアプリではMainActivityを想定している。
    public interface SettingMenuFragmentListener {
        //押されたボタンを渡す。buttonView.getIdでどのボタンが押されたのか識別する
        void onClickSettingMenuButton(View buttonView);
    }

    //このFragmentがアタッチされたやつをリスナーとして登録する。
    //もしそれがPlacePickerFragmentListenerを実装してなければエラー。
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity;
        if (context instanceof Activity){
            activity=(Activity) context;

            // 実装されてなかったらException吐かせて実装者に伝える
            if (!(activity instanceof PlacePickerFragment.PlacePickerFragmentListener)) {
                throw new UnsupportedOperationException(
                        "Listener is not Implementation.");
            } else {
                // ここでActivityのインスタンスではなくActivityに実装されたイベントリスナを取得
                listener = (SettingMenuFragment.SettingMenuFragmentListener) activity;
            }
        }
    }

    // Fragmentで表示するViewを作成するメソッド
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // viewの作成
        return inflater.inflate(R.layout.fragment_setting_menu, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        //目的地/出発地を設定するボタンが押された時の処理
        Button chooseDistinationButton = (Button)getActivity().findViewById(R.id.chooseDestinationButton);
        chooseDistinationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getActivity(), "hoge!", Toast.LENGTH_SHORT).show();
                //ここにボタンが押された時の処理
                listener.onClickSettingMenuButton(v);
            }
        });

        Button chooseOriginButton = (Button)getActivity().findViewById(R.id.chooseOriginButton);
        chooseOriginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getActivity(), "hoge!", Toast.LENGTH_SHORT).show();
                listener.onClickSettingMenuButton(v);
            }
        });
    }

    //目的地・出発地の情報をセット
    //今は目的地/出発地は名前しか表示してないけど後々は他の情報も載せたい
    public void setDestinationInfo(Place place){
        TextView destinationInfoText = (TextView)getActivity().findViewById(R.id.destinationInformation);
        destinationInfoText.setText(place.getName());
    }
    public  void setOriginInfo(Place place){
        TextView originInfoText = (TextView)getActivity().findViewById(R.id.originInformation);
        originInfoText.setText(place.getName());
    }


}
