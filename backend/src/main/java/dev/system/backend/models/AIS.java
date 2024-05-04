package dev.system.backend.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;


@Entity
public class AIS {

    @Id
    private String hash;
    private float speed;
    private float lon;
    private float lat;
    private int course;
    private int heading;
    private String timeStamp;
    private String departurePortName;

    public AIS(){}

    public AIS(String hash, float speed, float lon,
               float lat, int course, int heading, String timeStamp,
               String departurePortName){
        this.hash = hash;
        this.speed = speed;
        this.lon = lon;
        this.lat = lat;
        this.course = course;
        this.heading = heading;
        this.timeStamp = timeStamp;
        this.departurePortName = departurePortName;
    }

    public String getHash() {
        return hash;
    }

    public float getSpeed() {
        return speed;
    }

    public float getLon() {
        return lon;
    }

    public float getLat() {
        return lat;
    }

    public int getCourse() {
        return course;
    }

    public int getHeading() {
        return heading;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getDeparturePortName() {
        return departurePortName;
    }
}
