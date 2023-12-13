package com.example.firstapp;

import com.google.firebase.database.Exclude;

public class GeofenceData {

    private String id;

    private String id_user;
    private double latitude;

    private double longitude;
    private int radius;

    public GeofenceData(String id, String id_user, double latitude, double longitude, int radius) {
        this.id = id;
        this.id_user = id_user;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    // Default constructor required for Firebase
    public GeofenceData() {
    }
    public String getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getRadius() {
        return radius;
    }

    public String getId_user() {
        return id_user;
    }
}
