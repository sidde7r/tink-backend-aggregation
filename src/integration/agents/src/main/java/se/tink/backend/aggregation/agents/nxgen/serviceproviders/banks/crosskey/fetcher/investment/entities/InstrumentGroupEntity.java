package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.investment.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InstrumentGroupEntity {

    private int typeOfInstruments;
    private List<InstrumentEntity> instruments;
    private double totalMarketValue;
    private double totalProfit;

    public int getTypeOfInstruments() {
        return typeOfInstruments;
    }

    public List<InstrumentEntity> getInstruments() {
        return instruments;
    }

    public double getTotalMarketValue() {
        return totalMarketValue;
    }

    public double getTotalProfit() {
        return totalProfit;
    }
}
