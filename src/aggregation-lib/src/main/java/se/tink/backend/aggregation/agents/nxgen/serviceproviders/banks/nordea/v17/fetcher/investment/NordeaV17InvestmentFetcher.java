package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.investment;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.NordeaV17ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.parsers.NordeaV17Parser;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;

public class NordeaV17InvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private final NordeaV17ApiClient client;
    private final NordeaV17Parser parser;

    public NordeaV17InvestmentFetcher(NordeaV17ApiClient client, NordeaV17Parser parser) {
        this.client = client;
        this.parser = parser;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        return client.fetchCustodyAccounts().stream()
                .filter(Objects::nonNull)
                .map(parser::parseInvestmentAccount)
                .collect(Collectors.toList());
    }
}
