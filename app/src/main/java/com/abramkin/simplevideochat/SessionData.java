package com.abramkin.simplevideochat;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class SessionData {

    private static final String LOG_TAG = SessionData.class.getSimpleName();

    private final Context context;
    private Listener listener;

    public SessionData(Context context, Listener listener) {

        this.context = context;
        this.listener = listener;
    }

    public void getSessionData(String url) {

        RequestQueue reqQueue = Volley.newRequestQueue(context);
        reqQueue.add(new JsonObjectRequest(Request.Method.GET, url,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String apiKey = response.getString("apiKey");
                    String sessionId = response.getString("sessionId");
                    String token = response.getString("token");

                    listener.initializeSession(apiKey, sessionId, token);

                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Web Service error: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, "Web Service error: " + error.getMessage());
            }
        }));
    }

    public static interface Listener {

        void initializeSession(String apiKey, String sessionId, String token);

    }
}
