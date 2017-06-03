package com.example.user1.pacemakernavi;

import android.app.Application;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.places.Place;

/**
 * Created by user1 on 2017/06/03.
 */


/*
グローバル変数みたいに情報を共有する
Aplicationの拡張クラスは1個だけ？みたいなので、
内部でやってることの都合でApplicationの拡張クラスである必要のあるFusedLocationClientSingletonだけを、
Applicationの拡張クラスとしている。
 */
public class GlobalAppInfoSingleton {

    private static GlobalAppInfoSingleton sInstance = null;   //唯一のインスタンス

    public Place getDestination() {
        return destination;
    }

    public void setDestination(Place destination) {
        this.destination = destination;
    }

    public Place getOrigin() {
        return origin;
    }

    public void setOrigin(Place origin) {
        this.origin = origin;
    }

    //-----共有したい変数たち
    Place destination = null, origin = null;

    //-----ココマデ共有したい変数たち

////    private FusedLocationClientSingleton fusedLocationClientSingleton = null;
//    @Override
//    public void onCreate() {
//        super.onCreate();
////        fusedLocationClientSingleton = new FusedLocationClientSingleton();
//        sInstance = this;
//    }

    private GlobalAppInfoSingleton() {
        sInstance = this;
    }

    //シングルトンパターンでは、インスタンスはこのクラスメソッドを介して行う
    public static GlobalAppInfoSingleton getInstance() {
        if (sInstance == null) {
            new GlobalAppInfoSingleton();
            Log.e("GlobalAppInfoSingleton", "Singleton instance is not generated.");
        }
        return sInstance;
    }
}
