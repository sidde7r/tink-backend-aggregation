package se.tink.backend.aggregation.agents.banks.skandiabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import org.assertj.core.util.Strings;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.strings.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InvestmentEntity {
    private static final AggregationLogger LOGGER = new AggregationLogger(InvestmentEntity.class);

    private String id;
    private List<PortfolioEntity> portfolios;
    private String investmentAmount;
    private String investmentName;
    private String investmentNumber;
    private String investmentStyle;
    private String subType;
    private String givenName;
    private String surname;
    private int investmentDetailsStatus;
    private int ordersStatus;
    private int transferStatus;
    private int type;
    private int investmentStatus;
    private String disposableAmount;

    public double getDisposableAmount() {
        return Strings.isNullOrEmpty(disposableAmount)
                ? 0D
                : StringUtils.parseAmount(disposableAmount);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<PortfolioEntity> getPortfolios() {
        return portfolios;
    }

    public void setPortfolios(List<PortfolioEntity> portfolios) {
        this.portfolios = portfolios;
    }

    public double getInvestmentAmount() {
        return investmentAmount == null || investmentAmount.isEmpty()
                ? 0d
                : StringUtils.parseAmount(investmentAmount);
    }

    public void setInvestmentAmount(String investmentAmount) {
        this.investmentAmount = investmentAmount;
    }

    public String getInvestmentName() {
        return investmentName;
    }

    public void setInvestmentName(String investmentName) {
        this.investmentName = investmentName;
    }

    public String getInvestmentNumber() {
        return investmentNumber;
    }

    public void setInvestmentNumber(String investmentNumber) {
        this.investmentNumber = investmentNumber;
    }

    public String getInvestmentStyle() {
        return investmentStyle;
    }

    public void setInvestmentStyle(String investmentStyle) {
        this.investmentStyle = investmentStyle;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public int getInvestmentDetailsStatus() {
        return investmentDetailsStatus;
    }

    public void setInvestmentDetailsStatus(int investmentDetailsStatus) {
        this.investmentDetailsStatus = investmentDetailsStatus;
    }

    public int getOrdersStatus() {
        return ordersStatus;
    }

    public void setOrdersStatus(int ordersStatus) {
        this.ordersStatus = ordersStatus;
    }

    public int getTransferStatus() {
        return transferStatus;
    }

    public void setTransferStatus(int transferStatus) {
        this.transferStatus = transferStatus;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getInvestmentStatus() {
        return investmentStatus;
    }

    public void setInvestmentStatus(int investmentStatus) {
        this.investmentStatus = investmentStatus;
    }

    public Account toAccount() {
        Account account = new Account();

        account.setAccountNumber(getInvestmentNumber());
        account.setBankId(getId());
        account.setName(getSubType());
        account.setType(AccountTypes.INVESTMENT);
        account.setBalance(getInvestmentAmount() + getDisposableAmount());

        return account;
    }

    public Portfolio toPortfolio() {
        Portfolio portfolio = new Portfolio();

        portfolio.setRawType(getSubType());
        portfolio.setTotalValue(getInvestmentAmount());
        portfolio.setType(getPortfolioType());
        portfolio.setUniqueIdentifier(getId());

        return portfolio;
    }

    private Portfolio.Type getPortfolioType() {
        switch (getSubType().toLowerCase()) {
            case "investeringssparkonto":
                return Portfolio.Type.ISK;
            case "kapitalförsäkring - internet":
            case "kapitalförsäkring":
                return Portfolio.Type.KF;
            case "tjänstepension":
                return Portfolio.Type.PENSION;
            case "värdepappersdepå":
                return Portfolio.Type.DEPOT;
            default:
                LOGGER.warnExtraLong(
                        SerializationUtils.serializeToString(this),
                        LogTag.from("#Skandiabanken_unknown_portfolio_typ"));
                return Portfolio.Type.OTHER;
        }
    }
}
