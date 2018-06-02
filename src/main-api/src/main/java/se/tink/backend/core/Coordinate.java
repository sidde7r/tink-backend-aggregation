package se.tink.backend.core;

import io.protostuff.Tag;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class Coordinate implements Serializable, Cloneable {
    private static final long serialVersionUID = 5529030004191030435L;

    @Tag(1)
    private Double latitude;
    @Tag(2)
    private Double longitude;

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public static Coordinate create(Double lat, Double lng) {
        Coordinate c = new Coordinate();
        c.setLatitude(lat);
        c.setLongitude(lng);
        return c;
    }

    @Override
    public Coordinate clone() {
        try {
            return (Coordinate) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
