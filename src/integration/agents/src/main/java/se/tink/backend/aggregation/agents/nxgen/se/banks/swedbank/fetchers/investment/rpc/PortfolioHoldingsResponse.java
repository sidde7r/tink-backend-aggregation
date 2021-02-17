package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AbstractAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PortfolioHoldingsResponse {
    private List<FundAccountEntity> fundAccounts;
    private List<EndowmentInsuranceEntity> endowmentInsurances;
    private List<EquityTraderEntity> equityTraders;
    private List<SavingsAccountEntity> savingsAccounts;
    private List<InvestmentSavingsAccountEntity> investmentSavings;
    private String serverTime;
    private AmountEntity totalValue;

    public List<FundAccountEntity> getFundAccounts() {
        return fundAccounts;
    }

    public List<EndowmentInsuranceEntity> getEndowmentInsurances() {
        return endowmentInsurances;
    }

    public List<EquityTraderEntity> getEquityTraders() {
        return equityTraders;
    }

    public List<SavingsAccountEntity> getSavingsAccounts() {
        return savingsAccounts;
    }

    public List<InvestmentSavingsAccountEntity> getInvestmentSavings() {
        return investmentSavings;
    }

    public String getServerTime() {
        return serverTime;
    }

    public AmountEntity getTotalValue() {
        return totalValue;
    }

    public boolean hasInvestments() {
        return (endowmentInsurances != null && !endowmentInsurances.isEmpty())
                || (equityTraders != null && !equityTraders.isEmpty())
                || (fundAccounts != null && !fundAccounts.isEmpty())
                || (investmentSavings != null && !investmentSavings.isEmpty());
    }

    @JsonIgnore
    // extract all account numbers for the investment accounts,
    // i.e. not savings accounts.
    // These accounts will be fetched by the investment fetcher,
    // all other can be handled by transactional account fetcher
    public List<String> getInvestmentAccountNumbers() {
        if (!hasInvestments()) {
            return Collections.emptyList();
        }

        return ImmutableList.of(
                        Optional.ofNullable(fundAccounts),
                        Optional.ofNullable(endowmentInsurances),
                        Optional.ofNullable(equityTraders),
                        Optional.ofNullable(investmentSavings))
                .stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(accounts -> accounts.stream())
                .map(AbstractAccountEntity::getFullyFormattedNumber)
                .collect(Collectors.toList());
    }
}
