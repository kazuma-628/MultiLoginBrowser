package com.sibaken.multiloginbrowser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import java.util.ArrayList;
import com.sibaken.multiloginbrowser.Common.*;
import static com.sibaken.multiloginbrowser.Common.*;

public class MainActivity extends Activity {

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

        FileReadBookmarkList(MainActivity.this, BookmarkList);
    }

    /**
     * ブラウザクライアントの設定
     */
    public class BrowserClient extends WebViewClient {
        //ページの読み込み時の処理
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.i(LOG_TAG, "BrowserClient onPageStarted : " + url);
            //アドレスバーの表示更新
            AddressBar.setText(url);

            //ブックマークボタンの表示を更新
            BookmarkButtonUpdate( );
        }
    }


    /**
     * ブックマークボタンが押されたときに処理するクラス
     */
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
            Log.i(LOG_TAG, "BookmarkButtonListener Common");
            //現在表示中のアドレスを取得
            String UrlString = Browser.getUrl();
            //URLからブックマークリストにあるデータを取得
            BookmarkInfo BookmarkObj = GetBookmarkList(BookmarkList, UrlString);

            //ブックマークされていない場合は新規追加（nullであればデータが存在しない）
            if (null == BookmarkObj) {
                //ブックマークリストにページタイトルとURLを追加
                BookmarkList.add(new BookmarkInfo(Browser.getTitle(), UrlString));
                //登録を完了したことを知らせるトーストを表示
                Toast.makeText(MainActivity.this, "ブックマークに追加", Toast.LENGTH_SHORT).show();
            }
            //既に存在する場合はリストから削除
            else {
                //ブックマークリストからデータを削除
                BookmarkList.remove(BookmarkObj);
                //削除したことをトーストで表示
                Toast.makeText(MainActivity.this, "ブックマークから削除", Toast.LENGTH_SHORT).show();
            }
            //ブックマークボタンの表示を更新
            BookmarkButtonUpdate( );

            //ブックマークをファイルに書き込む
            FileWriteBookmarkList(MainActivity.this, BookmarkList);
        }

        //ブックマークメニューボタン処理
        protected void BookmarkMenu () {
            //ブックマークアクティビティへの移動を設定
            Intent intent = new Intent(MainActivity.this, BookmarkActivity.class);
            //ブックマークリストをアクティビティへ転送設定
            intent.putExtra("BookmarkList", BookmarkList);
            //画面遷移実行（返却値を考慮したActivityの起動を行う）
            startActivityForResult(intent, REQUEST_CODE_BOOKMARK);
        }
    }

    /**
     * キーが押下されたときの動作
     */
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

    /**
     * 前画面から復帰するときに呼ばれるメソッド
     */
    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent intent ) {
        super.onActivityResult(requestCode, resultCode, intent);

        // startActivityForResult()の際に指定したリクエストコードとの比較
        switch (requestCode) {
            //ブックマークアクティビティ
            case REQUEST_CODE_BOOKMARK:
                // 返却されてきたintentから値を取り出す
                BookmarkList = (ArrayList<BookmarkInfo>)intent.getSerializableExtra("BookmarkList");

                // ブックマークリストのアイテムが選択された場合はそのブックマークのURLを表示する
                if( ACTIVITY_RESULT_LIST_SELECT == resultCode ){
                    int position = intent.getIntExtra("BookmarkList_position", -1);
                    Browser.loadUrl(BookmarkList.get(position).GetUrl());
                }

                //ブックマークボタンの表示を更新
                BookmarkButtonUpdate( );
                break;
        }
    }

    /**
     * ブックマークボタンの表示更新処理
     */
    public void BookmarkButtonUpdate( ) {
        //現在表示中のアドレスを取得
        String UrlString = AddressBar.getText().toString();

        //ブックマークボタンの表示を更新
        //すでにブックマークされている要素だった場合
        if (null == GetBookmarkList(BookmarkList, UrlString)) {
            BookmarkButton.setText("☆");
        }
        else {
            BookmarkButton.setText("★");
        }
    }
}
