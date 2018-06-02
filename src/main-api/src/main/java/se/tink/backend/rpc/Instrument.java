package se.tink.backend.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Instrument {

    @ApiModelProperty(value="The internal identifier of the user which owns the instrument.", example = "a52e9890520d4ec38cc0d4526a4cdcbe")
    private UUID userId;

    @ApiModelProperty(value="The internal identifier of the portfolio which the instrument belongs to.", example = "01f21bc10f2b46abb9b25fccd3dc64eb")
    private UUID portfolioId;

    @ApiModelProperty(value="An International Securities Identification Number (ISIN) uniquely identifies a security.", example = "US0378331005")
    private String isin;

    @ApiModelProperty(value="The market where the instrument is traded.", example = "NASDAQ")
    private String marketPlace;

    @ApiModelProperty(value="The internal identifier of the instrument.", example = "50c3e10233ed4048bd48f3a55b5d062a")
    private UUID id;

    @ApiModelProperty(value="An instrument can be traded multiple times and this is the average acquisition price calculated over all trades.", example = "53.41")
    private Double averageAcquisitionPrice;

    @ApiModelProperty(value="The currency that the instrument is traded in.", example = "SEK")
    private String currency;

    @ApiModelProperty(value="The current market value of the whole instrument. That is, not for a single share but for the entire instrument.", example = "22917.00")
    private Double marketValue;

    @ApiModelProperty(value="The name of the instrument, which can be different on different markets.", example = "Apple Inc.")
    private String name;

    @ApiModelProperty(value="The current market price for one share of the instrument.", example = "76.39")
    private Double price;

    @ApiModelProperty(value="The number of underlying shares that the user owns of this instrument.", example = "300.00")
    private Double quantity;

    @ApiModelProperty(value="The total profit for this instrument over all trades.", example = "6894.00")
    private Double profit;

    @ApiModelProperty(value="A ticker symbol is an abbreviation used to uniquely identify a stock on a particular stock market.", example = "AAPL")
    private String ticker;

    @ApiModelProperty(value="The instrument type.", example = "STOCK", allowableValues = se.tink.backend.core.Instrument.Type.DOCUMENTED)
    private se.tink.backend.core.Instrument.Type type;

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

    public se.tink.backend.core.Instrument.Type getType() {
        return type;
    }

    public void setType(se.tink.backend.core.Instrument.Type type) {
        this.type = type;
    }
}
