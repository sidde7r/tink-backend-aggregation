package se.tink.backend.core;

import java.util.Objects;
import java.util.UUID;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

@Table(value = "instruments")
public class Instrument {

    public enum Type {
        FUND, STOCK, OTHER;

        public static final String DOCUMENTED = "FUND, STOCK, OTHER";
    }

    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID userId;
    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private UUID portfolioId;
    @PrimaryKeyColumn(ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private UUID id;
    private String isin; // An International Securities Identification Number (ISIN) uniquely identifies a security.
    private String marketPlace;
    private Double averageAcquisitionPrice;
    private String currency;
    private Double marketValue;
    private String name;
    private Double price;
    private Double quantity;
    private Double profit;
    private String ticker;
    private Type type;
    private String rawType;
    // Normally the uniqueIdentifier should be isin + market.
    // If isin and market is hard to get hold of and the bank / broker have some other way to identify the instrument
    // we can use that.
    private String uniqueIdentifier;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(UUID portfolioId) {
        this.portfolioId = portfolioId;
    }

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getMarketPlace() {
        return marketPlace;
    }

    public void setMarketPlace(String marketPlace) {
        this.marketPlace = marketPlace;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Double getAverageAcquisitionPrice() {
        return averageAcquisitionPrice;
    }

    public void setAverageAcquisitionPrice(Double averageAcquisitionPrice) {
        this.averageAcquisitionPrice = averageAcquisitionPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(Double marketValue) {
        this.marketValue = marketValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getProfit() {
        return profit;
    }

    public void setProfit(Double profit) {
        this.profit = profit;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getRawType() {
        return rawType;
    }

    public void setRawType(String rawType) {
        this.rawType = rawType;
    }

    // Check static fields when checking equality.
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Instrument)) {
            return false;
        }

        Instrument instrument = (Instrument) o;

        if (this.userId == null || instrument.userId == null || !Objects.equals(this.userId, instrument.userId)) {
            return false;
        }

        if (this.portfolioId == null || instrument.portfolioId == null ||
                !Objects.equals(this.portfolioId, instrument.portfolioId)) {
            return false;
        }

        if (this.uniqueIdentifier == null || instrument.uniqueIdentifier == null ||
                !Objects.equals(this.uniqueIdentifier, instrument.uniqueIdentifier)) {
            return false;
        }

        if (this.type == null || instrument.type == null || !Objects.equals(this.type, instrument.type)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, portfolioId, uniqueIdentifier, type);
    }
}
