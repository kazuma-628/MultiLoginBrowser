package com.sibaken.multiloginbrowser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

public class MainActivity extends Activity {

    //ブックマーク情報を保持するクラス
    public class BookmarkInfo{
        //コンストラクタ
        public BookmarkInfo(String p_title, String p_Url){
            Title = p_title;  //タイトルを保持
            Url = p_Url;      //URLを保持
        }

        //データ取得IF
        public String GetTitle(){ return Title; }
        public String GetUrl(){ return Url; }

        private String Title;  //タイトル
        private String Url;    //URL
    }

    //Android タグ名
    static final String LOG_TAG = "MLB";
    //ブックマークファイルへのファイルパスを指定する
    public static final String BOOKMARK_LIST_FILENAME = "BookmarkList.txt";
    //ブックマークアクティビティとのやり取り用のリクエストコード
    static final int REQUEST_CODE_BOOKMARK = 1;

    WebView Browser;                     //ブラウザ
    EditText AddressBar;                //アドレスバー
    ArrayList<BookmarkInfo> BookmarkList;     //ブックマークリスト
    Button BookmarkButton;              //ブックマークボタン
    Button BookmarkMenuButton;         //ブックマークメニューボタン

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //起動時にキーボードを表示しない
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //部品IDを取得
        Browser = (WebView)findViewById(R.id.Browser);
        AddressBar = (EditText) findViewById(R.id.AddressBar);

        //////////////////////////////////////////
        // ボタンに関する初期処理

        //ブックマークボタン
        BookmarkButton = (Button)findViewById(R.id.Bookmark);
        BookmarkButton.setOnClickListener(new ButtonListener());

        //ブックマークメニューボタン
        BookmarkMenuButton = (Button) findViewById(R.id.BookmarkMenu);
        BookmarkMenuButton.setOnClickListener(new ButtonListener());

        //////////////////////////////////////////
        // ブラウザに関する初期処理

        //リンクをタップしたときに標準ブラウザを起動させない
        Browser.setWebViewClient(new BrowserClient());
        //javascriptを許可する
        Browser.getSettings().setJavaScriptEnabled(true);
        //拡大を許可する
        Browser.getSettings().setSupportZoom(true);
        //拡大縮小ボタンをつける
        Browser.getSettings().setBuiltInZoomControls(true);
        //WebViewのズームコントロールを非表示
        Browser.getSettings().setDisplayZoomControls(false);
        //最初に表示するブラウザを設定
        Browser.loadUrl("http://www.yahoo.co.jp/");

        //ブックマークのリストを作成
        BookmarkList = new ArrayList<BookmarkInfo>();

        //////////////////////////////////////////
        // アドレスバーに関する初期処理
        
        AddressBar.setOnKeyListener( new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //ボタンが押されてなおかつエンターキーだったとき
                if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                    // ソフトキーボードを隠す
                    ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);

