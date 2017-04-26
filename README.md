# PacemakerNavi
A navigation app with setting your pace in running (or driving).  
  
目標時刻までに目的地に到達することを支援するナビアプリです。
残念ながら音声はでません。
地図上に、理想的な移動スピードを表すゴースト（レースゲームで出て来る半透明のアレ）を黒いラインで表示しています。 

![Alt apptop](/readmeImages/apptop.jpg)
  
まだ未実装ですが、到着時刻の編集と、右左折までの距離や、理想的な速度のコーチなどもする予定です。  

## 動作説明
利用するだけの場合は、この欄の説明は読まなくても大丈夫。飛ばしてください。  
![Alt appflow](/readmeImages/appflow.png)
  
上の図のようになってます。Activityが画面全体のコントロールをして、Fragmentが画面上のパーツのレイアウトとコントロールを持っているような感じです。  


## 要件
AndroidStudioがあれば動くと思います。  
Mac版のAndroidStudio2.3.2を使用、実機での確認はAndroid6.0のHuawei Mediapad m3だけでしかしていません。

## 使い方
Android StudioなどにGithubからクローンします。Mac版ではメニューバーのVCS>Git>Cloneからできるはずです。
あとはメニューバーのRun>Run appを選択し、起動したい機種を選べばok。

アプリを起動し、目的地などをセットしないままStartNaviボタンを押すと、規定の目的地/出発地でナビを開始します。
