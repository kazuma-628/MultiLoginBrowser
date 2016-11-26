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

    //Android タグ名
    static final String LOG_TAG = "MLB";
    //ブックマークファイルへのファイルパスを指定する
    public static final String BOOKMARK_LIST_FILENAME = "BookmarkList.txt";

    //ブラウザ
    WebView Browser;
    //アドレスバー
    EditText AddressBar;
    //ブックマークリスト
    ArrayList<String> BookmarkList;
    //ブックマークボタン
    Button BookmarkButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

        //起動時にキーボードを表示しない
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //部品IDを取得
        Browser = (WebView)findViewById(R.id.Browser);
        AddressBar = (EditText) findViewById(R.id.AddressBar);

        //////////////////////////////////////////
        // ボタンに関する初期処理

        BookmarkButton = (Button)findViewById(R.id.Bookmark);
        BookmarkButton.setOnClickListener(new ButtonListener());


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
        BookmarkList = new ArrayList<String>();

        //////////////////////////////////////////
        // ブックマークに関する初期処理

        //ブックマークのデータをファイルから読み込み
        try {
            //ファイルオープン（読み込み指定）
            FileInputStream File = openFileInput(BOOKMARK_LIST_FILENAME);
            //文字コードを指定してオープン
            BufferedReader Reader = new BufferedReader(new InputStreamReader(File, "UTF-8"));

            //1行ごとにURLを取り出す（最後の行まで）
            String UrlString;
            while ((UrlString = Reader.readLine()) != null) {
                //データが存在した場合は、アプリ内で管理するブックマークリストに追加
                BookmarkList.add(UrlString);
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
            if (-1 != BookmarkList.indexOf(url)) {
                BookmarkButton.setText("★");
            }
            else {
                BookmarkButton.setText("☆");
            }
        }
    }


    //ボタンが押されたときに処理するクラス
    public class ButtonListener implements View.OnClickListener {
        public void onClick(View v){
            Log.i(LOG_TAG, "ButtonListener onClick : ");
            //アドレスバーからURLの文字列を取り出す
            String UrlString = AddressBar.getText().toString();

            //ブックマークされている要素だった場合は削除
            if (-1 != BookmarkList.indexOf(UrlString)) {
                //重複していることを知らせるトーストを表示
                Toast.makeText(MainActivity.this, "ブックマークから削除", Toast.LENGTH_SHORT).show();
                //ブックマークボタンの表示を更新
                BookmarkButton.setText("☆");
            }
            //そうでない場合は新規追加
            else {
                //ブックマークリストにデータを追加
                BookmarkList.add(UrlString);

                //登録を完了したことを知らせるトーストを表示
                Toast.makeText(MainActivity.this, "ブックマークに追加", Toast.LENGTH_SHORT).show();
                //ブックマークボタンの表示を更新
                BookmarkButton.setText("★");

                //ブックマークをファイルに書き込む
                FileWriteBookmarkList();
            }
        }

        //ブックマークリストデータをファイルに書き込み
        protected void FileWriteBookmarkList ( ) {
            try {
                //ファイルのオープン（追記指定）
                FileOutputStream BookmarkListFile = openFileOutput(BOOKMARK_LIST_FILENAME, Context.MODE_APPEND);
                //文字コードを指定して書き込み実行
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(BookmarkListFile, "UTF-8"));

                //ブックマークリストを全部書き込む
                for(String UrlString : BookmarkList){
                    writer.append(UrlString);
                }
                //後処理（オープンしたファイルなどをクローズ）
                writer.close();
                BookmarkListFile.close();

                //エラーが起きた時の例外処理
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            }
        }
    }

    //キーが押下されたときの動作
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        //バックキーが押されたとき
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //ブラウザが前に戻れるか判定
            if (Browser.canGoBack()) {
                //戻れるなら戻す
                Browser.goBack();
                return false;
            } else {
                return super.onKeyDown(keyCode, event);
            }
        }
        //エンター（検索キーが押された場合）
        //アドレスバーにフォーカスが当たっているときの判定が必要な気が？
        else if ((keyCode == KeyEvent.KEYCODE_ENTER)) {
            //アドレスバーのテキスト内容を取得して設定
            Editable Text = AddressBar.getText();
            Browser.loadUrl(Text.toString());
            return super.onKeyDown(keyCode, event);
        } else {
            //戻れない場合はアクティビティを閉じる
            return super.onKeyDown(keyCode, event);
        }
    }

}
