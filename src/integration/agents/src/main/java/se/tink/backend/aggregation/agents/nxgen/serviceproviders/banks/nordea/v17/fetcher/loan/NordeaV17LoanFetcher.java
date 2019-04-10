package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.loan;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.NordeaV17ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.parsers.NordeaV17Parser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.NordeaV20Constants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

public class NordeaV17LoanFetcher implements AccountFetcher<LoanAccount> {
    private final NordeaV17ApiClient client;
    private final NordeaV17Parser parser;

    public NordeaV17LoanFetcher(NordeaV17ApiClient client, NordeaV17Parser parser) {
        this.client = client;
        this.parser = parser;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        return client.getAccountProductsOfTypes(NordeaV20Constants.ProductType.LOAN).stream()
                .map(
                        pe ->
                                parser.parseLoanAccount(
                                        pe,
                                        client.fetchLoanDetails(pe.getNordeaAccountIdV2())
                                                .getLoanDetails()))
                .collect(Collectors.toList());
    }
}
