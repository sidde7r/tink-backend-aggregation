package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class HandelsbankenSEProperty {
    private String label;
    private String value;

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    public boolean isPayment() {
        return "Inbetalningar".equalsIgnoreCase(label);
    }

    public double asDouble() {
        return StringUtils.parseAmount(value);
    }
}
