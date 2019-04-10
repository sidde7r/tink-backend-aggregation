package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.investment.entities;

import java.util.Map;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PortfolioContentDetails {
    private String id;
    private String fullName;
    private String categoryId;
    private String status;
    private boolean canView;
    private double marketValue;
    private double purchaseValue;
    private double amount;
    private double marketValueChange;
    private double marketValueChangeInPercent;
    private double purchasePrice;
    private String currency;
    private int openOrderCount;
    private boolean canBuy;
    private boolean complex;

    public Instrument toTinkInstrument(Map<String, String> fundIdIsinMapper) {
        String isin = fundIdIsinMapper.get(id);
        Instrument instrument = new Instrument();
        instrument.setType(Instrument.Type.FUND);
        instrument.setName(fullName);
        instrument.setRawType(categoryId);
        instrument.setCurrency(currency);
        instrument.setQuantity(amount);
        instrument.setMarketValue(marketValue);
        instrument.setProfit(marketValueChange);
        instrument.setPrice(purchasePrice);
        instrument.setIsin(isin);
        instrument.setUniqueIdentifier(id);

        return instrument;
    }
}
