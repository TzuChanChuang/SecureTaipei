package com.example.alicechuang.safetymap;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import android.location.Location;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.Marker;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;


public class MapsActivity extends ActionBarActivity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener, OnMapReadyCallback {///, SearchView.OnQueryTextListener

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    // Google API用戶端物件
    private GoogleApiClient googleApiClient;

    // Location請求物件
    private LocationRequest locationRequest;

    // 記錄目前最新的位置ß
    private Location currentLocation;

    // 顯示目前與儲存位置的標記物件
    private Marker currentMarker, itemMarker;

    //////Move to current location////
    private int CurrentLocationStart = 0;

    //////////timer
    long startTime = 0;
    long millis;
    int seconds;
    int minutes;
    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            millis = System.currentTimeMillis() - startTime;
            seconds = (int) (millis / 1000);
            minutes = seconds / 60;
            seconds = seconds % 60;

            android.support.v7.app.ActionBar show= getSupportActionBar();
            show.setTitle(minutes+":"+seconds);
            //timerTextView.setText(String.format("%d:%02d", minutes, seconds));

            timerHandler.postDelayed(this, 500);

            if(minutes==1){
                startTime = System.currentTimeMillis();
                Notification();
            }
        }
    };


    @Override
    public void onMapReady(GoogleMap map) {
        LatLng NTU = new LatLng(25.017353, 121.539848);     //NTU
        currentMarker = map.addMarker(new MarkerOptions().position(NTU).title("Marker in NTU"));
        map.moveCamera(CameraUpdateFactory.newLatLng(NTU));
        moveMap(NTU);
    }


    // ConnectionCallbacks
    @Override
    public void onConnected(Bundle bundle) {
        // 已經連線到Google Services
        // 啟動位置更新服務
        // 位置資訊更新的時候，應用程式會自動呼叫LocationListener.onLocationChanged
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, MapsActivity.this);
    }

    // ConnectionCallbacks
    @Override
    public void onConnectionSuspended(int i) {
        // Google Services連線中斷
        // int參數是連線中斷的代號
    }

    // OnConnectionFailedListener
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Google Services連線失敗
        // ConnectionResult參數是連線失敗的資訊
        int errorCode = connectionResult.getErrorCode();

        // 裝置沒有安裝Google Play服務
        if (errorCode == ConnectionResult.SERVICE_MISSING) {
            Toast.makeText(this, R.string.google_play_service_missing,
                    Toast.LENGTH_LONG).show();
        }
    }

    // LocationListener
    @Override
    public void onLocationChanged(Location location) {
        // 位置改變
        // Location參數是目前的位置
        currentLocation = location;
        LatLng latLng = new LatLng(
                location.getLatitude(), location.getLongitude());

        if(CurrentLocationStart==1){
            if (currentMarker == null) {
                currentMarker = mMap.addMarker(new MarkerOptions().position(latLng));
            }
            else{
                currentMarker.setPosition(latLng);
            }
            moveMap(latLng);
            CurrentLocationStart = 0;

        }
        
        Circle circle = mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(300)    //meters
                .strokeColor(Color.BLACK)
                .fillColor(Color.RED));
    }

    // 移動地圖到參數指定的位置
    private void moveMap(LatLng place) {
        // 建立地圖攝影機的位置物件
        CameraPosition cameraPosition =
                new CameraPosition.Builder()
                        .target(place)
                        .zoom(17)
                        .build();

        // 使用動畫的效果移動地圖
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    // 在地圖加入指定位置與標題的標記
    private void addMarker(LatLng place, String title, String snippet) {
        BitmapDescriptor icon =
                BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(place)
                .title(title)
                .snippet(snippet)
                .icon(icon);

        itemMarker = mMap.addMarker(markerOptions);
    }

    // 建立Google API用戶端物件
    private synchronized void configGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    // 建立Location請求物件
    private void configLocationRequest() {
        locationRequest = new LocationRequest();
        // 設定讀取位置資訊的間隔時間為一秒（1000ms）
        locationRequest.setInterval(1000);
        // 設定讀取位置資訊最快的間隔時間為一秒（1000ms）
        locationRequest.setFastestInterval(1000);
        // 設定優先讀取高精確度的位置資訊（GPS）
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //get bubble
        getUserInfo();
        setUserInfo();

        initActionBar();
        initDrawer();
        initDrawerList();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        setUpMapIfNeeded();

        // 建立Google API用戶端物件
        configGoogleApiClient();

        // 建立Location請求物件
        configLocationRequest();

        // 讀取記事儲存的座標
        Intent intent = getIntent();
        double lat = intent.getDoubleExtra("lat", 25.017353);
        double lng = intent.getDoubleExtra("lng", 121.539848);

        // 如果記事已經儲存座標
        if (lat != 25.017353 && lng != 121.539848) {
            // 建立座標物件
            LatLng itemPlace = new LatLng(lat, lng);
            // 加入地圖標記
            addMarker(itemPlace, intent.getStringExtra("title"),
                    intent.getStringExtra("datetime"));
            // 移動地圖
            moveMap(itemPlace);
        }
        else {
            // 連線到Google API用戶端
            if (!googleApiClient.isConnected()) {
                googleApiClient.connect();
            }
        }


        ///////for search bar
        // Getting a reference to the map
        mMap = mapFragment.getMap();

        //////Image Button--CurrentLocation
        SetupImageButton1_CurrentLocation();

        ////timer
        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);


    }

    /////////add notification///
    public void Notification(){
        Intent intent = new Intent(this, NotificationReceiverActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        Notification noti = new Notification.Builder(this)
                .setContentTitle("New mail from " + "test@gmail.com")
                .setContentText("Subject").setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent)
                .addAction(R.drawable.ic_launcher, "Call", pIntent)
                .addAction(R.drawable.ic_launcher, "More", pIntent)
                .setVibrate(new long[] { 200,200,200,200})
                .addAction(R.drawable.ic_launcher, "And more", pIntent).build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, noti);
    }

    public void SetupImageButton1_CurrentLocation(){
        ImageButton CurrentLocation =(ImageButton)findViewById(R.id.CurrentLocation);
        CurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CurrentLocationStart = 1;
            }
        });
    }

    @Override
    protected void onResume() {
            super.onResume();
        setUpMapIfNeeded();

        // 連線到Google API用戶端
        if (!googleApiClient.isConnected() && currentMarker != null) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // 移除位置請求服務
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient, this);
        }

        //timer
        //timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // 移除Google API用戶端連線
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    private void processController() {
        // 對話框按鈕事件
        final DialogInterface.OnClickListener listener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            // 更新位置資訊
                            case DialogInterface.BUTTON_POSITIVE:
                                // 連線到Google API用戶端
                                if (!googleApiClient.isConnected()) {
                                    googleApiClient.connect();
                                }
                                break;
                            // 清除位置資訊
                            case DialogInterface.BUTTON_NEUTRAL:
                                Intent result = new Intent();
                                result.putExtra("lat", 25.017353);
                                result.putExtra("lng", 121.539848);
                                setResult(Activity.RESULT_OK, result);
                                finish();
                                break;
                            // 取消
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

        // 標記訊息框點擊事件
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // 如果是目前位置標記
                if (marker.equals(currentMarker)) {
                    AlertDialog.Builder ab = new AlertDialog.Builder(MapsActivity.this);

                    ab.setTitle(R.string.title_current_location)
                            .setMessage(R.string.message_current_location)
                            .setCancelable(true);

                    ab.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent result = new Intent();
                            result.putExtra("lat", currentLocation.getLatitude());
                            result.putExtra("lng", currentLocation.getLongitude());
                            setResult(Activity.RESULT_OK, result);
                            finish();
                        }
                    });
                    ab.setNegativeButton(android.R.string.cancel, null);

                    ab.show();

                    return true;
                }

                return false;
            }
        });
    }

    //////////////////////////////////////////////////////////////////////////
    // An AsyncTask class for accessing the GeoCoding Web Service
    private class GeocoderTask extends AsyncTask<String, Void, List<Address>> {

        @Override
        protected List<Address> doInBackground(String... locationName) {
            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(getBaseContext());
            List<Address> addresses = null;

            try {
                // Getting a maximum of 3 Address that matches the input text
                addresses = geocoder.getFromLocationName(locationName[0], 3);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {

            if(addresses==null || addresses.size()==0){
                Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
            }

            // Clears all the existing markers on the map
            mMap.clear();

            // Adding Markers on Google Map for each matching address
            for(int i=0;i<addresses.size();i++){

                Address address = (Address) addresses.get(i);

                // Creating an instance of GeoPoint, to display in Google Map
                LatLng now = new LatLng(address.getLatitude(), address.getLongitude());

                String addressText = String.format("%s, %s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        address.getCountryName());

                MarkerOptions marker= new MarkerOptions();
                marker.position(now);
                marker.title(addressText);

                mMap.addMarker(marker);

                // Locate the first location
                if(i==0)
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(now));
            }
        }
    }



    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                //setUpMap();
                processController();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));

        // 建立位置的座標物件
        LatLng place = new LatLng(25.017353, 121.539848);   //台大
        // 移動地圖
        moveMap(place);

        // 加入地圖標記
        addMarker(place, "Hello!", " This is HackNTU!");
    }



    ///////////////////
    /////menu/////////
    //////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);

        /////
        try {
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
            searchView.setOnQueryTextListener(queryListener);
        }catch(Exception e){

        }
        //////
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        /** drawer*/
        //按到friend_button的反應
        if (id == R.id.friend_button) {
            layDrawer.closeDrawer(Gravity.LEFT);
            if(layDrawer.isDrawerOpen(Gravity.RIGHT))
                layDrawer.closeDrawer(Gravity.RIGHT);
            else
                layDrawer.openDrawer(Gravity.RIGHT);
            return true;
        }
        //按ic_drawer有反應
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    final private android.support.v7.widget.SearchView.OnQueryTextListener queryListener = new android.support.v7.widget.SearchView.OnQueryTextListener() {

        @Override
        public boolean onQueryTextChange(String newText) {

            //直接丟給filter

            //MessageListMainFragment.this.adapter.getFilter().filter(newText);
            return false;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            //Log.d(TAG, "submit:"+query);
            if(query!=null && !query.equals("")){
                new GeocoderTask().execute(query);
            }
            return false;
        }
    };

    ///////////////////////////
    /////////UI init///////////
    ///////////////////////////

    /**initial actionbar */
    android.support.v7.widget.Toolbar toolbar;
    private void initActionBar(){

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.main_menu);
        setSupportActionBar(toolbar);

        toolbar.hideOverflowMenu();

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);


        //要讓 ActionBar 左邊的 App icon 出現返回的箭號，可以設定
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //這個方法和 setDisplayHomeAsUpEnabled 會互相影響。
        //setHomeButtonEnabled 若為 true 則 App icon 可以被點選，若為 false 則無任何事件觸發
        getSupportActionBar().setHomeButtonEnabled(true);
    }


    /** initial Drawer */
    private DrawerLayout layDrawer;
    private ListView lsv_L_Drawer;
    //private LinearLayout liv_L_Drawer;
    private ListView lsv_R_Drawer;
    //private LinearLayout liv_R_Drawer;
    private ActionBarDrawerToggle drawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private void initDrawer(){
        //get view
        layDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        lsv_L_Drawer = (ListView) findViewById(R.id.lsv_left_menu);
        //liv_L_Drawer = (LinearLayout) findViewById(R.id.left_drawer);
        lsv_R_Drawer = (ListView) findViewById(R.id.lsv_right_menu);
        //liv_R_Drawer = (LinearLayout) findViewById(R.id.right_drawer);

        //layDrawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        layDrawer.setDrawerShadow(new ColorDrawable(Color.BLACK), Gravity.LEFT);
        layDrawer.setDrawerShadow(new ColorDrawable(Color.BLACK), Gravity.RIGHT);

        //mTitle = mDrawerTitle = getTitle();
        mTitle = getTitle();
        mDrawerTitle = "MENU";

        drawerToggle = new ActionBarDrawerToggle(
                this,
                layDrawer,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View view) {

                super.onDrawerClosed(view);

                getSupportActionBar().setTitle(mTitle);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if(layDrawer.isDrawerOpen(Gravity.LEFT))
                    layDrawer.closeDrawer(Gravity.RIGHT);

                if(layDrawer.isDrawerOpen(Gravity.LEFT))
                    mDrawerTitle = "Left Menu";
                else
                    mDrawerTitle = "Right Menu";
                getSupportActionBar().setTitle(mDrawerTitle);

                //check friend request
                if(layDrawer.isDrawerOpen(Gravity.RIGHT)){
                    if(hasFriReq){
                        setFriendRequest();
                    }
                }
            }

        };
        drawerToggle.syncState();

        layDrawer.setDrawerListener(drawerToggle);

        //側選單點選偵聽器
        lsv_L_Drawer.setOnItemClickListener(new DrawerItemClickListener());
        lsv_R_Drawer.setOnItemClickListener(new DrawerItemClickListener());
    }
    /** 側邊欄點選事件 */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }
    /** 選到ListView的時候 */
    private void selectItem(int position){
        //換標題文字
        if(layDrawer.isDrawerOpen(Gravity.LEFT))
            mDrawerTitle = (left_drawer_menu[position]);
        else
            mDrawerTitle = (right_drawer_menu[position]);
        getSupportActionBar().setTitle(mDrawerTitle);
    }

    /** initial DrawerList */
    String[] left_drawer_menu;
    String[] right_drawer_menu;
    private void initDrawerList(){
        //left
        left_drawer_menu = this.getResources().getStringArray(R.array.left_drawer_menu);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.drawer_list_item, left_drawer_menu);
        lsv_L_Drawer.setAdapter(adapter);
        //right
        get_friends_list();
        adapter = new ArrayAdapter<String>(this, R.layout.drawer_list_item, right_drawer_menu);
        lsv_R_Drawer.setAdapter(adapter);
    }
    /**跟資料庫要friend_list*/
    private void get_friends_list(){

        right_drawer_menu = new String[] {"friendA", "friendB"};

    }


    ///////////////////////////
    ////////user info//////////
    ///////////////////////////
    String username;
    int user_id;
    private void getUserInfo(){
        Bundle bundle = this.getIntent().getExtras();

        username = bundle.getString("USR_NAME");
        user_id = bundle.getInt("USR_ID");
    }

    private void setUserInfo() {
        TextView username_text = (TextView) findViewById(R.id.username_textv);
        username_text.setText(username);
    }


    ///////////////////////////
    ////////friend_list////////
    ///////////////////////////
    String fre_req_name = "fre_req_name";
    LinearLayout fri_req_linlay;
    boolean hasFriReq = true;
    private void setFriendRequest(){
        fri_req_linlay = (LinearLayout)findViewById(R.id.fri_req_lay);
        fri_req_linlay.setVisibility(View.VISIBLE);

        TextView fri_req_text = (TextView)findViewById(R.id.fri_req_text);

        fri_req_text.setText(this.getResources().getString(R.string.friend_request) + " " + fre_req_name );

        //set button and listener
        Button fri_req_OK_btn = (Button)findViewById(R.id.fri_req_OK_btn);
        Button fri_req_NO_btn = (Button)findViewById(R.id.fri_req_NO_btn);

        fri_req_OK_btn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                //confirm
                hasFriReq = false;

                fri_req_linlay.setVisibility(View.GONE);
            }
        });

        fri_req_NO_btn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                //拒絕
                hasFriReq = false;

                fri_req_linlay.setVisibility(View.GONE);
            }
        });
    }
}

