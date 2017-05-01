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
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.vision.text.Text;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by user1 on 2017/04/18.
 */

//起動した時に表示される初めのメニューです
//目的地・出発地選択ボタンと、到着時刻・所要時間の設定などを行う予定だけど未実装。
public class SettingMenuFragment extends Fragment implements GoogleMapsDistanceMatrixApiClient.GoogleMapsDistanceMatrixApiListner {

    private SettingMenuFragment.SettingMenuFragmentListener listener = null;
    public int durationOfRoute = 0; //目的地までの時間
    public int targetTimeParcent = 0;

    //GoogleDistanceMatrixAPIをリクエストした後に、結果を受け取るコールバックメソッド
    @Override
    public void onResultOfGoogleMapsDistanceMatrixApi(String result) {
        TextView distanceInfo = (TextView) getActivity().findViewById(R.id.distanceInformation);

        try {
            //GoogleDistanceMatrixAPIのレスポンスを解釈
            JSONObject distanceResult = new JSONObject(result);
            JSONObject element = distanceResult.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(0);
            JSONObject distanceJson = element.getJSONObject("distance");
            JSONObject durationJson = element.getJSONObject("duration");
            Log.i("MainActivity", "DistanceMatrixAPI: DISTANCE: " + distanceJson.getString("text") + " DURATION: " + durationJson.getString("text"));
            //画面に表示
            distanceInfo.setText("DISTANCE: " + distanceJson.getString("text") + "       DURATION: " + durationJson.getString("text"));
            durationOfRoute = durationJson.getInt("value");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

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

    //TODO: Resume時にOnStartはもう一度実行されてしまうが、大丈夫なのか？
    @Override
    public void onStart() {
        super.onStart();

        //目的地/出発地を設定するボタンが押された時の処理をセット
        Button chooseDistinationButton = (Button)getActivity().findViewById(R.id.chooseDestinationButton);
        chooseDistinationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getActivity(), "hoge!", Toast.LENGTH_SHORT).show();
                //ここにボタンが押された時の処理。親のアクティビティで処理している
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

        //案内開始ボタン
        Button startNavigationButton = (Button)getActivity().findViewById(R.id.startNavigation);
        startNavigationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getActivity(), "hoge!", Toast.LENGTH_SHORT).show();
                listener.onClickSettingMenuButton(v);
            }
        });


        //下のシークバーの挙動のセットで、「インナークラスから参照されるときはfinalにして」って言われるので
        final TextView targetTimeInformation = (TextView) getActivity().findViewById(R.id.targetTimeInformation);
        //シークバーの挙動をセット
        ((SeekBar) getActivity().findViewById(R.id.timebar)).setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progress, boolean fromUser) {
                        // ツマミをドラッグしたときに呼ばれる
                    }

                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // ツマミに触れたときに呼ばれる
                    }

                    public void onStopTrackingTouch(SeekBar seekBar) {
                        //ツマミを離したときに呼ばれる
                        targetTimeParcent = seekBar.getProgress();
                        targetTimeInformation.setText("TARGET DURATION: " + durationOfRoute * targetTimeParcent * 0.01 + "sec (" + targetTimeParcent + "%)");
                    }
                }
        );
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

    public void setDirectionInformation(LatLng origin, LatLng destination) {
        GoogleMapsDistanceMatrixApiClient.fetchData(origin, destination, this);
    }


}
