package se.tink.libraries.identity.model;

public class Address {
    private String name;
    private String postalCode;
    private String city;
    private String community;

    public Address(String name, String postalCode, String city, String community) {
        this.name = name;
        this.postalCode = postalCode;
        this.city = city;
        this.community = community;
    }

    public String getName() {
        return name;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCity() {
        return city;
    }

    public String getCommunity() {
        return community;
    }

    public static Address of(String name, String postalCode, String city, String community) {
        return new Address(name, postalCode, city, community);
    }
}
