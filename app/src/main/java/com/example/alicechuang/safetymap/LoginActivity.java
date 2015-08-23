package com.example.alicechuang.safetymap;

import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.StrictMode;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;


import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class LoginActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener{

    String username = "username";
    int id = 0;
    String account = "account";
    String password = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //getHashKey();

        ////連php
        String username= "root";
        String password= "123456";
        int Post=1;
        new SigninActivity(Post).execute(username, password);

        //FB
        //初始化FacebookSdk，記得要放第一行，不然setContentView會出錯
        FacebookSdk.sdkInitialize(getApplicationContext());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        LinearLayout loading_lin = (LinearLayout)findViewById(R.id.loading_linlay);
        loading_lin.setVisibility(View.GONE);
        LinearLayout type_lin = (LinearLayout)findViewById(R.id.choicetype_linlay);
        type_lin.setVisibility(View.VISIBLE);

        //FB
        initialFB();

        //Google
        initialGoogle();

        setButtonListener();
    }


    boolean isLogin = false;
    boolean isSignup = false;
    private void setButtonListener(){
        /*input_button Listener*/
        final Button input_button = (Button)findViewById(R.id.input_btn);
        final EditText account_edittext = (EditText)findViewById(R.id.account_editText);
        final EditText password_edittext = (EditText)findViewById(R.id.password_editText);
        input_button.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                //initial error toast
                Toast error_toast = Toast.makeText(LoginActivity.this, "error account or password", Toast.LENGTH_LONG);

                //get input
                account = account_edittext.getText().toString();
                password =password_edittext.getText().toString();

                //check and change activity
                if(isLogin){
                    if(isLoginCorrect()){
                        showLoading();

                        username = "Login"+account;
                        toMapActivity();
                    }
                    else {
                        error_toast.show();
                    }
                }

                if(isSignup){
                    if(isSignupCorrect()){
                        showLoading();

                        username = "Signup"+account;
                        /*get username*/
                        toMapActivity();
                    }
                    else {
                        error_toast.show();
                        //reinput
                    }
                }
            }
        });

        /*choice login type */
        Button login_btn = (Button)findViewById(R.id.login_btn);
        login_btn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                isLogin = true;
                changeLin();

            }
        });

        Button signup_btn = (Button)findViewById(R.id.signup_btn);
        signup_btn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                isSignup = true;
                changeLin();
            }
        });

        //fb
        FBloginButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                showLoading();
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile", "user_friends"));
            }
        });

    }

    /** change LinearLayout */
    private void changeLin(){
        LinearLayout input_lin = (LinearLayout)findViewById(R.id.input_linlay);
        LinearLayout type_lin = (LinearLayout)findViewById(R.id.choicetype_linlay);

        input_lin.setVisibility(View.VISIBLE);
        type_lin.setVisibility(View.GONE);
    }

    /** check password and username correct */
    private boolean isSignupCorrect(){
        return true;
    }
    private boolean isLoginCorrect(){
        return true;
    }

    /** change activity and send message*/
    private void toMapActivity(){
        Intent intent = new Intent();
        intent.setClass(LoginActivity.this, MapsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("USR_NAME", username);
        bundle.putInt("USR_ID", id);
        intent.putExtras(bundle);
        //Log.e("FB", "start map activity");
        startActivity(intent);

        //FB
        if(isFB){
            LoginManager.getInstance().logOut();
        }

        this.finish();
    }

    /** facebook */
    private boolean isFB = false;
    CallbackManager callbackManager;
    private Button FBloginButton;
    private void setCallBack() {

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            //登入成功
            @Override
            public void onSuccess(LoginResult loginResult) {
                //Log.e("FBLogin", "onSuccess");

                getFBinfo(loginResult);
                isFB = true;

                toMapActivity();
            }

            //登入取消
            @Override
            public void onCancel() {
                //Log.e("FBLogin", "onCancel");
            }

            //登入失敗
            @Override
            public void onError(FacebookException exception) {
                Log.e("FBLogin", exception.toString());
            }

        });
    }
    private void getFBinfo(LoginResult loginResult) {

        //accessToken之後或許還會用到 先存起來
        AccessToken accessToken = loginResult.getAccessToken();

        //send request and call graph api
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {

                    //當RESPONSE回來的時候
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {

                        //讀出姓名 ID FB個人頁面連結
                        //Log.d("FB", "complete");
                        //Log.d("FB", object.optString("name"));
                        //Log.d("FB", object.optString("id"));

                        id = object.optInt("id");
                        username = "FB" + object.optString("name");
                        password = Integer.toString(id);
                    }
                });

        //包入你想要得到的資料 送出request
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name");
        request.setParameters(parameters);

        //make request doesn't crash
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        request.executeAndWait();

    }
    private void initialFB() {
        //宣告callback Manager
        callbackManager = CallbackManager.Factory.create();
        //找到button
        FBloginButton = (Button) findViewById(R.id.FBlogin_button);

        setCallBack();
    }
    private void getHashKey(){

        PackageInfo info;
        try{
            info = getPackageManager().getPackageInfo("com.example.alicechuang.safetymap", PackageManager.GET_SIGNATURES);
            for(Signature signature : info.signatures)
            {      MessageDigest md;
                md =MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String KeyResult =new String(Base64.encode(md.digest(), 0));//String something = new String(Base64.encodeBytes(md.digest()));
                Log.e("hash key", KeyResult);
                Toast.makeText(this, "My FB Key is \n" + KeyResult, Toast.LENGTH_LONG).show();
            }
        }catch(PackageManager.NameNotFoundException e1){Log.e("name not found", e1.toString());
        }catch(NoSuchAlgorithmException e){Log.e("no such an algorithm", e.toString());
        }catch(Exception e){Log.e("exception", e.toString());}

    }

    /** google+ */
    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;
    /* Google client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;
    /**
     * A flag indicating that a PendingIntent is in progress and prevents us
     * <p/>
     * from starting further intents.
     */
    private boolean mIntentInProgress;
    private boolean mSignInClicked;
    private boolean isGoogleSignin = false;
    private boolean isGoogleBtn = false;
    private ConnectionResult mConnectionResult;
    private void initialGoogle(){
        // Build GoogleApiClient with access to basic profile
        // Initializing google plus api client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN) //影響顯示同意畫面?
                .build();

        //UI
        btnSignin = (Button)findViewById(R.id.sign_in_button);

        btnSignin.setOnClickListener(this);
    }
    private void signInWithGplus() {
        if (!mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        }
    }
    private void signOutFromGplus() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
        }
    }
    // Method to resolve any signin errors
    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (IntentSender.SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }
    //google+ UI
    private Button btnSignin;
    private Button btnSignout;
    //google fun
    //Fetching user's information name, email, profile pic
    private String googleUsername;
    private int googleId;
    private void getProfileInformation() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi
                        .getCurrentPerson(mGoogleApiClient);
                String personName = currentPerson.getDisplayName();
                String personPhotoUrl = currentPerson.getImage().getUrl();
                String personGooglePlusProfile = currentPerson.getUrl();
                String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
                String ID = currentPerson.getId();

                /*Log.e("Google+", "Name: " + personName + ", plusProfile: "
                        + personGooglePlusProfile + ", email: " + email
                        + ", Image: " + personPhotoUrl);*/

                googleUsername= personName;
                googleId = Integer.parseInt(ID);

            } else {
                //Toast.makeText(getApplicationContext(),"Person information is null", Toast.LENGTH_LONG).show();
                Log.e("Google+", "Person information is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Loading */
    private void showLoading(){
        LinearLayout loading_lin = (LinearLayout)findViewById(R.id.loading_linlay);
        loading_lin.setVisibility(View.VISIBLE);
        LinearLayout type_lin = (LinearLayout)findViewById(R.id.choicetype_linlay);
        type_lin.setVisibility(View.GONE);
        LinearLayout type_linlay = (LinearLayout)findViewById(R.id.choicetype_linlay);
        type_linlay.setVisibility(View.GONE);
    }

    private void unshowLoading(){
        LinearLayout loading_lin = (LinearLayout)findViewById(R.id.loading_linlay);
        loading_lin.setVisibility(View.GONE);
        LinearLayout type_lin = (LinearLayout)findViewById(R.id.choicetype_linlay);
        type_lin.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

        //google
        if (v.getId() == R.id.sign_in_button) {
            showLoading();

            if(!isGoogleSignin){//還沒登入
                //Toast.makeText(this, "login", Toast.LENGTH_LONG).show();
                signInWithGplus();
                isGoogleBtn = true;
            }
            else{//已經登入
                username = "Googole+" + googleUsername;
                id = googleId;

                toMapActivity();
            }

        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        //google
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        //google
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        // TODO Auto-generated method stub

        //google
        // onConnected indicates that an account was selected on the device, that the selected
        // account has granted any requested permissions to our app and that we were able to
        // establish a service connection to Google Play services.
        Log.d("Google+", "onConnected:" + bundle);

        mSignInClicked = false;
        //Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG).show();

        // Get user's information
        getProfileInformation();

        //有連上帳號
        isGoogleSignin = true;

        //如果有按button
        if(isGoogleBtn) {
            username = "Googole+" + googleUsername;
            id = googleId;

            toMapActivity();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        // TODO Auto-generated method stub

        //google
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // TODO Auto-generated method stub

        //google
        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an error in order to sign in. Refer to the javadoc for
        // ConnectionResult to see possible error codes.
        Log.d("Google+", "onConnectionFailed:" + result);

        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
                    0).show();
            return;
        }

        if (!mIntentInProgress) {
            // Store the ConnectionResult for later usage
            mConnectionResult = result;

            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to
                // resolve all
                // errors until the user is signed in, or they cancel.
                resolveSignInError();
            }
        }

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //FB
        callbackManager.onActivityResult(requestCode, resultCode, data);

        //google
        Log.d("Google+", "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //FB
        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        //FB
        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id==R.id.google_logout_btn){
            signOutFromGplus();
        }

        return super.onOptionsItemSelected(item);
    }
}
