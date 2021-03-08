package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.banks.seb.SEBAgentUtils;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FundAccountEntity {
    @JsonProperty("KONTO_NR")
    private String accountNumber;

    @JsonProperty("KTOSLAG_TXT")
    private String accountType;

    @JsonProperty("DISP_BEL")
    private Double disposableIncome;

    @JsonProperty("FONDID")
    private String fundId;

    @JsonProperty("ISIN_CODE")
    private String isin;

    @JsonProperty("FONDNAMN")
    private String name;

    @JsonProperty("ANDEL_ANT")
    private Double quantity;

    @JsonProperty("KURS")
    private Double price;

    @JsonProperty("VALUTA")
    private String currency;

    @JsonProperty("GAV")
    private Double averageAcquisitionCost;

    @JsonProperty("ACQUISITION_COST")
    private Double totalAcquisitionCost;

    @JsonProperty("MARKET_VALUE")
    private Double marketValue;

    @JsonProperty("FMS_ID")
    private String shortId;

    @JsonProperty("KHAV")
    private String owner;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public Double getDisposableIncome() {
        return disposableIncome;
    }

    public void setDisposableIncome(Double disposableIncome) {
        this.disposableIncome = disposableIncome;
    }

    public String getFundId() {
        return fundId;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public void setFundId(String fundId) {
        this.fundId = fundId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getAverageAcquisitionCost() {
        return averageAcquisitionCost;
    }

    public void setAverageAcquisitionCost(Double averageAcquisitionCost) {
        this.averageAcquisitionCost = averageAcquisitionCost;
    }

    public Double getTotalAcquisitionCost() {
        return totalAcquisitionCost;
    }

    public void setTotalAcquisitionCost(Double totalAcquisitionCost) {
        this.totalAcquisitionCost = totalAcquisitionCost;
    }

    public Double getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(Double marketValue) {
        this.marketValue = marketValue;
    }

    public String getShortId() {
        return shortId;
    }

    public void setShortId(String shortId) {
        this.shortId = shortId;
    }

    public Account toAccount() {
        Account account = new Account();

        account.setAccountNumber(getAccountNumber());
        account.setBankId(getAccountNumber());
        account.setBalance(getDisposableIncome());
        account.setName(getAccountType());
        account.setType(AccountTypes.INVESTMENT);
        account.setCapabilities(SEBAgentUtils.getInvestmentAccountCapabilities());
        account.setHolderName(owner);
        account.setSourceInfo(
                AccountSourceInfo.builder()
                        .bankAccountType(getAccountType())
                        .bankProductCode(getName())
                        .build());

        return account;
    }

    public Portfolio toPortfolio() {
        Portfolio portfolio = new Portfolio();

        portfolio.setRawType(getAccountType());
        portfolio.setCashValue(getDisposableIncome());
        portfolio.setType(Portfolio.Type.DEPOT);
        portfolio.setUniqueIdentifier(getAccountNumber());

        return portfolio;
    }

    public Optional<Instrument> toInstrument() {
        Instrument instrument = new Instrument();

        if (getQuantity() == 0) {
            return Optional.empty();
        }

        instrument.setAverageAcquisitionPrice(getAverageAcquisitionCost());
        instrument.setCurrency(getCurrency());
        instrument.setMarketValue(getMarketValue());
        instrument.setName(getName());
        instrument.setPrice(getPrice());
        instrument.setProfit(getMarketValue() - getTotalAcquisitionCost());
        instrument.setQuantity(getQuantity());
        instrument.setRawType(getAccountType());
        instrument.setType(Instrument.Type.FUND);
        instrument.setUniqueIdentifier(getFundId() + getShortId());
        instrument.setIsin(getIsin());

        return Optional.of(instrument);
    }
}
