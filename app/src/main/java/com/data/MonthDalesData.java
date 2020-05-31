package com.data;

public class MonthDalesData {
    String month;
    int sale;

    public MonthDalesData(String month, int sale) {
        this.month = month;
        this.sale = sale;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public int getSale() {
        return sale;
    }

    public void setSale(int sale) {
        this.sale = sale;
    }
}
