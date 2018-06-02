package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.system.rpc.Portfolio;
import se.tink.backend.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EndowmentInsuranceEntity {
    private AmountEntity value;
    private LinksEntity links;
    private List<OperationsEntity> operations;
    private List<EndowmentInsurancePlacementEntity> placements;
    private AmountEntity totalValue;
    private String changePercent;
    private String name;
    private String fullyFormattedNumber;
    private TotalEquitiesEntity totalEquities;

    public AmountEntity getValue() {
        return value;
    }

    public void setValue(AmountEntity value) {
        this.value = value;
    }

    public LinksEntity getLinks() {
        return links;
    }

    public void setLinks(LinksEntity links) {
        this.links = links;
    }

    public List<OperationsEntity> getOperations() {
        return operations;
    }

    public void setOperations(
            List<OperationsEntity> operations) {
        this.operations = operations;
    }

    public List<EndowmentInsurancePlacementEntity> getPlacements() {
        return placements;
    }

    public void setPlacements(
            List<EndowmentInsurancePlacementEntity> placements) {
        this.placements = placements;
    }

    public AmountEntity getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(AmountEntity totalValue) {
        this.totalValue = totalValue;
    }

    public String getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(String changePercent) {
        this.changePercent = changePercent;
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

    public TotalEquitiesEntity getTotalEquities() {
        return totalEquities;
    }

    public void setTotalEquities(TotalEquitiesEntity totalEquities) {
        this.totalEquities = totalEquities;
    }

    public Optional<Account> toAccount() {
        if (getTotalValue() == null || getTotalValue().getAmount() == null) {
            return Optional.empty();
        }

        Account account = new Account();

        account.setAccountNumber(getFullyFormattedNumber());
        account.setBankId(getFullyFormattedNumber());
        account.setBalance(StringUtils.parseAmount(getTotalValue().getAmount()));
        account.setName(getName());
        account.setType(AccountTypes.INVESTMENT);

        return Optional.of(account);
    }

    public Portfolio toPortfolio() {
        Portfolio portfolio = new Portfolio();

        portfolio.setRawType(getName());
        portfolio.setType(Portfolio.Type.KF);

        Double totalMarketValue = getTotalValue() != null && getTotalValue().getAmount() != null ?
                StringUtils.parseAmount(getTotalValue().getAmount()) : null;

        portfolio.setTotalValue(totalMarketValue);

        // Transform percentage to real change
        Double changeQuotient = getChangePercent() != null ? StringUtils.parseAmount(getChangePercent()) / 100 : null;

        // Change = Market value - Acquisition value
        // Change percentage = Change / Acquisition value  --> Acquisition value = Change / Change percentage
        // Change = Market value - Change / Change percentage -->
        // Change = Market value * (Change percentage / (Change percentage + 1))
        Double totalProfit = totalMarketValue != null && changeQuotient != null ?
                totalMarketValue * (changeQuotient / (changeQuotient + 1)) : null;

        portfolio.setTotalProfit(totalProfit);
        portfolio.setUniqueIdentifier(getFullyFormattedNumber());

        // Add buying power if available
        if (totalEquities != null && totalEquities.getBuyingPower() != null &&
                totalEquities.getBuyingPower().getAmount() != null) {
            portfolio.setCashValue(StringUtils.parseAmount(totalEquities.getBuyingPower().getAmount()));
        }

        return portfolio;
    }
}
