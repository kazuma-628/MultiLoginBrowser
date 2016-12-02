package com.sibaken.multiloginbrowser;

import android.content.Context;
import android.util.Log;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;

public class Common {

    //Android タグ名
    static final String LOG_TAG = "MLB";
    //ブックマークファイルへのファイルパスを指定する
    public static final String BOOKMARK_LIST_FILENAME = "BookmarkList.txt";

    //ブックマーク情報を保持するクラス
    public static class BookmarkInfo implements Serializable {
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

    //ファイルからブックマークリストデータを読み込む
    public static void FileReadBookmarkList ( Context context, ArrayList<BookmarkInfo> BookmarkList ) {
        Log.i(LOG_TAG, "FileReadBookmarkList");
        //ブックマークのデータをファイルから読み込み
        try {
            //ファイルオープン（読み込み指定）
            FileInputStream File = context.openFileInput(BOOKMARK_LIST_FILENAME);
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

    //ブックマークリストデータをファイルに書き込み
    public static void FileWriteBookmarkList ( Context context, ArrayList<BookmarkInfo> BookmarkList ) {
        Log.i(LOG_TAG, "FileWriteBookmarkList");
        try {
            //ファイルのオープン（追記指定）
            FileOutputStream BookmarkListFile = context.openFileOutput(BOOKMARK_LIST_FILENAME, Context.MODE_PRIVATE);
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
    public static BookmarkInfo GetBookmarkList ( ArrayList<BookmarkInfo> BookmarkList, String Url ) {
        Log.i(LOG_TAG, "GetBookmarkList");

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
}
