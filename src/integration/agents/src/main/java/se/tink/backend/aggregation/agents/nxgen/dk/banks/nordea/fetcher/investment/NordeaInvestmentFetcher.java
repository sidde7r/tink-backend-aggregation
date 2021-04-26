package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.investment;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.investment.entities.CustodyAccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

public class NordeaInvestmentFetcher implements AccountFetcher<InvestmentAccount> {

    private final NordeaDkApiClient bankClient;

    public NordeaInvestmentFetcher(NordeaDkApiClient bankClient) {
        this.bankClient = Objects.requireNonNull(bankClient);
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        return bankClient.fetchInvestments().getAccounts().stream()
                .map(CustodyAccountEntity::toTinkAccount)
                .collect(Collectors.toList());
    }
}
