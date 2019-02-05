package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HandelsbankenAnalyticsConfiguration {

    private HandelsbankenClearingNumber shbClearingNo;

    public HandelsbankenClearingNumber getShbClearingNo() {
        return shbClearingNo;
    }
}
