package com.sibaken.multiloginbrowser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;
import com.sibaken.multiloginbrowser.Common.*;
import static com.sibaken.multiloginbrowser.Common.*;

public class BookmarkActivity extends Activity {

    //前画面からのブックマークリスト受け取り用変数用意
    ArrayList<BookmarkInfo> BookmarkList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);

        //前画面からデータを受け取る
        Intent intent = getIntent();
        BookmarkList = (ArrayList<BookmarkInfo>)intent.getSerializableExtra("BookmarkList");

        //ブックマークのタイトルだけを抽出する
        ArrayList<String> BookmarkList_title = new ArrayList<String>();
        for(BookmarkInfo BookmarkObj : BookmarkList) {
            BookmarkList_title.add(BookmarkObj.GetTitle());
        }

        //リストビューのレイアウトを設定
        ArrayAdapter<String> Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, BookmarkList_title);

        //リストビューの設定
        ListView BookmarkListView = (ListView) findViewById(R.id.BookmarkListView);
        BookmarkListView.setAdapter(Adapter);

        //リストがクリックされたときに呼ばれるリスナーを登録
        BookmarkListView.setOnItemClickListener(new ListViewClickListener());
    }

    //リストがクリックされたときに呼ばれるリスナー
    public class ListViewClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //リストに表示していたアイテム名をトーストで表示
            String text = (String)parent.getItemAtPosition(position);
            Toast.makeText(BookmarkActivity.this, text, Toast.LENGTH_SHORT).show();

            // インテントの生成
            Intent intent = new Intent();
            //ブックマークリストを前のアクティビティへ転送設定
            intent.putExtra("BookmarkList", BookmarkList);
            //押されたリストの場所のポジションを返す設定
            intent.putExtra("BookmarkList_position", position);
            // 返却したい結果ステータスをセットする
            setResult( Activity.RESULT_OK, intent );
            // アクティビティを終了させる（前画面に戻る遷移）
            finish();
        }
    }
}
