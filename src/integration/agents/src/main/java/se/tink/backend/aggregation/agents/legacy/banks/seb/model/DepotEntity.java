package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.banks.seb.SEBAgentUtils;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
import se.tink.libraries.strings.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DepotEntity {
    @JsonIgnore
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @JsonProperty("DEPA_ID")
    private String id;

    @JsonProperty("DEPA_BEN")
    private String name;

    @JsonProperty("DEPA_KORT_NAMN")
    private String shortName;

    @JsonProperty("KHAV")
    private String owner;

    @JsonProperty("DISP_BELOPP_SEK")
    private double availableAmount;

    @JsonProperty("DEPA_TYP_TXT")
    private String type;

    @JsonProperty("DEPA_TYP_KOD")
    private Integer depotTypeCode;

    @JsonProperty("TOT_PER_SINCE_BUY")
    private double profitInPercentage;

    @JsonProperty("SUM_SECURITIES_VALUE")
    private String sumOfSecurities;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public double getAvailableAmount() {
        return availableAmount;
    }

    public void setAvailableAmount(double availableAmount) {
        this.availableAmount = availableAmount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getDepotTypeCode() {
        return depotTypeCode;
    }

    public void setDepotTypeCode(Integer depotTypeCode) {
        this.depotTypeCode = depotTypeCode;
    }

    public double getProfitInPercentage() {
        return profitInPercentage;
    }

    public void setProfitInPercentage(double profitInPercentage) {
        this.profitInPercentage = profitInPercentage;
    }

    public Double getSumOfSecurities() {
        return sumOfSecurities == null || sumOfSecurities.isEmpty()
                ? 0
                : StringUtils.parseAmount(sumOfSecurities);
    }

    public void setSumOfSecurities(String sumOfSecurities) {
        this.sumOfSecurities = sumOfSecurities;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Account toAccount(String accountNumber) {
        Account account = new Account();

        account.setAccountNumber(accountNumber);
        account.setBalance(getSumOfSecurities() + getAvailableAmount());
        account.setBankId(accountNumber);
        account.setName(StringUtils.firstLetterUppercaseFormatting(getType().trim()));
        account.setType(AccountTypes.INVESTMENT);
        account.setCapabilities(SEBAgentUtils.getInvestmentAccountCapabilities());
        account.setHolderName(!Strings.isNullOrEmpty(owner) ? owner.trim() : null);
        account.setSourceInfo(
                AccountSourceInfo.builder()
                        .bankAccountType(getType())
                        .bankProductCode(getName())
                        .bankProductCode(String.format("%d", getDepotTypeCode()))
                        .build());

        return account;
    }

    public Portfolio toPortfolio() {
        Portfolio portfolio = new Portfolio();

        portfolio.setTotalValue(getSumOfSecurities());
        portfolio.setCashValue(getAvailableAmount());
        portfolio.setRawType(getType().trim());
        portfolio.setType(getPortfolioType());
        portfolio.setUniqueIdentifier(getId());

        return portfolio;
    }

    private Portfolio.Type getPortfolioType() {
        switch (getDepotTypeCode()) {
            case 85:
            case 86:
            case 88:
            case 91:
            case 94:
            case 97:
            case 100:
            case 112:
            case 119:
                return Portfolio.Type.ISK;
            default:
                logger.info(
                        String.format(
                                "SEB portfolio type - code: %s, type: %s",
                                getDepotTypeCode(), getType().trim()));
                return Portfolio.Type.OTHER;
        }
    }
}
