package com.bfong.stockwatchbf;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class StockViewHolder extends RecyclerView.ViewHolder {

    public TextView symbol;
    TextView price;
    TextView change;
    TextView name;

    StockViewHolder(@NonNull View view) {
        super(view);
        symbol = view.findViewById(R.id.stockSymbol);
        price = view.findViewById(R.id.stockPrice);
        change = view.findViewById(R.id.stockChange);
        name = view.findViewById(R.id.companyName);
    }

    public void setColor(double chng) {
        if (chng < 0) {
            symbol.setTextColor(Color.parseColor("#FF0000"));
            price.setTextColor(Color.parseColor("#FF0000"));
            change.setTextColor(Color.parseColor("#FF0000"));
            name.setTextColor(Color.parseColor("#FF0000"));
        } else {
            symbol.setTextColor(Color.parseColor("#08FF00"));
            price.setTextColor(Color.parseColor("#08FF00"));
            change.setTextColor(Color.parseColor("#08FF00"));
            name.setTextColor(Color.parseColor("#08FF00"));
        }
    }
}
