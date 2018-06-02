package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.system.rpc.Portfolio;
import se.tink.backend.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FundAccountsEntity {
    private List<OperationsEntity> operations;
    private List<PlacementEntity> placements;
    private boolean rightOfDisposal;
    private TotalFundAccountEntity totalFundAccount;
    private String name;
    private String id;
    private String accountNumber;
    private String clearingNumber;
    private String fullyFormattedNumber;

    public List<OperationsEntity> getOperations() {
        return operations;
    }

    public void setOperations(
            List<OperationsEntity> operations) {
        this.operations = operations;
    }

    public List<PlacementEntity> getPlacements() {
        return placements;
    }

    public void setPlacements(List<PlacementEntity> placements) {
        this.placements = placements;
    }

    public boolean isRightOfDisposal() {
        return rightOfDisposal;
    }

    public void setRightOfDisposal(boolean rightOfDisposal) {
        this.rightOfDisposal = rightOfDisposal;
    }

    public TotalFundAccountEntity getTotalFundAccount() {
        return totalFundAccount;
    }

    public void setTotalFundAccount(
            TotalFundAccountEntity totalFundAccount) {
        this.totalFundAccount = totalFundAccount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getClearingNumber() {
        return clearingNumber;
    }

    public void setClearingNumber(String clearingNumber) {
        this.clearingNumber = clearingNumber;
    }

    public String getFullyFormattedNumber() {
        return fullyFormattedNumber;
    }

    public void setFullyFormattedNumber(String fullyFormattedNumber) {
        this.fullyFormattedNumber = fullyFormattedNumber;
    }

    public Optional<Account> toAccount() {
        if (getTotalFundAccount() == null || getTotalFundAccount().getValue() == null ||
                getTotalFundAccount().getValue().getAmount() == null) {
            return Optional.empty();
        }

        Account account = new Account();

        account.setAccountNumber(getFullyFormattedNumber());
        account.setBankId(getFullyFormattedNumber());
        account.setBalance(getAmountAsDouble(getTotalFundAccount().getValue()));
        account.setName(getName());
        account.setType(AccountTypes.INVESTMENT);

        return Optional.of(account);
    }

    public Portfolio toPortfolio() {
        Portfolio portfolio = new Portfolio();

        portfolio.setRawType(getName());
        portfolio.setType(Portfolio.Type.DEPOT);
        portfolio.setTotalValue(getAmountAsDouble(getTotalFundAccount().getValue()));
        portfolio.setUniqueIdentifier(getAccountNumber());

        return portfolio;
    }

    private Double getAmountAsDouble(AmountEntity amountEntity) {
        if (amountEntity == null) {
            return null;
        }

        if (amountEntity.getAmount() == null) {
            return null;
        }

        return StringUtils.parseAmount(amountEntity.getAmount());
    }
}
