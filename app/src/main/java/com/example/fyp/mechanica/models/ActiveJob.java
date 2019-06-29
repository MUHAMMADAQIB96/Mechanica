package com.example.fyp.mechanica.models;

import java.io.Serializable;

public class ActiveJob implements Serializable {
    public double cusLat;
    public double cusLon;
    public String customerID;
    public int jobStatus;
    public double mechLat;
    public double mechLon;
    public String mechanicID;
    public long startedAt;
}
