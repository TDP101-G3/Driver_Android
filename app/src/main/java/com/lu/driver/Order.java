package com.lu.driver;

import java.io.Serializable;

public class Order implements Serializable {
    private int order_id;
    private String order_start;
    private String order_end;
    private double customer_score;
    private double driver_score;

    public Order(int order_id, double customer_score) {
        this.order_id = order_id;
        this.customer_score = customer_score;
    }

    public Order(String order_start, String order_end) {
        this.order_start = order_start;
        this.order_end = order_end;
    }

    public Order(int order_id, String order_start, String order_end) {
        this.order_id = order_id;
        this.order_start = order_start;
        this.order_end = order_end;
    }

    public int getOrder_id() {
        return order_id;
    }

    public void setOrder_id(int order_id) {
        this.order_id = order_id;
    }

    public String getOrder_start() {
        return order_start;
    }

    public void setOrder_start(String order_start) {
        this.order_start = order_start;
    }

    public String getOrder_end() {
        return order_end;
    }

    public void setOrder_end(String order_end) {
        this.order_end = order_end;
    }

    public double getCustomer_score() {
        return customer_score;
    }

    public void setCustomer_score(double customer_score) {
        this.customer_score = customer_score;
    }

    public double getDriver_score() {
        return driver_score;
    }

    public void setDriver_score(double driver_score) {
        this.driver_score = driver_score;
    }
}
