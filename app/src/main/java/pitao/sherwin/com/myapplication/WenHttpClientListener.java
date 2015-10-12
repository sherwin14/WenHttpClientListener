package pitao.sherwin.com.myapplication;

import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by Android on 8/19/2015.
 */
public class WenHttpClientListener {

    private ContentValues contentValues;
    private ContentValues contentHeaders;
    private ArrayList<Object> arrayListParams;

    public ArrayList<Object> getArrayListParams() {
        return arrayListParams;
    }

    public void setArrayListParams(ArrayList<Object> arrayListParams) {
        this.arrayListParams = arrayListParams;
    }

    public ContentValues getContentHeaders() {
        return contentHeaders;
    }

    public void setContentHeaders(ContentValues contentHeaders) {
        this.contentHeaders = contentHeaders;
    }

    public ContentValues getParamaters() {
        return getContentValues();
    }

    public void setParamaters(ContentValues contentValues) {
        this.setContentValues(contentValues);
    }

    public ContentValues getContentValues() {
        return contentValues;
    }

    public void setContentValues(ContentValues contentValues) {
        this.contentValues = contentValues;
    }

    public interface IResponseCollector {
        void onSuccess(int code, String response);
        void onProgress(Object progress);
        void onFailure(int code, String message);
    }

    /* Method name: Post
     * Parameters:  Url (String) , Params (ContentValues) , AsyncResponseCollector (IResponseCollector)(Interface)
     * Description: Used to submit data to WEB SERVER
     * Author: Sherwin Pitao
     * Date Created: 08/19/2015
     */
    public void Post(String Url,ContentValues Params,IResponseCollector AsyncResponseCollector){
        this.setContentValues(Params);
        new AsyncPost(AsyncResponseCollector).execute(Url);
    }

    /* Method name: Get
     * Parameters:  Url (String), Params (ArrayList<Object>),Headers(ContentValues), AsyncResponseCollector (IResponseCollector)(Interface)
     * Description: Used to retrieve data to WEB SERVER
     * Author: Sherwin Pitao
     * Date Created: 08/20/2015
     */
    public void Get(String Url,ArrayList<Object> Params,ContentValues Headers,IResponseCollector AsyncResponseCollector){
        this.setArrayListParams(Params);
        this.setContentHeaders(Headers);
        Uri.Builder builder = Uri.parse(Url).buildUpon();
        if(Params != null){
            for(Object param: Params){
                builder.appendPath(param.toString());
            }
        }

        try {
            URI uri = new URI(builder.build().toString());
            new AsyncGet(AsyncResponseCollector).execute(uri.toString());
        }catch (URISyntaxException u){

        }

    }


    public void Put(){}

    public void Delete(){}


    /* Method name: generatePayload
     * Parameters:  values (ContentValues)
     * Description: Used to convert a String to the application/x-www-form-urlencoded MIME format
     * Return: String
     * Author: Sherwin Pitao
     * Date Created: 08/19/2015
     */
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

    /* Class name: AsyncPost
     * Super Class: AsyncTask
     * Parameters:  Url (String)
     * Return: String
     * Description: Used to communicate Mobile Device to WEB SERVER
     * Author: Sherwin Pitao
     * Date Created: 08/19/2015
     */
    private class AsyncPost extends AsyncTask<String,Object,String>{
        String s;
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

                    while ((s = streamReader.readLine()) != null) {
                        sb.append(s);
                    }

                }
                // Delimiter "¬",
                sb.append("¬"+conn.getResponseCode());

            }catch (MalformedURLException m){

            }catch (IOException i){

            }
            return sb.toString();
        }

        @Override
        protected void onPostExecute(String string) {
            //Log.d("Res",string);
            String[] result = string.split("¬");
            if(result[1].toString() != null){
                //  Log.d("LOGGER",string);
                if((Integer.parseInt(result[1].toString()) == 200)){
                    responseCollector.onSuccess(Integer.parseInt(result[1].toString()), result[0].toString());
                }else{
                    responseCollector.onFailure(Integer.parseInt(result[1].toString()),result[0].toString());
                }
            }else{
                responseCollector.onFailure(14,"No Result Found");
            }
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);
            responseCollector.onProgress(values);
        }
    }
    private class AsyncGet extends AsyncTask<String,Object,String>{

        String s;
        StringBuilder sb = new StringBuilder();
        IResponseCollector responseCollector;

        private AsyncGet(IResponseCollector responseCollector) {
            this.responseCollector = responseCollector;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {

            if(s!=null){
                String[] result = s.split(";;");
                //  Log.d("Result0",result[0].toString());
                //   Log.d("Result1",result[1].toString());
                if((Integer.parseInt(result[1].toString()) == 200)){
                    responseCollector.onSuccess(Integer.parseInt(result[1].toString()), result[0].toString());
                }else{
                    responseCollector.onFailure(Integer.parseInt(result[1].toString()),"May eeror boss");
                }
            }else{
                responseCollector.onFailure(14,"No Result Found");
            }
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);
            responseCollector.onProgress(values);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {

                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(false);
                urlConnection.setRequestMethod("GET");
                /*urlConnection.setRequestProperty("Authorization", api_key);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "*");*/
                if(getContentHeaders()!=null){
                    for(String val: getContentHeaders().keySet()) {
                        urlConnection.setRequestProperty(val, getContentHeaders().get(val).toString());
                    }
                }

                urlConnection.setUseCaches(false);
                urlConnection.setDoInput(true);
                urlConnection.connect();

                if(urlConnection.getResponseCode()==200){
                    InputStream is = urlConnection.getInputStream();
                    BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                    while ((s = streamReader.readLine()) != null) {
                        sb.append(s);
                    }

                }
                // Delimiter ";;",
                sb.append(";;"+urlConnection.getResponseCode());
            }
            catch (MalformedURLException mue){
                //TODO: Add code here
                Log.d("MalformedURLException", mue.getMessage());
            }catch (IOException ioe){
                //TODO: Add code here
                Log.d("IOException",ioe.getMessage());
            }
            return sb.toString();
        }
    }

}
