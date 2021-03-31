package com.bfong.stockwatchbf.api;

import android.net.Uri;
import android.util.Log;

import com.bfong.stockwatchbf.MainActivity;
import com.bfong.stockwatchbf.Stock;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class StockDownloader implements Runnable {

    private static final String TAG = "StockDownloader";
    private static final String url1 = "https://cloud.iexapis.com/stable/stock/";
    private static final String url2 = "/quote?token=pk_2ac6214d645747b2961df5207ccffb82";
    private MainActivity mainActivity;
    private String query;

    public StockDownloader(MainActivity mainActivity, String target) {
        this.mainActivity = mainActivity;
        this.query = url1 + target + url2;
    }

    @Override
    public void run() {
        Uri uriBuilder = Uri.parse(query);
        String urlString = uriBuilder.toString();
        Log.d(TAG, "run: " + uriBuilder.toString());

        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlString);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "run: HTTP ResponseCode not OK: " + conn.getResponseCode());
                return;
            }

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

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
            JSONObject jStock = new JSONObject(s);

            String symbol = jStock.getString("symbol");
            String name = jStock.getString("companyName");
            double price;
            if (jStock.getString("latestPrice").equals("null")) {
                price = 0.0;
            } else {
                price = jStock.getDouble("latestPrice");
            }
            double change;
            if (jStock.getString("change").equals("null")) {
                change = 0.0;
            } else {
                change = jStock.getDouble("change");
            }
            double changePct;
            if (jStock.getString("changePercent").equals("null")) {
                changePct = 0.0;
            } else {
                changePct = jStock.getDouble("changePercent");
            }

            final Stock stock = new Stock(symbol, name, price, change, changePct);

            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.addStock(stock);
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "process: error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
