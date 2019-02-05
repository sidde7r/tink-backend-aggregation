package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.investment.entities;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.agents.models.Instrument;

@JsonObject
public class InstrumentEntity {

    private String instrumentName;
    private String instrumentId;
    private String isinCode;
    private String marketPlace;
    private double marketValue;
    private String marketValueCurrency;
    private double averageBuyPrice;
    private double profit;
    private double holdingsAmount;
    private double usableAmount;

    public String getInstrumentName() {
        return instrumentName;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public String getIsinCode() {
        return isinCode;
    }

    public String getMarketPlace() {
        return marketPlace;
    }

    public double getMarketValue() {
        return marketValue;
    }

    public String getMarketValueCurrency() {
        return marketValueCurrency;
    }

    public double getAverageBuyPrice() {
        return averageBuyPrice;
    }

    public double getProfit() {
        return profit;
    }

    public double getHoldingsAmount() {
        return holdingsAmount;
    }

    public double getUsableAmount() {
        return usableAmount;
    }

    public boolean hasIsinCode() {
        return !Strings.isNullOrEmpty(isinCode);
    }

    public Instrument toTinkInstrument(int typeOfInstrument) {
        return toTinkInstrument(typeOfInstrument, null, null);
    }

    public Instrument toTinkInstrument(int typeOfInstrument, Instrument.Type type) {
        return toTinkInstrument(typeOfInstrument, type, null);
    }

    public Instrument toTinkInstrument(int typeOfInstrument, InstrumentDetailsEntity instrumentDetailsEntity) {
        return toTinkInstrument(typeOfInstrument, null, instrumentDetailsEntity);
    }

    public Instrument toTinkInstrument(
            int typeOfInstrument, Instrument.Type type, InstrumentDetailsEntity instrumentDetails) {
        Instrument instrument = new Instrument();
        instrument.setUniqueIdentifier(resolveUniqueIdentifier(instrumentDetails));
        instrument.setName(instrumentName);
        instrument.setMarketPlace(resolveMarketPlace(instrumentDetails));
        instrument.setIsin(isinCode);
        instrument.setMarketValue(marketValue);
        instrument.setCurrency(marketValueCurrency);
        instrument.setAverageAcquisitionPrice(averageBuyPrice);
        instrument.setProfit(profit);
        instrument.setQuantity(holdingsAmount);
        instrument.setType(resolveType(type, instrumentDetails));
        instrument.setRawType(resolveRawType(typeOfInstrument, type, instrumentDetails));
        return instrument;
    }

    private String resolveMarketPlace(InstrumentDetailsEntity instrumentDetails) {
        return instrumentDetails != null ? instrumentDetails.getMarket() : marketPlace;
    }

    private String resolveUniqueIdentifier(InstrumentDetailsEntity instrumentDetails) {
        String marketPlace = resolveMarketPlace(instrumentDetails);
        return isinCode + ":" + marketPlace;
    }

    private Instrument.Type resolveType(Instrument.Type type, InstrumentDetailsEntity instrumentDetails) {
        return type != null ? type : instrumentDetails != null ? instrumentDetails.getType() : Instrument.Type.OTHER;
    }

    private String resolveRawType(int typeOfInstrument, Instrument.Type type, InstrumentDetailsEntity instrumentDetails) {
        String instrumentGroup = instrumentDetails != null ? instrumentDetails.getInstrumentGroup() : "";
        String stringType = type != null ? type.toString() : "";
        return String.format("%s:%s:%s", typeOfInstrument, stringType, instrumentGroup);
    }
}
