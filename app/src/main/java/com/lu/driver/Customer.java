package com.lu.driver;

import java.io.Serializable;

public class Customer implements Serializable {
    private int customer_id;
    private String customer_name;
    private String customer_phone;
    private String customer_number_plate;
    private String customer_car_model;
    private String customer_car_color;

    public Customer(int customer_id, String customer_name, String customer_phone, String customer_number_plate,
                    String customer_car_model, String customer_car_color) {
        this.customer_id = customer_id;
        this.customer_name = customer_name;
        this.customer_phone = customer_phone;
        this.customer_number_plate = customer_number_plate;
        this.customer_car_model = customer_car_model;
        this.customer_car_color = customer_car_color;
    }

    public int getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(int customer_id) {
        this.customer_id = customer_id;
    }

    public String getCustomer_name() {
        return customer_name;
    }

    public void setCustomer_name(String customer_name) {
        this.customer_name = customer_name;
    }

    public String getCustomer_phone() {
        return customer_phone;
    }

    public void setCustomer_phone(String customer_phone) {
        this.customer_phone = customer_phone;
    }

    public String getCustomer_number_plate() {
        return customer_number_plate;
    }

    public void setCustomer_number_plate(String customer_number_plate) {
        this.customer_number_plate = customer_number_plate;
    }

    public String getCustomer_car_model() {
        return customer_car_model;
    }

    public void setCustomer_car_model(String customer_car_model) {
        this.customer_car_model = customer_car_model;
    }

    public String getCustomer_car_color() {
        return customer_car_color;
    }

    public void setCustomer_car_color(String customer_car_color) {
        this.customer_car_color = customer_car_color;
    }
}
