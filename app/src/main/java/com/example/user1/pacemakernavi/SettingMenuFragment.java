package com.example.user1.pacemakernavi;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by user1 on 2017/04/18.
 */

//メインメニューです
//目的地・出発地選択ボタンと、到着時刻・所要時間の設定などを行います。
public class SettingMenuFragment extends Fragment{

    // Fragmentで表示するViewを作成するメソッド
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // viewの作成
        return inflater.inflate(R.layout.fragment_setting_menu, container, false);
    }


}
