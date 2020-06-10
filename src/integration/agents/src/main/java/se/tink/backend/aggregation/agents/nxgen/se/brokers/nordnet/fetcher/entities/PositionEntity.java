package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdModule;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PositionEntity {
    @JsonProperty("accno")
    private String accountNumber;

    @JsonProperty("accid")
    private String accountId;

    private InstrumentEntity instrument;

    @JsonProperty("main_market_price")
    private AmountEntity mainMarketPrice;

    @JsonProperty("morning_price")
    private AmountEntity morningPrice;

    @JsonProperty("qty")
    private double quantity;

    @JsonProperty("pawn_percent")
    private int pawnPercentage;

    @JsonProperty("market_value_acc")
    private AmountEntity accountCurrencyMarketValue;

    @JsonProperty("market_value")
    private AmountEntity marketValue;

    @JsonProperty("acq_price")
    private AmountEntity acquisitionPrice;

    @JsonProperty("acq_price_acc")
    private AmountEntity accountCurrencyAcquisitionPrice;

    @JsonProperty("is_custom_gav")
    private boolean isCustomerGav;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public InstrumentEntity getInstrument() {
        return instrument;
    }

    public void setInstrument(InstrumentEntity instrument) {
        this.instrument = instrument;
    }

    public AmountEntity getMainMarketPrice() {
        return mainMarketPrice;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public AmountEntity getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(AmountEntity marketValue) {
        this.marketValue = marketValue;
    }

    public AmountEntity getAcquisitionPrice() {
        return acquisitionPrice;
    }

    private InstrumentModule.InstrumentType getType() {
        if (getInstrument() == null || getInstrument().getGroupType() == null) {
            return InstrumentModule.InstrumentType.OTHER;
        }

        switch (getInstrument().getGroupType().toLowerCase()) {
            case "eq":
                return InstrumentModule.InstrumentType.STOCK;
            case "fnd":
                return InstrumentModule.InstrumentType.FUND;
            default:
                return InstrumentModule.InstrumentType.OTHER;
        }
    }

    private InstrumentIdModule getIdModule() {
        return InstrumentIdModule.builder()
                .withUniqueIdentifier(instrument.getIsin())
                .withName(instrument.getName())
                .setIsin(instrument.getIsin())
                .setMarketPlace(instrument.getInstitute())
                .build();
    }

    public Optional<InstrumentModule> toTinkInstrument() {
        return Optional.of(
                InstrumentModule.builder()
                        .withType(getType())
                        .withId(getIdModule())
                        .withMarketPrice(mainMarketPrice.getValue().doubleValue())
                        .withMarketValue(marketValue.getValue().doubleValue())
                        .withAverageAcquisitionPrice(acquisitionPrice.getValue().doubleValue())
                        .withCurrency(instrument.getCurrency())
                        .withQuantity(quantity)
                        .withProfit(getProfit())
                        .setRawType(
                                String.format(
                                        "type: %s, group type: %s",
                                        instrument.getType(), instrument.getGroupType()))
                        .setTicker(instrument.getSymbol())
                        .build());
    }

    private Double getProfit() {
        if (getQuantity() == 0) {
            return null;
        }

        if (getMarketValue() == null || getMarketValue().getValue().intValue() == 0) {
            return null;
        }

        if (getAcquisitionPrice() == null || getAcquisitionPrice().getValue().intValue() == 0) {
            return null;
        }

        return getMarketValue().getValue().intValue()
                - (getQuantity() * getAcquisitionPrice().getValue().intValue());
    }
}
