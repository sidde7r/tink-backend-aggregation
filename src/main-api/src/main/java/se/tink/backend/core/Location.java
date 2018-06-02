package se.tink.backend.core;

import io.protostuff.Exclude;
import io.protostuff.Tag;

import javax.persistence.Embeddable;
import javax.persistence.Transient;

@Embeddable
public class Location implements Cloneable {
    // TODO: Remove transient when we can add columns to the transaction table.
    @Transient
    @Tag(2)
    private Double accuracy;
    @Exclude
    private String address;
    @Tag(3)
    private String city;
    // TODO: Remove transient when we can add columns to the transaction table.
    @Transient
    @Exclude
    private String country;
    @Tag(1)
    private Coordinate coordinate;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    @Override
    public Location clone() {
        try {
            Location myClone = (Location) super.clone();
            if (coordinate != null) {
                myClone.setCoordinate(coordinate.clone());
            }
            return myClone;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
