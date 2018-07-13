package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.investment;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.NordeaV20ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.parsers.NordeaV20Parser;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;

public class NordeaV20InvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private final NordeaV20ApiClient client;
    private final NordeaV20Parser parser;

    public NordeaV20InvestmentFetcher(NordeaV20ApiClient client, NordeaV20Parser parser) {
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
