package se.tink.backend.insights.core.valueobjects;

import java.util.Objects;

public class Address {

    private String address;

    private Address(String address) {
        this.address = address;
    }

    public String value() {
        return address;
    }

    public static Address of(String address) {
        return new Address(address);
    }

    @Override
    public String toString() {
        return address;
    }

    public boolean equals(Address that) {
        return Objects.equals(address, that.value());
    }
}
