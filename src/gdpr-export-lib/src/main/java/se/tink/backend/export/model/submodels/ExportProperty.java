package se.tink.backend.export.model.submodels;

import java.util.Date;
import se.tink.backend.export.helper.DefaultSetter;

public class ExportProperty implements DefaultSetter {

    private final String address;
    private final String city;
    private final String community;
    private final String postalCode;
    private final Double latitude;
    private final Double longitude;
    private final String type;
    private final String status;
    private final String addressRegistered;
    private final String created;
    private final Integer numberOfRooms;
    private final Integer numberOfSquareMeters;
    private final Integer mostRecentValuation;

    public ExportProperty(String address, String city, String community, String postalCode, Double latitude,
            Double longitude, String type, String status, String addressRegistered, Date created, Integer numberOfRooms,
            Integer numberOfSquareMeters, Integer mostRecentValuation) {
        this.address = address;
        this.city = city;
        this.community = community;
        this.postalCode = postalCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
        this.status = status;
        this.addressRegistered = addressRegistered;
        this.created = notNull(created);
        this.numberOfRooms = numberOfRooms;
        this.numberOfSquareMeters = numberOfSquareMeters;
        this.mostRecentValuation = mostRecentValuation;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getCommunity() {
        return community;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public String getAddressRegistered() {
        return addressRegistered;
    }

    public String getCreated() {
        return created;
    }

    public Integer getNumberOfRooms() {
        return numberOfRooms;
    }

    public Integer getNumberOfSquareMeters() {
        return numberOfSquareMeters;
    }

    public Integer getMostRecentValuation() {
        return mostRecentValuation;
    }
}
