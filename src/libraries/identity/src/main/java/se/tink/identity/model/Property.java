package se.tink.libraries.identity.model;

import java.util.Date;

public class Property {
    private String name;
    private String municipality;

    // number can be null
    private String number;
    // date can be null
    private Date acquisitionDate;

    public Property(String name, String municipality, String number, Date acquisitionDate) {
        this.name = name;
        this.municipality = municipality;
        this.number = number;
        this.acquisitionDate = acquisitionDate;
    }

    public String getName() {
        return name;
    }

    public String getMunicipality() {
        return municipality;
    }

    public String getNumber() {
        return number;
    }

    public Date getAcquisitionDate() {
        return acquisitionDate;
    }

    public static Property of(String name, String municipality, String number, Date acquisitionDate) {
        return new Property(name, municipality, number, acquisitionDate);
    }
}
