package dev.jmoore.lametricnotify;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.Base64;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Lametric extends ContextWrapper {
    private String address, api;

    public Lametric(Context base, String address, String api) {
        super(base);
        this.address = address;
        this.api = api;
    }

    public void sendNotification(int icon, final String text) throws JSONException {
        String url = "http://" + address + ":8080/api/v2/device/notifications";
        Toast.makeText(Lametric.super.getBaseContext(),text,Toast.LENGTH_LONG).show();

        RequestQueue queue = Volley.newRequestQueue(this);

        //JSONObject params = new JSONObject();
        //params.put("icon", 555);
        //params.put("text", text);

        String body = "{\n" +
                "   \"model\": {\n" +
                "        \"frames\": [\n" +
                "            {\n" +
                "               \"icon\":" + icon + ",\n" +
                "               \"text\":\"" + text + "\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";
        JSONObject params = new JSONObject(body);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, params, new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                //This code is executed if the server responds, whether or not the response contains data.
                //The String 'response' contains the server's response.
                Toast.makeText(Lametric.super.getBaseContext(),"Response: " + response,Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //This code is executed if there is an error.
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    //This indicates that the reuest has either time out or there is no connection
                    Toast.makeText(Lametric.super.getBaseContext(),"timeout/conn: "+error,Toast.LENGTH_LONG).show();
                } else if (error instanceof AuthFailureError) {
                    //Error indicating that there was an Authentication Failure while performing the request
                    Toast.makeText(Lametric.super.getBaseContext(),"auth: "+error,Toast.LENGTH_LONG).show();
                } else if (error instanceof ServerError) {
                    //Indicates that the server responded with a error response
                    Toast.makeText(Lametric.super.getBaseContext(),"server: "+error.getCause(),Toast.LENGTH_LONG).show();
                } else if (error instanceof NetworkError) {
                    //Indicates that there was network error while performing the request
                    Toast.makeText(Lametric.super.getBaseContext(),"network: "+error,Toast.LENGTH_LONG).show();
                } else if (error instanceof ParseError) {
                    // Indicates that the server response could not be parsed
                    Toast.makeText(Lametric.super.getBaseContext(),"parse: "+error,Toast.LENGTH_LONG).show();
                }
            }
        }) {
            @Override
            public Map getHeaders() throws AuthFailureError {
                Map headers = new HashMap();
                String creds = "dev:" + api;
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", auth);
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        queue.add(request);
        //queue.start();
    }
}
