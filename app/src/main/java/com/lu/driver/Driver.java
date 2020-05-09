package com.lu.driver;

import java.io.Serializable;

public class Driver implements Serializable {
    private int driver_id;
    private String driver_name;
    private String driver_phone;
    private int driver_status;
    private double driver_longitude;
    private double driver_latitude;
    private String driver_email;
    private String driver_password;
    private String driver_bank_name;
    private String driver_bank_account;
    private String driver_bank_code;

    public Driver(int driver_id) {
        this.driver_id = driver_id;
    }

    public Driver(int driver_id, String driver_name, String driver_phone, int driver_status) {
        this.driver_id = driver_id;
        this.driver_name = driver_name;
        this.driver_phone = driver_phone;
        this.driver_status = driver_status;
    }

    public Driver(int driver_id, double driver_longitude, double driver_latitude) {
        this.driver_id = driver_id;
        this.driver_longitude = driver_longitude;
        this.driver_latitude = driver_latitude;
    }

    public void setLocation(double driver_latitude, double driver_longitude) {
        this.driver_latitude = driver_latitude;
        this.driver_longitude = driver_longitude;
    }

    public Driver(String driver_name, String driver_email, String driver_password, String driver_phone, String driver_bank_name, String driver_bank_account, String driver_bank_code) {
        this.driver_name = driver_name;
        this.driver_email = driver_email;
        this.driver_password = driver_password;
        this.driver_phone = driver_phone;
        this.driver_bank_name = driver_bank_name;
        this.driver_bank_account = driver_bank_account;
        this.driver_bank_code = driver_bank_code;
    }

    public void setStatus(int driver_status) {
        this.driver_status = driver_status;
    }

    public int getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(int driver_id) {
        this.driver_id = driver_id;
    }

    public String getDriver_name() {
        return driver_name;
    }

    public void setDriver_name(String driver_name) {
        this.driver_name = driver_name;
    }

    public String getDriver_phone() {
        return driver_phone;
    }

    public void setDriver_phone(String driver_phone) {
        this.driver_phone = driver_phone;
    }

    public int getDriver_status() {
        return driver_status;
    }

    public void setDriver_status(int driver_status) {
        this.driver_status = driver_status;
    }

    public double getDriver_longitude() {
        return driver_longitude;
    }

    public void setDriver_longitude(double driver_longitude) {
        this.driver_longitude = driver_longitude;
    }

    public double getDriver_latitude() {
        return driver_latitude;
    }

    public void setDriver_latitude(double driver_latitude) {
        this.driver_latitude = driver_latitude;
    }


    public String getDriver_email() {
        return driver_email;
    }

    public void setDriver_email(String driver_email) {
        this.driver_email = driver_email;
    }

    public String getDriver_password() {
        return driver_password;
    }

    public void setDriver_password(String driver_password) {
        this.driver_password = driver_password;
    }


    public String getDriver_bank_name() {
        return driver_bank_name;
    }

    public void setDriver_bank_name(String driver_bank_name) {
        this.driver_bank_name = driver_bank_name;
    }

    public String getDriver_bank_account() {
        return driver_bank_account;
    }

    public void setDriver_bank_account(String driver_bank_account) {
        this.driver_bank_account = driver_bank_account;
    }

    public String getDriver_bank_code() {
        return driver_bank_code;
    }

    public void setDriver_bank_code(String driver_bank_code) {
        this.driver_bank_code = driver_bank_code;
    }
}
