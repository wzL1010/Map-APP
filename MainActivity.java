package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cursoradapter.widget.SimpleCursorAdapter;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.CaseMap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    DBHelper DH;
    private final String DB_NAME = "marker.db";
    private String TABLE_NAME = "lat_lng";
    private final int DB_VERSION = 1;
    private ListView LV;
    String uriString,getLatlng,temp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        DH = new DBHelper(this, DB_NAME
                , null, DB_VERSION, TABLE_NAME);
        LV = (ListView) findViewById(R.id.listView);
        ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
        SQLiteDatabase db = DH.getWritableDatabase();
        // arrayList = DH.showAll();
        getCursor();
        showInList();
    }

    private Cursor getCursor() {
        SQLiteDatabase db = DH.getReadableDatabase();  //透過dbHelper取得讀取資料庫的SQLiteDatabase物件，可用在查詢
        String[] columns = {"_id", "Tittle", "Taste_good", "Snippet","Latlng"};
        Cursor cursor = db.query(TABLE_NAME, columns, null, null, null, null, null);  //查詢所有欄位的資料
        return cursor;
    }

    private void showInList() {
        Cursor cursor = getCursor();
        String[] from = {"_id", "Tittle", "Taste_good", "Snippet", "Latlng"};
        int[] to = {R.id.textView, R.id.textView2, R.id.textView3, R.id.textView4, R.id.textView5};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.listview, cursor, from, to); //SimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to)
        LV.setAdapter(adapter);

        LV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) view.findViewById(R.id.textView5);
                getLatlng = textView.getText().toString();
                temp= getLatlng.substring(9);
                //double latitude = Double.parseDouble(latlong[0]);
                // double longitude = Double.parseDouble(latlong[1]);
                //LatLng latLng=new LatLng(latitude, longitude);
                Toast.makeText(MainActivity.this, "導航至" + temp, Toast.LENGTH_SHORT).show();
                uriString="google.navigation:q="+temp+"&mode=w";
                launchMap();
                return false;
            }
        });
    }
    public void launchMap(){
        Uri intentUri = Uri.parse(uriString);
        Intent intent  = new Intent(Intent.ACTION_VIEW,intentUri);
        intent.setPackage("com.google.android.apps.maps");

        if(intent.resolveActivity(getPackageManager())!=null){
            startActivity(intent);
        }
    }
}