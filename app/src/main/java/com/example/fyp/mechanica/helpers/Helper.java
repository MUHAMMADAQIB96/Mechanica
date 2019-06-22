package com.example.fyp.mechanica.helpers;

public class Helper {

    public static int getDurationOfDistance(double distance) {
        double meterPerSec = 14;
        double time = (distance*1000) / meterPerSec;
        long t = (long) time;

        return (int) (t / 60);
    }

}
