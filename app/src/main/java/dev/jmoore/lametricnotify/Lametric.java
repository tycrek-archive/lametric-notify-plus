package dev.jmoore.lametricnotify;

import android.content.Context;
import android.content.ContextWrapper;
import android.text.TextUtils;
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
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Lametric extends ContextWrapper {
    private final String address;
    private final String api;

    public Lametric(Context base, String address, String api) {
        super(base);
        this.address = address;
        this.api = api;
    }

    public void sendNotification(String icon, String[] text) throws JSONException {
        String[] frames = new String[text.length];
        for (int i = 0; i < text.length; i++)
            frames[i] = "{ \"icon\": " + icon + ", \"text\": \"" + text[i] + "\" }";

        sendRequest("{ \"model\": { \"frames\": [ " + TextUtils.join(",", frames) + " ] } }");
    }

    private void sendRequest(String body) throws JSONException {
        String url = "http://" + address + ":8080/api/v2/device/notifications";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(body), new Response.Listener() {
            @Override
            public void onResponse(Object response) {}
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Set to false to toast errors for debugging
                //noinspection ConstantConditions
                if (true) return;
                if (error instanceof TimeoutError || error instanceof NoConnectionError) Toast.makeText(Lametric.super.getBaseContext(),"Timeout/Connection error: " + error, Toast.LENGTH_LONG).show();
                else if (error instanceof AuthFailureError) Toast.makeText(Lametric.super.getBaseContext(),"Authentication error: " + error, Toast.LENGTH_LONG).show();
                else if (error instanceof ServerError) Toast.makeText(Lametric.super.getBaseContext(),"Server error: " + error, Toast.LENGTH_LONG).show();
                else if (error instanceof NetworkError) Toast.makeText(Lametric.super.getBaseContext(),"Network error: " + error, Toast.LENGTH_LONG).show();
                else if (error instanceof ParseError) Toast.makeText(Lametric.super.getBaseContext(),"Parse error: " + error, Toast.LENGTH_LONG).show();
                else Toast.makeText(Lametric.super.getBaseContext(),"Unknown error: " + error, Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Basic " + Base64.encodeToString(("dev:" + api).getBytes(), Base64.NO_WRAP));
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}
