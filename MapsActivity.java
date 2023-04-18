package com.example.finalproject;
import android.Manifest;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.google.android.gms.common.util.IOUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.location.Location;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

//public class MapsActivity extends FragmentActivity implements OnMyLocationButtonClickListener,
public class MapsActivity extends AppCompatActivity implements OnMyLocationButtonClickListener,
        OnMyLocationClickListener,
        OnMapReadyCallback ,
        ActivityCompat.OnRequestPermissionsResultCallback{

    private GoogleMap mMap;
    Dialog dialog;
    EditText title,snippet;
    Button create,cancel;
    FloatingActionButton listbtn,nearby,reset;
    ImageView favorite_btn,trash_btn,current;
    AlertDialog.Builder alertDialog;
    float current_lat,current_lng;
    boolean permissionDenied = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    DBHelper DH;
    int flag = 0;
    private final String DB_NAME = "marker.db";
    private String TABLE_NAME = "lat_lng";
    private final int DB_VERSION = 1;
    public JSONObject jsonObject;
    List<String> nearby_name = new ArrayList<>();
    List<Double> nearby_lat = new ArrayList<>();
    List<Double> nearby_lng = new ArrayList<>();
    List<Marker> nearby_maker = new ArrayList<Marker>();
    FusedLocationProviderClient fusedLocationProviderClient;
    String TAG = MapsActivity.class.getSimpleName() + "My";
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();//取得所有資料
    ArrayList<HashMap<String, String>> getNowArray = new ArrayList<>();//取得被選中的項目資料

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        initDialog();   
        initRemoveMarkerDialog();
        Stetho.initializeWithDefaults(this);
        DH = new DBHelper(this, DB_NAME
                , null, DB_VERSION, TABLE_NAME);//初始化資料庫
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        //listbtn = (Button)findViewById(R.id.listbtn);
        setListeners();

    }
    private void setListeners() {
        listbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MapsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        LatLng fcu = new LatLng(24.178693, 120.646740); //120.646740,24.178693
        mMap.moveCamera(CameraUpdateFactory.newLatLng(fcu));
        initInfoWindowClick(googleMap);
        initMapClick(googleMap);
        enableMyLocation();
        //mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        arrayList = DH.showAll();
        for(int i=0;i< arrayList.size();i++){
            String temp = (arrayList.get(i).get("Latlng")).substring(10,arrayList.get(i).get("Latlng").length()-1);
            String[] latlong = temp.split(",");
            double latitude = Double.parseDouble(latlong[0]);
            double longitude = Double.parseDouble(latlong[1]);
            LatLng latLng = new LatLng(latitude, longitude);
            Log.d(TAG,latLng.toString());
            String sign =arrayList.get(i).get("Taste_good");
            Log.d(TAG ,sign);
            if(arrayList.get(i).get("Taste_good").compareTo("good")==0){
                String uri = "@android:drawable/btn_star_big_on"; // change current ImageView
                int imageResource = getResources().getIdentifier(uri, null, getPackageName());
                googleMap.addMarker(new MarkerOptions().position(latLng).title(arrayList.get(i).get("Tittle")).snippet(arrayList.get(i).get("Snippet")).icon(BitmapDescriptorFactory.fromResource(imageResource)));
            }else if(arrayList.get(i).get("Taste_good").compareTo("bad")==0){
                String uri = "@android:drawable/ic_delete"; // change current ImageView
                int imageResource = getResources().getIdentifier(uri, null, getPackageName());
                googleMap.addMarker(new MarkerOptions().position(latLng).title(arrayList.get(i).get("Tittle")).snippet(arrayList.get(i).get("Snippet")).icon(BitmapDescriptorFactory.fromResource(imageResource)));
            }
        }
    }
    public void initMapClick(final GoogleMap googleMap){
        googleMap.setOnMapClickListener(latLng -> {
            title.setText(""); //set dialog content
            snippet.setText("");
            dialog.show();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            create.setOnClickListener((v) -> {
                if(title.getText().toString().isEmpty()||snippet.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(),"Text fields cannot be empty",Toast.LENGTH_SHORT).show();
                }
                else if(flag==1) {
                    String uri = "@android:drawable/btn_star_big_on"; // change current ImageView
                    int imageResource = getResources().getIdentifier(uri, null, getPackageName());
                    googleMap.addMarker(new MarkerOptions().position(latLng).title(title.getText().toString()).snippet(snippet.getText().toString()).icon(BitmapDescriptorFactory.fromResource(imageResource)));
                    flag = 0;
                    String uri2 = "@android:drawable/menuitem_background"; // change current ImageView
                    int imageResource2 = getResources().getIdentifier(uri2, null, getPackageName());
                    Drawable image = getResources().getDrawable(imageResource2);
                    current.setImageDrawable(image);
                    DH.addData(latLng.toString(),"good",title.getText().toString(),snippet.getText().toString());
                    dialog.dismiss();
                }else if(flag==2){
                    String uri = "@android:drawable/ic_delete"; // change current ImageView
                    int imageResource = getResources().getIdentifier(uri, null, getPackageName());
                    googleMap.addMarker(new MarkerOptions().position(latLng).title(title.getText().toString()).snippet(snippet.getText().toString()).icon(BitmapDescriptorFactory.fromResource(imageResource)));
                    flag = 0;
                    String uri2 = "@android:drawable/menuitem_background"; // change current ImageView
                    int imageResource2 = getResources().getIdentifier(uri2, null, getPackageName());
                    Drawable image = getResources().getDrawable(imageResource2);
                    current.setImageDrawable(image);
                    DH.addData(latLng.toString(),"bad",title.getText().toString(),snippet.getText().toString());
                    dialog.dismiss();
                }else {
                    Toast.makeText(getApplicationContext(),"Please choose marker and try again",Toast.LENGTH_LONG).show();
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    flag = 0;
                    String uri2 = "@android:drawable/menuitem_background"; // change current ImageView
                    int imageResource2 = getResources().getIdentifier(uri2, null, getPackageName());
                    Drawable image = getResources().getDrawable(imageResource2);
                    current.setImageDrawable(image);
                    dialog.dismiss();
                }
            });
            favorite_btn = (ImageView)dialog.findViewById(R.id.good);
            trash_btn = (ImageView)dialog.findViewById(R.id.bad);
            current = (ImageView)dialog.findViewById(R.id.current) ;
            favorite_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String uri = "@android:drawable/btn_star_big_on"; // change current ImageView
                    int imageResource = getResources().getIdentifier(uri, null, getPackageName());
                    Drawable image = getResources().getDrawable(imageResource);
                    current.setImageDrawable(image);
                    flag = 1;
                }
            });
            trash_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String uri = "@android:drawable/ic_delete"; // change current ImageView
                    int imageResource = getResources().getIdentifier(uri, null, getPackageName());
                    Drawable image = getResources().getDrawable(imageResource);
                    current.setImageDrawable(image);
                    flag = 2;
                }
            });

        });
        nearby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                get_location(googleMap);
            }
        });
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAllMarkers();
                }

        });

    }
    public void initDialog(){ // bound layout Unit
        dialog = new Dialog(this);
        dialog.setTitle("add marker");
        dialog.setContentView(R.layout.dialog); //change layout
        title = (EditText)dialog.findViewById(R.id.title);
        snippet = (EditText)dialog.findViewById(R.id.snippet);
        create = (Button)dialog.findViewById(R.id.create);
        cancel = (Button)dialog.findViewById(R.id.cancel);
        nearby = findViewById(R.id.btn);
        reset = findViewById(R.id.restbtn);
        listbtn = findViewById(R.id.floatingActionButton);
    }

    public void initRemoveMarkerDialog(){ // alert dialog setting
        alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Delete Marker !");
        alertDialog.setMessage("Do you want to remove the marker?");
    }

    public  void initInfoWindowClick(GoogleMap googleMap){
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) { // set alertDialog btn and show
                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG,marker.getPosition().toString());
                        if(marker.getSnippet()=="")
                        {
                            //marker.setVisible(false);
                            marker.remove();
                        }
                        else
                        {
                            marker.remove();
                            DH.deleteByIdEZ(marker.getPosition().toString());
                        }

                    }
                });
                alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog.show();
            }

        });
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
    }

    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG)
                .show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT)
                .show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Permission was denied. Display an error message
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true;
        }
    }
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            permissionDenied = false;
        }
    }
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    public void nearby_request(Location location) throws JSONException {
        String key = "AIzaSyAWWNbn8GKVIOIUmaE3JOVp3-OMGJyJJeY";
        String latlng_for_nearby = Double.toString(location.getLatitude())+"," + Double.toString(location.getLongitude());
        Log.d(TAG, latlng_for_nearby);
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?key="+key+"&location="+latlng_for_nearby +"&language=zh-TW&radius=100&type=restaurant";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.isSuccessful()){
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("results");
                        for(int i=0;i<jsonArray.length();i++){
                            nearby_name.add(jsonArray.getJSONObject(i).getString("name"));
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            //Log.d(TAG,jsonObject1.toString());
                            JSONObject jsonObject2 = jsonObject1.getJSONObject("geometry");
                            JSONObject jsonObject3 = jsonObject2.getJSONObject("location");
                            nearby_lat.add(jsonObject3.getDouble("lat"));
                            nearby_lng.add(jsonObject3.getDouble("lng")) ;
                            Log.d(TAG,(nearby_name.get(0)));
                            Log.d(TAG,(Double.toString(nearby_lat.get(0))));
                            Log.d(TAG,(Double.toString(nearby_lng.get(0))));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    public void get_location(GoogleMap googleMap){
        if(ActivityCompat.checkSelfPermission(MapsActivity.this
                ,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Location> task) {
                    Location location = new Location(task.getResult());
                    try {
                        nearby_request(location);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    for(int i=0;i<nearby_name.size();i++){
                        LatLng latLng = new LatLng(nearby_lat.get(i),nearby_lng.get(i));
                        Marker marker = googleMap.addMarker(new MarkerOptions().position(latLng).title(nearby_name.get(i)));
                        nearby_maker.add(marker);
                    }
                    nearby_name.clear();
                    nearby_lat.clear();
                    nearby_lng.clear();
                }
            });
        }else{
            ActivityCompat.requestPermissions(MapsActivity.this
            ,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44);
        }
    }
    private void removeAllMarkers() {
        for (Marker marker: nearby_maker) {
            marker.remove();
        }
        nearby_maker.clear();

    }
}
