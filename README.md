# PacemakerNavi
A navigation app with setting your pace in running (or driving).  
  
目標時刻までに目的地に到達することを支援するナビアプリです。
残念ながら音声はでません。
地図上に、理想的な移動スピードを表すゴースト（レースゲームで出て来る半透明のアレ）を紅白ラインで表示しています。 



### 案内設定画面
出発地と目的地の設定、あと本来の到着時間(Google Mapsによる案内)のどのくらいの割合で到着するよう案内するかをシークバーで選びます。  
(その地点にGoogle Maps上で投稿された写真がイメージとして表示されます)  
<img src="/readmeImages/image1112.png" style="zoom:0.7;">

### 検索画面
出発地や目的地を検索して選びます。  
<img src="/readmeImages/image111.png" style="zoom:0.7;">

### ナビゲーション画面
設定したペースでの案内をします。画面上に、ナビゲーションが理想の移動ペースで描かれていくのでその通りに移動すれば、好きなペースで目的地へ向かうことができます。(できるのか？)  
下に出ているのは端末の現在の速度と平均速度、ゴースト(案内ライン)の速度です。
<img src="/readmeImages/image1113.png" style="zoom:0.4;">
   
## 動作説明
利用するだけの場合は、この欄の説明は読まなくても大丈夫。飛ばしてください。  
![Alt appflow](/readmeImages/appflow.png)
  
上の図のようになってます。Activityが画面全体のコントロールをして、Fragmentが画面上のパーツのレイアウトとコントロールを持っている感じです。*当たり前体操?*  


## 要件
AndroidStudioがあれば動くと思います。  
Mac版のAndroidStudio2.3.2を使用、実機での確認はAndroid6.0のHuawei Mediapad m3だけでしかしていません。

## 使い方
Android StudioなどにGithubからクローンします。Mac版ではメニューバーのVCS>Git>Cloneからできるはずです。
あとはメニューバーのRun>Run appを選択し、起動したい機種を選べばok。

アプリを起動し、目的地などをセットしないままStartNaviボタンを押すと、規定の目的地/出発地でナビを開始します。
