package com.example.fyp.mechanica.models;


import java.io.Serializable;
import java.util.List;

public class User implements Serializable {

    public String id;
    public String name;
    public String userRole;
    public String email;
    public String phoneNumber;

//    public List<Vehicle> vehicles;

    public Vehicle vehicle;

    public User() {

    }
}
