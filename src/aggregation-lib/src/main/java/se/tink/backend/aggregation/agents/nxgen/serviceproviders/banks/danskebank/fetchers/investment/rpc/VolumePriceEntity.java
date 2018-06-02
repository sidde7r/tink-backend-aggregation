package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class VolumePriceEntity {
    private double volume;
    private double price;

    public double getVolume() {
        return volume;
    }

    public double getPrice() {
        return price;
    }
}
