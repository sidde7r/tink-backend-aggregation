package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.investment.entities;

import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.system.rpc.Instrument;

@JsonObject
public class SecuritiesEntity {
    private MasterDataEntity masterData;
    private Double quote;
    private long quoteTimestamp;
    private Boolean realTimeQuote;
    private String customerCurrency;
    private Double marketValue;
    private Double amount;
    private Double amountTradable;
    private Double returnThisYear;
    private Double returnSinceBought;

    public MasterDataEntity getMasterData() {
        return masterData;
    }

    public Double getQuote() {
        return quote;
    }

    public long getQuoteTimestamp() {
        return quoteTimestamp;
    }

    public Boolean getRealTimeQuote() {
        return realTimeQuote;
    }

    public String getCustomerCurrency() {
        return customerCurrency;
    }

    public Double getMarketValue() {
        return marketValue;
    }

    public Double getAmount() {
        return amount;
    }

    public Double getAmountTradable() {
        return amountTradable;
    }

    public Double getReturnThisYear() {
        return returnThisYear;
    }

    public Double getReturnSinceBought() {
        return returnSinceBought;
    }

    Optional<Instrument> toTinkInstrument() {
        if (masterData == null || !masterData.hasIsin()) {
            return Optional.empty();
        }
        Instrument instrument = new Instrument();
        instrument.setType(Instrument.Type.FUND);

        instrument.setRawType(masterData.getNameLong());
        String isin = masterData.getIsin();
        instrument.setIsin(isin);
        instrument.setUniqueIdentifier(isin + "Jyske-DK");
        instrument.setQuantity(getAmount());
        instrument.setMarketValue(getMarketValue());
        instrument.setProfit(getReturnSinceBought());
        instrument.setPrice(getQuote());
        instrument.setCurrency(customerCurrency);
        return Optional.of(instrument);
    }
}
