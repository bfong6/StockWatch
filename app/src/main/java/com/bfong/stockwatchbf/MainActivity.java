package com.bfong.stockwatchbf;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bfong.stockwatchbf.api.NameDownloader;
import com.bfong.stockwatchbf.api.StockDownloader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "MainActivity";
    private final List<Stock> stockList = new ArrayList<>();
    private final List<Stock> tempList = new ArrayList<>();
    private RecyclerView recyclerView;
    private StockAdapter sAdapter;
    private SwipeRefreshLayout swiper;
    private String target;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: creating instance");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler);
        swiper = findViewById(R.id.swiper);
        swiper.setOnRefreshListener(this);

        sAdapter = new StockAdapter(stockList, this);
        recyclerView.setAdapter(sAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        getSupportActionBar().setTitle("Stock Watch");

        NameDownloader sDownloader = new NameDownloader();
        new Thread(sDownloader).start();

        Log.d(TAG, "onCreate: loading file");
        loadFile();
        if (!checkNetworkConnection()) {
            noNetworkError();
            for (Stock s : tempList) {
                stockList.add(s);
            }
            Collections.sort(stockList);
            sAdapter.notifyDataSetChanged();
        } else {
            stockList.clear();
            for (Stock s : tempList) {
                StockDownloader stockDownloader = new StockDownloader(this, s.getSymbol());
                new Thread(stockDownloader).start();
            }
        }
    }

    public void addStock(Stock stock) {
        if (stock == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage("No data for selection");
            builder.setTitle("Symbol Not Found: " + stock.getSymbol());

            AlertDialog dialog = builder.create();
            dialog.show();
            return;
        }

        for (Stock s : stockList) {
            if (s.equals(stock)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("Duplicate Stock");
                builder.setMessage("Stock symbol " + stock.getSymbol() + " is already displayed");
                builder.setIcon(R.drawable.baseline_warning_black_48);
                builder.setPositiveButton("DISMISS", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
                return;
            }
        }
        stockList.add(stock);
        Collections.sort(stockList);
        sAdapter.notifyDataSetChanged();
    }

    private void loadFile() {
        try {
            InputStream i = getApplicationContext().openFileInput("Stocks.json");

            BufferedReader r = new BufferedReader(new InputStreamReader(i, StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line);
            }

            JSONArray jsonArray = new JSONArray(sb.toString());
            for (int x = 0; x < jsonArray.length(); x++) {
                JSONObject jsonObject = jsonArray.getJSONObject(x);
                String symbol = jsonObject.getString("symbol");
                String name = jsonObject.getString("name");
                double price = 0;
                double priceChange = 0;
                double pctChange = 0;
                Stock s = new Stock(symbol, name, price, priceChange, pctChange);
                Log.d(TAG, "loadFile: adding item " + symbol);
                tempList.add(s);
            }
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "No saved stocks found", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Collections.sort(tempList);
    }

    @Override
    protected void onPause() {
        saveStocks();
        super.onPause();
    }

    private void saveStocks() {
        Log.d(TAG, "saveStocks: saving stocks");
        try {
            FileOutputStream f = getApplicationContext().openFileOutput("Stocks.json", Context.MODE_PRIVATE);

            JsonWriter w = new JsonWriter(new OutputStreamWriter(f, "UTF-8"));
            w.setIndent("  ");
            w.beginArray();
            for (int i = 0; i < stockList.size(); i++) {
                stockList.get(i).save(w);
            }
            w.endArray();
            w.close();

        } catch (Exception e) {
            Log.d(TAG, "saveNotes: Error");
            e.getStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addStockBtn:
                if (!checkNetworkConnection()) {
                    noNetworkError();
                    return true;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                final EditText et = new EditText(this);
                et.setInputType(InputType.TYPE_CLASS_TEXT);
                et.setGravity(Gravity.CENTER_HORIZONTAL);
                et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                builder.setView(et);
                builder.setTitle("Stock Selection");
                builder.setMessage("Please enter a stock symbol");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        target = et.getText().toString().trim();

                        final ArrayList<String> results = NameDownloader.findMatch(target);

                        if (results.size() == 0) {
                            noDataError(target);
                        } else if (results.size() == 1) {
                            Log.d(TAG, "onClick: searching for " + results.get(0));
                            selectStock(results.get(0));
                        } else {
                            String[] array = results.toArray(new String[0]);

                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("Make a selection");
                            builder.setItems(array, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int pos) {
                                    String symbol = results.get(pos);
                                    selectStock(symbol);
                                }
                            });
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            AlertDialog dialog2 = builder.create();
                            dialog2.show();
                        }
                        sAdapter.notifyDataSetChanged();
                        saveStocks();
                    }
                });
                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void selectStock(String s) {
        String[] data = s.split("-");
        StockDownloader stockDownloader = new StockDownloader(this, data[0].trim());
        new Thread(stockDownloader).start();
    }

    private void noDataError(String s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("No data for specified symbol/name");
        builder.setTitle("Symbol not found: " + s);
        builder.setPositiveButton("DISMISS", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        int pos = recyclerView.getChildLayoutPosition(v);
        String url = "http://www.marketwatch.com/investing/stock/" + stockList.get(pos).getSymbol();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    @Override
    public boolean onLongClick(View v) {
        int pos = recyclerView.getChildLayoutPosition(v);
        final Stock del = stockList.get(pos);

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setIcon(R.drawable.baseline_delete_forever_black_48);
        b.setTitle("Delete Stock");
        b.setMessage("Delete stock symbol \"" + stockList.get(pos).getSymbol() + "\"?");
        b.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                stockList.remove(del);
                saveStocks();
                sAdapter.notifyDataSetChanged();
            }
        });
        b.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = b.create();
        dialog.show();

        return true;
    }

    @Override
    public void onRefresh() {
        if (!checkNetworkConnection()) {
            noNetworkError();
        } else {
            tempList.clear();
            for (Stock s : stockList) {
                tempList.add(s);
            }
            Collections.sort(tempList);
            stockList.clear();
            for (Stock s : tempList) {
                StockDownloader stockDownloader = new StockDownloader(this, s.getSymbol());
                new Thread(stockDownloader).start();
            }
        }
        swiper.setRefreshing(false);
    }

    public void noNetworkError() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("No Network Connection");
        b.setMessage("No network connection detected!");
        b.setPositiveButton("DISMISS", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        AlertDialog d = b.create();
        d.show();
    }

    private boolean checkNetworkConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}