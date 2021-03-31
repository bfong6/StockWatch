package com.bfong.stockwatchbf.api;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class NameDownloader implements Runnable {

    private static final String TAG = "NameDownloader";
    private static final String STOCK_URL = "https://api.iextrading.com/1.0/ref-data/symbols";
    public static HashMap<String, String> stockMap = new HashMap<>();

    @Override
    public void run() {
        Uri dataUri = Uri.parse(STOCK_URL);
        String urlString = dataUri.toString();
        Log.d(TAG, "run: " + dataUri.toString());

        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlString);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.connect();

            if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "run: HTTP ResponseCode not ok: " + con.getResponseCode());
                return;
            }

            InputStream in = con.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(in)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

            Log.d(TAG, "run: " + sb.toString());
        } catch (Exception e) {
            Log.e(TAG, "run: ", e);
            return;
        }

        process(sb.toString());
        Log.d(TAG, "run: ");
    }

    private void process(String s) {
        try {
            JSONArray jArray = new JSONArray(s);

            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jStock = (JSONObject) jArray.get(i);

                String symbol = jStock.getString("symbol");
                String name = jStock.getString("name");

                stockMap.put(symbol, name);
            }
            Log.d(TAG, "process: ");
        } catch (Exception e) {
            Log.d(TAG, "process: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static ArrayList<String> findMatch(String s) {
        String toMatch = s.toLowerCase().trim();
        HashSet<String> matchSet = new HashSet<>();

        for (String sym : stockMap.keySet()) {
            if (sym.toLowerCase().trim().contains(toMatch)) {
                matchSet.add(sym + " - " + stockMap.get(sym));
            }
            String name = stockMap.get(sym);
            if (name != null && name.toLowerCase().trim().contains(toMatch)) {
                matchSet.add(sym + " - " + name);
            }
        }

        ArrayList<String> result = new ArrayList<>(matchSet);
        Collections.sort(result);

        return result;
    }
}
