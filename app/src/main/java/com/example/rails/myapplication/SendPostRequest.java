package com.example.rails.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.httpclient.HttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by rails on 29/9/16.
 */
public class SendPostRequest extends AsyncTask<String, Void, String>{

    private JSONObject jsonObject;
    private static String response;
    private Context context;
    public SendPostRequest(Context ctx){
        context = ctx;
    }

    public SendPostRequest(JSONObject jsonObject){
        this.jsonObject = jsonObject;
    }

    protected void onPreExecute(){}

    protected String doInBackground(String... arg0) {

        try {

            //URL url = new URL("wwww.ggoooooooggle..conm"); // here is your URL path
            URL url = new URL(arg0[0]); // here is your URL path
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(jsonObject));

            writer.flush();
            writer.close();
            os.close();

            int responseCode=conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {

                BufferedReader in=new BufferedReader(
                        new InputStreamReader(
                                conn.getInputStream()));
                StringBuffer sb = new StringBuffer("");
                String line="";

                while((line = in.readLine()) != null) {

                    sb.append(line);
                    break;
                }

                in.close();
                return sb.toString();
                //Log.d(String.valueOf(sb), "doInBackground: ");
                //Toast.makeText(context, " Response is " + responseCode, Toast.LENGTH_SHORT).show();

            }
            else {
                return new String(String.format("false : %d", responseCode));
               // System.out.print(" hello" , responseCode);
                //Toast.makeText(SendPostRequest.this, "" + response, Toast.LENGTH_SHORT).show();
            }
        }
        catch(Exception e){
            return new String(String.format("Exception: %s", e.getMessage()));
        }


    }

    @Override
    protected void onPostExecute(String result) {

    }


    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }

}
