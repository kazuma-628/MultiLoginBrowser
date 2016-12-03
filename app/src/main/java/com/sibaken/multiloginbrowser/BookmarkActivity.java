package com.sibaken.multiloginbrowser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;

import com.sibaken.multiloginbrowser.Common.*;
import static com.sibaken.multiloginbrowser.Common.*;

public class BookmarkActivity extends Activity {

    ArrayList<BookmarkInfo> BookmarkList;    //前画面からのブックマークリスト受け取り用変数用意
    ArrayAdapter<String> BookmarkAdapter;    //ブックマークアダプター
    ArrayList<String> BookmarkList_title;   //ブックマークのタイトルリスト

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);

        //前画面からデータを受け取る
        Intent intent = getIntent();
        BookmarkList = (ArrayList<BookmarkInfo>)intent.getSerializableExtra("BookmarkList");

        //ブックマークのタイトルだけを抽出する
        BookmarkList_title = new ArrayList<String>();
        for(BookmarkInfo BookmarkObj : BookmarkList) {
            BookmarkList_title.add(BookmarkObj.GetTitle());
        }

        //リストビューのレイアウトを設定
        BookmarkAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, BookmarkList_title);

        //リストビューの設定
        ListView BookmarkListView = (ListView) findViewById(R.id.BookmarkListView);
        BookmarkListView.setAdapter(BookmarkAdapter);

        //リストがクリックされたときに呼ばれるリスナーを登録
        BookmarkListView.setOnItemClickListener(new ListViewClickListener());

        // コンテキストメニュー登録（リストが長押しされたときに呼ばれる）
        registerForContextMenu(BookmarkListView);

    }

    /**
     * コンテキストメニュー生成時処理
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo info) {
        super.onCreateContextMenu(menu, view, info);
        //選択されたアイテム情報(idやposition)を取得する
        AdapterView.AdapterContextMenuInfo adapterInfo = (AdapterView.AdapterContextMenuInfo) info;

        menu.setHeaderTitle(BookmarkList.get(adapterInfo.position).GetTitle());
        menu.add("編集");
        menu.add("削除");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        //選択されたアイテム情報(idやposition)を取得する
        AdapterView.AdapterContextMenuInfo adapterInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        //削除が押された場合
        if("削除" == item.getTitle()){
            //ブックマークリストからデータを削除
            BookmarkList.remove(adapterInfo.position);
            //ブックマークリストに表示中のタイトルのみのリストからも削除
            BookmarkList_title.remove(adapterInfo.position);
            //削除したことをトーストで表示
            Toast.makeText(BookmarkActivity.this, "ブックマークから削除", Toast.LENGTH_SHORT).show();
            //ブックマークをファイルに書き込む
            FileWriteBookmarkList(BookmarkActivity.this, BookmarkList);
            //リストを更新したので表示更新
            BookmarkAdapter.notifyDataSetChanged();
        }
        return super.onContextItemSelected(item);
    }
    
    /**
     * リストがクリックされたときに呼ばれるリスナー
     */
    public class ListViewClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //リストに表示していたアイテム名をトーストで表示
            String text = (String)parent.getItemAtPosition(position);
            Toast.makeText(BookmarkActivity.this, text, Toast.LENGTH_SHORT).show();

            // 前の画面に戻る
            BackScreen(position);
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
            // 前の画面に戻る
            BackScreen(-1);
        }
        //それ以外は次にキーを流す（多分そういう意味）
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 前の画面に戻るときの処理
     */
    public void BackScreen(int position) {
        Log.i(LOG_TAG, "BackScreen");
        // インテントの生成
        Intent intent = new Intent();
        //ブックマークリストを前のアクティビティへ転送設定
        intent.putExtra("BookmarkList", BookmarkList);
        //押されたリストの場所のポジションを返す設定
        intent.putExtra("BookmarkList_position", position);

        // 返却したい結果ステータスをセットする（）
        // ブックマークボタンが押された場合は[position]が[-1]以外
        if(-1 != position){
            //リストが選択された
            setResult( ACTIVITY_RESULT_LIST_SELECT, intent );
        }
        else {
            //バックキーで戻る
            setResult( ACTIVITY_RESULT_BACK, intent );
        }

        // アクティビティを終了させる（前画面に戻る遷移）
        finish();
    }
}
