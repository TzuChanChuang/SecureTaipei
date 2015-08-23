package com.example.alicechuang.safetymap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ActionBar;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class SigninActivity  extends AsyncTask<String,Void,String>{
    static public URLConnection conn;
    public URL url;
    static  public String link_get_score;
    static  public String link_signup;

    private android.support.v7.app.ActionBar status;
    private int byGetOrPost = 0;

    //flag 0 means get and 1 means post.(By default it is get.)
    public SigninActivity(int flag) {

        byGetOrPost = flag;


        String username = "root";
        String password = "mnipoiperi";
        link_get_score = "http://106.184.0.211/get_score.php?username="+username+"&password="+password;
        link_signup = "http://106.184.0.211/sign_up.php?username="+username+"&password="+password;

    }

    protected void onPreExecute(){

    }

    @Override
    protected String doInBackground(String...arg0) {
        if(byGetOrPost == 0){ //means by Get Method
            /*
            try{
                String username = "root";
                String password = "mnipoiperi";
                String link = "http://106.184.0.211/index.php?username="+username+"& password="+password;

                URL url = new URL(link);
                client = new DefaultHttpClient();
                HttpGet request = new HttpGet();
                request.setURI(new URI(link));
                HttpResponse response = client.execute(request);
                BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                StringBuffer sb = new StringBuffer("");
                String line="";

                while ((line = in.readLine()) != null) {
                    sb.append(line);
                    break;
                }
                in.close();
                return sb.toString();
            }

            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }
            */
            return "";
        }
        else{
            try{


                /*String data  = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8");
                data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");*/

                url = new URL(link_signup);
                conn = url.openConnection();

                conn.setDoOutput(true);
                /*OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
//conn.setRequestProperty();
                wr.write( data );
                wr.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while((line = reader.readLine()) != null)
                {
                    sb.append(line);
                    break;
                }
                Log.e("shit", sb.toString());
                return sb.toString();*/
                return "";

            }
            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }
        }
    }

    @Override
    protected void onPostExecute(String result){
        Log.e("DB", "Success!!!!!!!!!!");
        Log.e("DB", result);
    }
}
