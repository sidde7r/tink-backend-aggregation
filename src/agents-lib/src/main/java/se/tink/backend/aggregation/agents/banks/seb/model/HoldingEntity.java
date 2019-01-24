package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.libraries.strings.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HoldingEntity {
    @JsonProperty("DEPA_NR")
    private long depotNumber;
    @JsonProperty("DEPA_BEN")
    private String depotName;
    @JsonProperty("INNEH_ID")
    private String shortId;
    @JsonProperty("VP_NR")
    private String isin;
    @JsonProperty("VALUTAKOD")
    private String currency;
    @JsonProperty("VALUTA_KOD_VP")
    private String holdingCurrency;
    @JsonProperty("MARKNAD_VARDE_BEL")
    private String marketValue;
    @JsonProperty("FOND_ID")
    private long fundId;
    @JsonProperty("FOND_TYP_KOD")
    private long fundType;
    @JsonProperty("ANT_NOM")
    private String quantity;
    @JsonProperty("VP_FULLST_NAMN1")
    private String name;
    @JsonProperty("VARDEPAPPERS_TYP")
    private String type;
    @JsonProperty("VP_KURS")
    private double price;
    @JsonProperty("PERCENT_SINCE_BUY")
    private String profitInPercentage;
    @JsonProperty("ACQUISITION_VALUE")
    private String acquisitionPrice;
    @JsonProperty("AVG_ACQUI_PRICE")
    private String averageAcquisitionValue;
    @JsonProperty("UNREALIZED_RESULT")
    private String profit;
    @JsonProperty("MKT")
    private String market;
    @JsonProperty("LISTA")
    private String list;
    @JsonProperty("LANDKOD")
    private String land;

    public long getDepotNumber() {
        return depotNumber;
    }

    public void setDepotNumber(long depotNumber) {
        this.depotNumber = depotNumber;
    }

    public String getDepotName() {
        return depotName;
    }

    public void setDepotName(String depotName) {
        this.depotName = depotName;
    }

    public String getShortId() {
        return shortId;
    }

    public void setShortId(String shortId) {
        this.shortId = shortId;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getHoldingCurrency() {
        return holdingCurrency;
    }

    public void setHoldingCurrency(String holdingCurrency) {
        this.holdingCurrency = holdingCurrency;
    }

    public Double getMarketValue() {
        return marketValue == null || marketValue.isEmpty() ? null : parseQuantity(marketValue);
    }

    public void setMarketValue(String marketValue) {
        this.marketValue = marketValue;
    }

    public long getFundId() {
        return fundId;
    }

    public void setFundId(long fundId) {
        this.fundId = fundId;
    }

    public long getFundType() {
        return fundType;
    }

    public void setFundType(long fundType) {
        this.fundType = fundType;
    }

    public Double getQuantity() {
        return quantity == null || quantity.isEmpty() ? null : StringUtils.parseAmount(quantity);
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getProfitInPercentage() {
        return profitInPercentage;
    }

    public void setProfitInPercentage(String profitInPercentage) {
        this.profitInPercentage = profitInPercentage;
    }

    public String getAcquisitionPrice() {
        return acquisitionPrice;
    }

    public void setAcquisitionPrice(String acquisitionPrice) {
        this.acquisitionPrice = acquisitionPrice;
    }

    public Double getAverageAcquisitionValue() {
        return averageAcquisitionValue == null || averageAcquisitionValue.isEmpty() ?
                null : StringUtils.parseAmount(averageAcquisitionValue);
    }

    public void setAverageAcquisitionValue(String averageAcquisitionValue) {
        this.averageAcquisitionValue = averageAcquisitionValue;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getList() {
        return list;
    }

    public void setList(String list) {
        this.list = list;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Double getProfit() {
        return profit == null || profit.isEmpty() ? null : StringUtils.parseAmount(profit);
    }

    public void setProfit(String profit) {
        this.profit = profit;
    }

    public String getLand() {
        return land;
    }

    public void setLand(String land) {
        this.land = land;
    }

    public Optional<Instrument> toInstrument() {
        Instrument instrument = new Instrument();

        if (getQuantity() == 0) {
            return Optional.empty();
        }

        instrument.setAverageAcquisitionPrice(getAverageAcquisitionValue());
        instrument.setCurrency(getCurrency());
        instrument.setIsin(getIsin());
        instrument.setMarketPlace(getMarket());
        instrument.setMarketValue(getMarketValue());
        instrument.setName(getName());
        instrument.setPrice(getPrice());
        instrument.setProfit(getProfit());
        instrument.setQuantity(getQuantity());
        instrument.setRawType(getType());
        instrument.setType(getInstrumentType());
        instrument.setUniqueIdentifier(getIsin() + getLand());

        return Optional.of(instrument);
    }

    private Instrument.Type getInstrumentType() {
        switch (getType().toLowerCase().trim()) {
        case "aktie":
            return Instrument.Type.STOCK;
        case "borshandladfond":
            return Instrument.Type.FUND;
        case "obligation":
            // Intentional fall through
        default:
            return Instrument.Type.OTHER;
        }
    }

    public Double parseQuantity(String quantity) {
        if (quantity.charAt(quantity.length() - 4) == '.') {
            quantity = quantity.replace(".", "");
        }
        return StringUtils.parseAmountUS(quantity);
    }
}
