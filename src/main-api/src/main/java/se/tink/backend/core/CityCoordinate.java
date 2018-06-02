package se.tink.backend.core;

import com.google.common.base.MoreObjects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "city_coordinates")
public class CityCoordinate {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String city;
    private String country;
    private Coordinate coordinate;

    public CityCoordinate() {

    }

    public CityCoordinate(String city, String country, Coordinate coordinate) {
        this.city = city;
        this.country = country;
        this.coordinate = coordinate;
    }

    public String getCity() {
        return city;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("city", city)
                .add("country", country)
                .add("lat", coordinate.getLatitude())
                .add("lng", coordinate.getLongitude())
                .toString();
    }
}