                    //アドレスバーのテキスト内容を取得して設定（表示）
                    Editable Text = AddressBar.getText();
                    Browser.loadUrl(Text.toString());
                    return true;
                }
                return false;
            }
        });

        //////////////////////////////////////////
        // ブックマークに関する初期処理

        //ブックマークのデータをファイルから読み込み
        try {
            //ファイルオープン（読み込み指定）
            FileInputStream File = openFileInput(BOOKMARK_LIST_FILENAME);
            //文字コードを指定してオープン
            BufferedReader Reader = new BufferedReader(new InputStreamReader(File, "UTF-8"));

            //2行ごとにタイトル・URLを取り出す（すべて取り出す）
            //1行目がタイトル、2行目がURL
            String TitleString;
            while ((TitleString = Reader.readLine()) != null) {
                //タイトルがあればURLも取り出す
                String UrlString = Reader.readLine();

                //URLもあるはずだが念のためチェック
                if(UrlString != null) {
                    //データが存在した場合は、アプリ内で管理するブックマークリストに追加
                    BookmarkList.add(new BookmarkInfo(TitleString, UrlString));
                }
                //あるはずのデータがなけれなエラー
                else {
                    Log.e(LOG_TAG, "Not UrlString!");
                }
            }

            //後処理（オープンしたファイルのクローズなど）
            Reader.close();
            File.close();
        } catch (FileNotFoundException e) {
            //エラー発生時などの例外受け取り
        } catch (IOException e) {
            //エラー発生時などの例外受け取り
        }

    }

    //ブラウザクライアントの設定
    public class BrowserClient extends WebViewClient {
        //ページの読み込み時の処理
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.i(LOG_TAG, "BrowserClient onPageStarted : " + url);
            //アドレスバーの表示更新
            AddressBar.setText(url);

            //ブックマークボタンの表示を更新
            //すでにブックマークされている要素だった場合
            if (null == GetBookmarkList(url)) {
                BookmarkButton.setText("☆");
            }
            else {
                BookmarkButton.setText("★");
            }
        }
    }


    //ブックマークボタンが押されたときに処理するクラス
    public class ButtonListener implements View.OnClickListener {
        public void onClick(View v) {
            Log.i(LOG_TAG, "ButtonListener onClick");

            //押されたボタンごとに処理を分ける
            switch (v.getId()) {
                //ブックマークボタン
                case R.id.Bookmark:
                    Bookmark();
                    break;
                //ブックマークメニューボタン
                case R.id.BookmarkMenu:
                    BookmarkMenu();
            }
        }

        //ブックマークボタン処理
        protected void Bookmark () {
            Log.i(LOG_TAG, "BookmarkButtonListener Bookmark");
            //現在表示中のアドレスを取得
            String UrlString = Browser.getUrl();
            //URLからブックマークリストにあるデータを取得
            BookmarkInfo BookmarkObj = GetBookmarkList(UrlString);

            //ブックマークされていない場合は新規追加（nullであればデータが存在しない）
            if (null == BookmarkObj) {
                //ブックマークリストにページタイトルとURLを追加
                BookmarkList.add(new BookmarkInfo(Browser.getTitle(), UrlString));
                //登録を完了したことを知らせるトーストを表示
                Toast.makeText(MainActivity.this, "ブックマークに追加", Toast.LENGTH_SHORT).show();
                //ブックマークボタンの表示を更新
                BookmarkButton.setText("★");
            }
            //既に存在する場合はリストから削除
            else {
                //ブックマークリストからデータを削除
                BookmarkList.remove(BookmarkObj);
                //削除したことをトーストで表示
                Toast.makeText(MainActivity.this, "ブックマークから削除", Toast.LENGTH_SHORT).show();
                //ブックマークボタンの表示を更新
                BookmarkButton.setText("☆");

            }
            //ブックマークをファイルに書き込む
            FileWriteBookmarkList();
        }

        //ブックマークメニューボタン処理
        protected void BookmarkMenu () {

            //ブックマークのタイトルだけを抽出する
            ArrayList<String> BookmarkList_title = new ArrayList<String>();
            for(BookmarkInfo BookmarkObj : BookmarkList) {
                BookmarkList_title.add(BookmarkObj.GetTitle());
            }

            //ブックマークアクティビティへの移動を設定
            Intent intent = new Intent(MainActivity.this, BookmarkActivity.class);
            //ブックマークリストをアクティビティへ転送設定
            intent.putExtra("BookmarkList_title", BookmarkList_title);
            //画面遷移実行（返却値を考慮したActivityの起動を行う）
            startActivityForResult(intent, REQUEST_CODE_BOOKMARK);
        }
    }

    //キーが押下されたときの動作
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i(LOG_TAG, "onKeyDown : " + keyCode);
        //バックキーが押されたとき
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //ブラウザが前に戻れるか判定
            if (Browser.canGoBack()) {
                //戻れるなら戻す
                Browser.goBack();
                return false;
            }
        }
        //それ以外は次にキーを流す（多分そういう意味）
        return super.onKeyDown(keyCode, event);
    }

    //ブックマークリストデータをファイルに書き込み
    public void FileWriteBookmarkList ( ) {
        Log.i(LOG_TAG, "BookmarkButtonListener FileWriteBookmarkList");
        try {
            //ファイルのオープン（追記指定）
            FileOutputStream BookmarkListFile = openFileOutput(BOOKMARK_LIST_FILENAME, Context.MODE_PRIVATE);
            //文字コードを指定して書き込み実行
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(BookmarkListFile, "UTF-8"));

            //ブックマークリストを全部書き込む
            for(BookmarkInfo BookmarkObj : BookmarkList){
                //タイトルとURLを1行ごとに格納（2行で1セット）
                writer.append(BookmarkObj.GetTitle() + "\n");
                writer.append(BookmarkObj.GetUrl() + "\n");
            }
            //後処理（オープンしたファイルなどをクローズ）
            writer.close();
            BookmarkListFile.close();

            //エラーが起きた時の例外処理
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
    }

    //対象のURLが含まれるブックマークリストオブジェクトを取得
    protected BookmarkInfo GetBookmarkList ( String Url ) {
        Log.i(LOG_TAG, "BookmarkButtonListener FindBookmarkListToUrl");

        //ブックマークリストを全検索
        for(BookmarkInfo BookmarkObj : BookmarkList) {
            //URLで一致するものがあればオブジェクトを返却
            if(true == Url.equals(BookmarkObj.GetUrl())) {
                return BookmarkObj;
            }
        }
        //該当のRULがなければnullを返却
        return null;
    }

    //前画面から復帰するときに呼ばれるメソッド
    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent intent ) {
        super.onActivityResult(requestCode, resultCode, intent);

        // startActivityForResult()の際に指定したリクエストコードとの比較
        switch (requestCode) {
            //ブックマークアクティビティ
            case REQUEST_CODE_BOOKMARK:
                // 返却結果ステータスが正常なら
                if( Activity.RESULT_OK == resultCode ){
                    // 返却されてきたintentから値を取り出す
                    int position = intent.getIntExtra("BookmarkList_position", -1);
                    Browser.loadUrl(BookmarkList.get(position).GetUrl());
                }
                break;
        }
    }
}
