package connection;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import cipher.Cipher;
import common.LogManager;
import dto.RET_OI_VMC_01;

/**
 * Created by njoy on 2015-04-24.
 */
public class WebDataRequest  {
    /**
     * HTTP Get
     * @access	public
     * @param	String
     * @return	String
     */
    public static String HTTPRequestGet(String getUri){
        String res = null;
        try{

            HttpClient 		client 		= new DefaultHttpClient();
            HttpGet 		get 		= new HttpGet(getUri);
            HttpResponse 	response 	= client.execute(get);
            HttpEntity 		resEntity 	= response.getEntity();

            if(resEntity != null){
                res = EntityUtils.toString(resEntity);
                //LogManager.DEBUG("[HttpConnectManager-HTTPRequestGet] result : " + res);
            }
        }catch(Exception e){
            //LogManager.ERROR("[HttpConnectManager-HTTPRequestGet] Exception : " + e.toString());
            return null;
        }
        finally{}

        return res;
    }

    /**
     * HTTP Post
     * @access	public
     * @param	String
     * @return	String
     */
    public static String HTTPRequestPost(String postUri) {
        String res = null;
        try{
            //LogManager.DEBUG("[HttpConnectManager-HTTPRequestPost] url : " + postUri);

            HttpClient 		client 			= new DefaultHttpClient();
            HttpPost 		post 			= new HttpPost(postUri);
            HttpResponse 	responsePost 	= client.execute(post);
            HttpEntity 		resEntity 		= responsePost.getEntity();

            if(resEntity != null){
                res = EntityUtils.toString(resEntity);
                //LogManager.DEBUG("[HttpConnectManager-HTTPRequestPost] result : " + res);
            }
        }catch(Exception e) {
            //LogManager.ERROR("[HttpConnectManager-HTTPRequestPost] Exception : " + e.toString());
        }
        finally{}

        return res;
    }

    /**
     * HTTP Post
     * @access	public
     * @param	String
     * @param	List<BasicNameValuePair>
     * @return	String
     */
    public static String HTTPRequestPost(String postUri, List<BasicNameValuePair> params) {
        String res = null;

        try{
            HttpClient 				client 	= new DefaultHttpClient();
            HttpPost 				post 	= new HttpPost(postUri);

            UrlEncodedFormEntity 	ent 	= new UrlEncodedFormEntity(params, HTTP.UTF_8);
            post.setEntity(ent); // post case

            HttpResponse 	responsePost 	= client.execute(post);
            HttpEntity 		resEntity 		= responsePost.getEntity();

            /*-- Header�� ����...
            Header[] resHeaders = responsePost.getAllHeaders();
            for(int i=0;i<resHeaders.length;i++){
                //LogManager.ERROR("HEADER : " + resHeaders[i].toString());
            }*/

            if(resEntity != null){
                res = EntityUtils.toString(resEntity);
                //LogManager.DEBUG("[HttpConnectManager-HTTPRequestPost params] result : " + res);
            }


        }catch(Exception e){
            //LogManager.ERROR("[HttpConnectManager-HTTPRequestPost params] Exception : " + e.toString());
            return null;
        }
        finally{

        }

        return res;
    }

    /**
     * HTTP Post JSON
     * @access	public
     * @param	String
     * @param	List<BasicNameValuePair>
     * @return	String
     */
    public static String HTTPRequestJsonPost(String postUri, JSONObject params) {
        String res = null;

        try{
            HttpClient 				client 	= new DefaultHttpClient();
            HttpPost 				post 	= new HttpPost(postUri);
            // START JSON POST AREA
            post.setHeader("Content-type", "application/json");
            post.setHeader("Accept", "application/json");
            post.setEntity(new StringEntity(params.toString(), "UTF-8"));

            HttpResponse 	responsePost 	= client.execute(post);
            HttpEntity 		resEntity 		= responsePost.getEntity();

            if(resEntity != null){
                res = EntityUtils.toString(resEntity);
                //LogManager.DEBUG("[HttpConnectManager-HTTPRequestPost params] result : " + res);
            }


        }catch(Exception e){
            //LogManager.ERROR("[HttpConnectManager-HTTPRequestPost params] Exception : " + e.toString());
            return null;
        }
        finally{

        }

        return res;
    }


}
