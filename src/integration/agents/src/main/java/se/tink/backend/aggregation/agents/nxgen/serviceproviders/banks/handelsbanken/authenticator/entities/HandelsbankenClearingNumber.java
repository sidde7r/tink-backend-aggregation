package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class HandelsbankenClearingNumber {

    private String number;

    @JsonCreator
    public static HandelsbankenClearingNumber create(String number) {
        HandelsbankenClearingNumber clearingNumber = new HandelsbankenClearingNumber();
        clearingNumber.number = number;
        return clearingNumber;
    }

    @JsonValue
    @Override
    public String toString() {
        return number;
    }
}
