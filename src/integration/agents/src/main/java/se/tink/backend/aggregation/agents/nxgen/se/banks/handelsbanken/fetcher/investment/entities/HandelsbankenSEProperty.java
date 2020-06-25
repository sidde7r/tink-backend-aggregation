package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class HandelsbankenSEProperty {
    private String label;
    private String value;
    private String type;

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public boolean isIdentifer() {
        return "Identifier".equalsIgnoreCase(type);
    }

    public boolean isPayment() {
        return "Inbetalningar".equalsIgnoreCase(label);
    }

    public boolean isAmount() {
        return "Värde".equalsIgnoreCase(label);
    }

    public double asDouble() {
        return StringUtils.parseAmount(value);
    }
}
