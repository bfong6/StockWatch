package com.bfong.stockwatchbf;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bfong.stockwatchbf.R.color;

import java.util.List;

public class StockAdapter extends RecyclerView.Adapter<StockViewHolder> {

    private static final String TAG = "StockAdapter";
    private List<Stock> stockList;
    private MainActivity mainAct;

    public StockAdapter(List<Stock> stockList, MainActivity mainAct) {
        this.stockList = stockList;
        this.mainAct = mainAct;
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_list_row, parent, false);

        itemView.setOnLongClickListener(mainAct);
        itemView.setOnClickListener(mainAct);
        return new StockViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {

        Stock stock = stockList.get(position);

        holder.symbol.setText(stock.getSymbol());
        holder.price.setText(String.valueOf(stock.getPrice()));
        double priceChange = stock.getPriceChange();
        double pctChange = stock.getChangePct();
        if (priceChange < 0) {
            holder.change.setText(String.format("▼ %.2f (%.2f%s)", priceChange, pctChange, "%"));
        } else {
            holder.change.setText(String.format("▲ %.2f (%.2f%s)", priceChange, pctChange, "%"));
        }
        holder.name.setText(stock.getName());
        holder.setColor(priceChange);
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }
}
