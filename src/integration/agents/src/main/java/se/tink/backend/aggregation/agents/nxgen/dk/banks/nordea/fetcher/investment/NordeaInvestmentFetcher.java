package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.investment;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.investment.entities.CustodyAccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
public class NordeaInvestmentFetcher implements AccountFetcher<InvestmentAccount> {

    private final NordeaDkApiClient bankClient;

    public NordeaInvestmentFetcher(NordeaDkApiClient bankClient) {
        this.bankClient = Objects.requireNonNull(bankClient);
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        try {
            return bankClient.fetchInvestments().getAccounts().stream()
                    .map(CustodyAccountEntity::toTinkAccount)
                    .collect(Collectors.toList());
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == 500) {
                log.error("[NordeaInvestmentFetcher] Issue with fetching investments", e);
                return Collections.emptyList();
            }
            throw e;
        }
    }
}
