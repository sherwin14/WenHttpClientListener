package pitao.sherwin.com.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Android on 8/19/2015.
 */
public class WenHttpClientListener {

    private ContentValues contentValues;


    public ContentValues getParamaters() {
        return contentValues;
    }

    public void setParamaters(ContentValues contentValues) {
        this.contentValues = contentValues;
    }

    public interface IResponseCollector {
        void onSuccess(int code,String response);
        void onFailure(int code,String message);
    }

    public void Post(String Url,ContentValues Params,IResponseCollector AsyncResponseCollector){
        this.contentValues = Params;
        new AsyncPost(AsyncResponseCollector).execute(Url);
    }

    public void Post(String Url,IResponseCollector AsyncResponseCollector){
        new AsyncPost(AsyncResponseCollector).execute(Url);
    }

    private class AsyncPost extends AsyncTask<String,Integer,String>{
        String inputStr;
        StringBuilder sb = new StringBuilder();
        IResponseCollector responseCollector;

        private AsyncPost(IResponseCollector responseCollector) {
            this.responseCollector = responseCollector;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {

                URL url = new URL(strings[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(generatePayload(getParamaters()));
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

                if(conn.getResponseCode()==200){
                    InputStream is = conn.getInputStream();
                    BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                    while ((inputStr = streamReader.readLine()) != null) {
                        sb.append(inputStr);
                    }

                }
                sb.append(";;"+conn.getResponseCode());

            }catch (MalformedURLException m){

            }catch (IOException i){

            }
            return sb.toString();
        }

        @Override
        protected void onPostExecute(String string) {
           String[] result = string.split(";;");
           if((Integer.parseInt(result[1].toString()) != 200)){
               responseCollector.onFailure(Integer.parseInt(result[1].toString()), result[0].toString());
           }else{
               responseCollector.onFailure(Integer.parseInt(result[1].toString()),result[0].toString());
           }
        }
    }

    private String generatePayload(ContentValues values){
        StringBuilder builder = new StringBuilder();
        for(String val: values.keySet()) {
            try {
                builder.append("&");
                builder.append(URLEncoder.encode(val, "UTF-8"));
                builder.append("=");
                builder.append(URLEncoder.encode(values.get(val).toString(), "UTF-8"));

            }catch (UnsupportedEncodingException u){}
        }
        return  builder.substring(1,builder.length());
    }

}
