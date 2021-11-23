package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AbstractAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class PortfolioHoldingsResponse {
    private List<FundAccountEntity> fundAccounts;
    private List<EndowmentInsuranceEntity> endowmentInsurances;
    private List<EquityTraderEntity> equityTraders;
    private List<SavingsAccountEntity> savingsAccounts;
    private List<InvestmentSavingsAccountEntity> investmentSavings;
    private String serverTime;
    private AmountEntity totalValue;

    public boolean hasInvestments() {
        return (isNotNullNorEmpty(endowmentInsurances))
                || (isNotNullNorEmpty(equityTraders))
                || (isNotNullNorEmpty(fundAccounts))
                || (isNotNullNorEmpty(investmentSavings));
    }

    private boolean isNotNullNorEmpty(
            List<? extends AbstractInvestmentAccountEntity> holdingsList) {
        return holdingsList != null && !holdingsList.isEmpty();
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
                .flatMap(Collection::stream)
                .map(AbstractAccountEntity::getFullyFormattedNumber)
                .collect(Collectors.toList());
    }
}
