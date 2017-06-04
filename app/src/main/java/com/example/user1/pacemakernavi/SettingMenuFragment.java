package com.example.user1.pacemakernavi;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;
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

    private SettingMenuFragment.SettingMenuFragmentListener listener = null;    //ボタンを押したときにコールバックメソッドを呼ぶ(呼ばれるリスナー)
    public int durationOfRoute = 0; //目的地までの時間(秒数)
    public int distance = 0;    //目的地までの距離(メートル)
    public int targetTimeParcent = 0;   //通常の何パーセントの時間で目的地に着くのを目標とするか(パーセント)

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

            //画面に距離と時間を表示
            distanceInfo.setText("DISTANCE: " + distanceJson.getString("text") + "       DURATION: " + durationJson.getString("text"));
            durationOfRoute = durationJson.getInt("value");
            distance = distanceJson.getInt("value");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //このPlacePickerのイベント?のリスナー。このアプリでは通常、リスナーはMainActivityを想定している。
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
        SeekBar timeSetBar = ((SeekBar) getActivity().findViewById(R.id.timebar));
        timeSetBar.setOnSeekBarChangeListener(
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
                        targetTimeInformation.setText("TARGET DURATION: " + durationOfRoute * targetTimeParcent * 0.01 + "sec (" + targetTimeParcent + "%)" + "   SPEED: " + (distance / durationOfRoute * targetTimeParcent * 0.01) + "m/s");
                    }
                }
        );
        targetTimeParcent = timeSetBar.getProgress();
    }

    //目的地・出発地の情報をセット
    //今は目的地/出発地は名前しか表示してないけど後々は他の情報も載せたい
    public void setDestinationInfo(Place place){
        TextView destinationInfoText = (TextView)getActivity().findViewById(R.id.destinationInformation);
        destinationInfoText.setText(place.getName());
        setPlaceImage(place, (ImageView) getActivity().findViewById(R.id.destinationImage), 1000, 1000); //PlacePhotoを取得して表示
    }
    public  void setOriginInfo(Place place){
        TextView originInfoText = (TextView)getActivity().findViewById(R.id.originInformation);
        originInfoText.setText(place.getName());
        setPlaceImage(place, (ImageView) getActivity().findViewById(R.id.originImage), 1000, 1000);  //PlacePhotoを取得して表示
    }

    //経路情報の取得(終わった後、コールバックメソッドとしてonResultOfGoogleMapsDistanceMatrixApiが呼ばれる)
    public void setDirectionInformation(LatLng origin, LatLng destination) {
        GoogleMapsDistanceMatrixApiClient.fetchData(origin, destination, this);
    }

    //選択した目的地や出発地の画像をセットするためのメソッドmHeightやmWidthは画像の最大サイズ...だったかな
    public void setPlaceImage(final Place place, final ImageView imageView, final int mHeight, final int mWidth) {

        /**
         * Holder for an image and its attribution.
         */
        class AttributedPhoto {

            public final CharSequence attribution;

            public final Bitmap bitmap;

            public AttributedPhoto(CharSequence attribution, Bitmap bitmap) {
                this.attribution = attribution;
                this.bitmap = bitmap;
            }
        }

        new AsyncTask<String, Void, AttributedPhoto>() {

            /**
             * Loads the first photo for a place id from the Geo Data API.
             * The place id must be the first (and only) parameter.
             */
            @Override
            public AttributedPhoto doInBackground(String... params) {
                if (params.length != 1) {
                    return null;
                }
                final String placeId = params[0];
                AttributedPhoto attributedPhoto = null;

                //GoogleApiclientを新たに作るのが面倒なのでFusedLoactionClientSingletonのものを使っている...こんがらがる原因かも
                PlacePhotoMetadataResult result = Places.GeoDataApi
                        .getPlacePhotos(FusedLocationClientSingleton.getInstance().mGoogleApiClient, placeId).await();

                if (result.getStatus().isSuccess()) {
                    PlacePhotoMetadataBuffer photoMetadataBuffer = result.getPhotoMetadata();

                    if (photoMetadataBuffer.getCount() > 0 && !isCancelled()) {
                        // Get the first bitmap and its attributions.
                        PlacePhotoMetadata photo = photoMetadataBuffer.get(0);
                        CharSequence attribution = photo.getAttributions();
                        // Load a scaled bitmap for this photo.
                        Bitmap image = photo.getScaledPhoto(FusedLocationClientSingleton.getInstance().mGoogleApiClient, mWidth, mHeight).await()
                                .getBitmap();

                        attributedPhoto = new AttributedPhoto(attribution, image);
                    }
                    // Release the PlacePhotoMetadataBuffer.
                    photoMetadataBuffer.release();
                }
                return attributedPhoto;
            }

            @Override
            public void onPostExecute(AttributedPhoto photo) {
                super.onPostExecute(photo);
                if (photo == null) return;
                imageView.setImageBitmap(photo.bitmap);
            }
        }.execute(place.getId());
    }
}
