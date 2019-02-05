package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.investment.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.investment.entities.CustodyAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.investment.entities.HoldingEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.investment.entities.PoolAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.libraries.amount.Amount;

@JsonObject
public class FetchInvestmentResponse {

    private List<CustodyAccountEntity> custodyAccounts;
    private List<PoolAccountEntity> poolAccounts;
    private HoldingEntity holding;

    public Collection<InvestmentAccount> toInvestmentAccounts() {
        return Stream.concat(getCustodyInvestments(), getPoolInvestments())
                .collect(Collectors.toList());
    }

    // Funds
    private Stream<InvestmentAccount> getCustodyInvestments() {
        Optional<HoldingEntity> holdingOption = Optional.ofNullable(this.holding);
        boolean isDKKCurrency = holdingOption.filter(HoldingEntity::isDKKCurrency).isPresent();
        if (this.custodyAccounts == null || !isDKKCurrency) {
            return Stream.empty();
        }
        return this.custodyAccounts.stream()
                .map(custodyAccount -> InvestmentAccount
                        .builder(custodyAccount.createUniqueIdentifier())
                        .setAccountNumber(custodyAccount.createUniqueIdentifier())
                        .setName(custodyAccount.getName())
                        .setCashBalance(Amount.inDKK(0))
                        .setPortfolios(
                                holdingOption
                                        .map(holding -> holding.toPortfolio(custodyAccount))
                                        .map(Collections::singletonList)
                                        .orElseGet(Collections::emptyList)
                        )
                        .build());
    }

    private Stream<InvestmentAccount> getPoolInvestments() {
        if (this.poolAccounts == null) {
            return Stream.empty();
        }
        return this.poolAccounts.stream()
                .map(PoolAccountEntity::toInvestmentAccount)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }
}
