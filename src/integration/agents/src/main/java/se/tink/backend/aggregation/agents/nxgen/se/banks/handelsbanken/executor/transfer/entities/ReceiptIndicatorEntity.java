package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ReceiptIndicatorEntity {
    private String indicatorType;
    private Object shareAction;
    private String text;
    private String type;

    public String getIndicatorType() {
        return indicatorType;
    }
}
