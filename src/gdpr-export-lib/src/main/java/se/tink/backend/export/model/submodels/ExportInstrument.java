package se.tink.backend.export.model.submodels;

public class ExportInstrument {

    private final String portfolioName;
    private final String type;
    private final String rawType;
    private final Double averageAcquisitionPrice;
    private final String currency;
    private final String isin;
    private final String marketplace;
    private final Double marketValue;
    private final Double price;
    private final Double profit;
    private final Double quantity;
    private final String ticker;

    public ExportInstrument(String portfolioName, String type, String rawType,
            Double averageAcquisitionPrice,
            String currency, String isin, String marketplace, Double marketValue, Double price, Double profit,
            Double quantity, String ticker) {
        this.portfolioName = portfolioName;
        this.type = type;
        this.rawType = rawType;
        this.averageAcquisitionPrice = averageAcquisitionPrice;
        this.currency = currency;
        this.isin = isin;
        this.marketplace = marketplace;
        this.marketValue = marketValue;
        this.price = price;
        this.profit = profit;
        this.quantity = quantity;
        this.ticker = ticker;
    }

    public String getPortfolioName() {
        return portfolioName;
    }

    public String getType() {
        return type;
    }

    public String getRawType() {
        return rawType;
    }

    public Double getAverageAcquisitionPrice() {
        return averageAcquisitionPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public String getIsin() {
        return isin;
    }

    public String getMarketplace() {
        return marketplace;
    }

    public Double getMarketValue() {
        return marketValue;
    }

    public Double getPrice() {
        return price;
    }

    public Double getProfit() {
        return profit;
    }

    public Double getQuantity() {
        return quantity;
    }

    public String getTicker() {
        return ticker;
    }
}
