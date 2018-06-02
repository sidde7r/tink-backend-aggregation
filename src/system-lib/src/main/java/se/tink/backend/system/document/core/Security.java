package se.tink.backend.system.document.core;

public class Security {
    private final String address;
    private final String town;

    public Security(String address, String town) {
        this.address = address;
        this.town = town;
    }

    public String getAddress() {
        return address;
    }

    public String getTown() {
        return town;
    }
}
