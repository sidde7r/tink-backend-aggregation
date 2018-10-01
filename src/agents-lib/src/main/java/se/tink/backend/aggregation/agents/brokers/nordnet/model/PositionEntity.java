package se.tink.backend.aggregation.agents.brokers.nordnet.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.system.rpc.Instrument;

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

    public void setMainMarketPrice(AmountEntity mainMarketPrice) {
        this.mainMarketPrice = mainMarketPrice;
    }

    public AmountEntity getMorningPrice() {
        return morningPrice;
    }

    public void setMorningPrice(AmountEntity morningPrice) {
        this.morningPrice = morningPrice;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public int getPawnPercentage() {
        return pawnPercentage;
    }

    public void setPawnPercentage(int pawnPercentage) {
        this.pawnPercentage = pawnPercentage;
    }

    public AmountEntity getAccountCurrencyMarketValue() {
        return accountCurrencyMarketValue;
    }

    public void setAccountCurrencyMarketValue(
            AmountEntity accountCurrencyMarketValue) {
        this.accountCurrencyMarketValue = accountCurrencyMarketValue;
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

    public void setAcquisitionPrice(AmountEntity acquisitionPrice) {
        this.acquisitionPrice = acquisitionPrice;
    }

    public AmountEntity getAccountCurrencyAcquisitionPrice() {
        return accountCurrencyAcquisitionPrice;
    }

    public void setAccountCurrencyAcquisitionPrice(
            AmountEntity accountCurrencyAcquisitionPrice) {
        this.accountCurrencyAcquisitionPrice = accountCurrencyAcquisitionPrice;
    }

    public boolean isCustomerGav() {
        return isCustomerGav;
    }

    public void setCustomerGav(boolean customerGav) {
        isCustomerGav = customerGav;
    }

    public Optional<Instrument> toInstrument() {
        Instrument instrument = new Instrument();

        if (getQuantity() == 0 || getInstrument() == null) {
            return Optional.empty();
        }

        instrument.setAverageAcquisitionPrice(getAcquisitionPrice() != null ? getAcquisitionPrice().getValue() : null);
        instrument.setCurrency(getInstrument().getCurrency());
        instrument.setIsin(getInstrument().getIsin());
        instrument.setMarketValue(getMarketValue() != null ? getMarketValue().getValue() : null);
        instrument.setName(getInstrument().getName());
        instrument.setPrice(getMainMarketPrice() != null ? getMainMarketPrice().getValue() : null);
        instrument.setProfit(getProfit());
        instrument.setQuantity(getQuantity());
        instrument.setRawType(String.format("type: %s, group type: %s", getInstrument().getType(),
                getInstrument().getGroupType()));
        instrument.setType(getInstrumentType());
        instrument.setTicker(getInstrument().getSymbol());
        instrument.setUniqueIdentifier(getInstrument().getIsin() + getInstrument().getId());

        return Optional.of(instrument);
    }

    private Instrument.Type getInstrumentType() {
        if (getInstrument() == null || getInstrument().getGroupType() == null) {
            return Instrument.Type.OTHER;
        }

        switch (getInstrument().getGroupType().toLowerCase()) {
        case "eq":
            return Instrument.Type.STOCK;
        case "fnd":
            return Instrument.Type.FUND;
        default:
            return Instrument.Type.OTHER;
        }
    }

    private Double getProfit() {
        if (getQuantity() == 0) {
            return null;
        }

        if (getMarketValue() == null || getMarketValue().getValue() == 0) {
            return null;
        }

        if (getAcquisitionPrice() == null || getAcquisitionPrice().getValue() == 0) {
            return null;
        }

        return getMarketValue().getValue() - (getQuantity() * getAcquisitionPrice().getValue());
    }
}
