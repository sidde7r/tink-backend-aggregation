package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InstrumentDetailLine {
    private String header;
    private String value;

    public boolean isIn() {
        return "isin".equalsIgnoreCase(header);
    }

    public boolean isLista() {
        return "lista".equalsIgnoreCase(header);
    }

    public boolean isNamn() {
        return "namn".equalsIgnoreCase(header);
    }

    public String getValue() {
        return value;
    }
}
