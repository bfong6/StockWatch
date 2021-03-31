package com.bfong.stockwatchbf;

import android.util.JsonWriter;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

public class Stock implements Serializable, Comparable<Stock> {

    private String symbol;
    private String name;
    private double price;
    private double priceChange;
    private double changePct;

    public Stock(String symbol, String name, double price, double priceChange, double changePct) {
        this.symbol = symbol;
        this.name = name;
        this.price = price;
        this.priceChange = priceChange;
        this.changePct = changePct;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public double getPriceChange() {
        return priceChange;
    }

    public double getChangePct() {
        return changePct;
    }

    public void save(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("symbol").value(this.symbol);
        writer.name("name").value(this.name);
        writer.endObject();
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, name);
    }

    @Override
    public int compareTo(Stock stock) {
        return symbol.compareTo(stock.getSymbol());
    }

    public boolean equals(Stock stock) {
        if (this.name.equals(stock.getName()) && this.symbol.equals(stock.getSymbol())) {
            return true;
        }
        return false;
    }
}
