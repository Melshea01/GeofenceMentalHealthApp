package com.example.firstapp;

import com.google.firebase.database.Exclude;

public class GeofenceData {
    private String id;
    private double latitude;
    private double longitude;
    private int radius;

    public GeofenceData(String id, double latitude, double longitude, int radius) {
        this.id = id;
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
}
