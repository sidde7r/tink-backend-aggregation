package se.tink.backend.core;

import java.util.List;

public class Place {
    protected String address;
    protected String postalCode;
    protected String icon;
    protected Location location;
    protected String name;
    protected String phoneNumber;
    protected String placeId;
    protected String photoReference;
    protected String photoAttributions;
    protected String website;
    protected List<String> types;

    public String getAddress() {
        return address;
    }

    public String getIcon() {
        return icon;
    }

    public Location getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPlaceId() {
        return placeId;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getPhotoReference() {
        return photoReference;
    }

    public void setPhotoReference(String photoReference) {
        this.photoReference = photoReference;
    }

    public String getPhotoAttributions() {
        return photoAttributions;
    }

    public void setPhotoAttributions(String photoAttributions) {
        this.photoAttributions = photoAttributions;
    }

    public boolean isEstablishment(){
        return types != null && types.contains("establishment");
    }
}
