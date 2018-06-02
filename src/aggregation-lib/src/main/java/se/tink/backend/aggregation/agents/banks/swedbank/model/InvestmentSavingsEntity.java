package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.system.rpc.Portfolio;
import se.tink.backend.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InvestmentSavingsEntity {
    private AmountEntity value;
    private List<OperationsEntity> operations;
    private List<IskPlacementEntity> placements;
    private String fundAccountId;
    private String changePercent;
    private Boolean dispositionRight;
    private TotalEquitiesEntity totalEquities;
    private String name;
    private String fullyFormattedNumber;

    public AmountEntity getValue() {
        return value;
    }

    public void setValue(AmountEntity value) {
        this.value = value;
    }

    public List<OperationsEntity> getOperations() {
        return operations;
    }

    public void setOperations(
            List<OperationsEntity> operations) {
        this.operations = operations;
    }

    public List<IskPlacementEntity> getPlacements() {
        return placements;
    }

    public void setPlacements(
            List<IskPlacementEntity> placements) {
        this.placements = placements;
    }

    public String getFundAccountId() {
        return fundAccountId;
    }

    public void setFundAccountId(String fundAccountId) {
        this.fundAccountId = fundAccountId;
    }

    public String getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(String changePercent) {
        this.changePercent = changePercent;
    }

    public Boolean getDispositionRight() {
        return dispositionRight;
    }

    public void setDispositionRight(Boolean dispositionRight) {
        this.dispositionRight = dispositionRight;
    }

    public TotalEquitiesEntity getTotalEquities() {
        return totalEquities;
    }

    public void setTotalEquities(TotalEquitiesEntity totalEquities) {
        this.totalEquities = totalEquities;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullyFormattedNumber() {
        return fullyFormattedNumber;
    }

    public void setFullyFormattedNumber(String fullyFormattedNumber) {
        this.fullyFormattedNumber = fullyFormattedNumber;
    }

    public Optional<Account> toAccount() {
        if (getValue() == null || getValue().getAmount() == null) {
            return Optional.empty();
        }

        Account account = new Account();

        account.setType(AccountTypes.INVESTMENT);
        account.setName(getName());
        account.setAccountNumber(getFullyFormattedNumber());
        account.setBankId(getFullyFormattedNumber());
        account.setBalance(StringUtils.parseAmount(getValue().getAmount()));

        return Optional.of(account);
    }

    public Portfolio toPortfolio(Double cashValue) {
        Portfolio portfolio = new Portfolio();

        portfolio.setUniqueIdentifier(getFullyFormattedNumber());
        portfolio.setRawType(getName());
        portfolio.setType(Portfolio.Type.ISK);
        portfolio.setTotalValue(StringUtils.parseAmount(getValue() != null && getValue().getAmount() != null ?
                getValue().getAmount() : null));
        portfolio.setCashValue(cashValue);

        return portfolio;
    }
}
