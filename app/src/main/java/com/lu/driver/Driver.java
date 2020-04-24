package com.lu.driver;

import java.io.Serializable;

public class Driver implements Serializable {
    private int driver_id;
    private int driver_status;
    private double driver_longitude;
    private double driver_latitude;

    public Driver(int driver_id) {
        this.driver_id = driver_id;
    }

    public Driver(int driver_id, double driver_longitude, double driver_latitude) {
        this.driver_id = driver_id;
        this.driver_longitude = driver_longitude;
        this.driver_latitude = driver_latitude;
    }

    public void setLocation(double driver_latitude, double driver_longitude){
        this.driver_latitude = driver_latitude;
        this.driver_longitude = driver_longitude;
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
}
