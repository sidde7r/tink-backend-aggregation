package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.loan;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.NordeaV21ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.parsers.NordeaV21Parser;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.NordeaV21Constants.ProductType;

public class NordeaV21LoanFetcher implements AccountFetcher<LoanAccount> {
    private final NordeaV21ApiClient client;
    private final NordeaV21Parser parser;

    public NordeaV21LoanFetcher(NordeaV21ApiClient client, NordeaV21Parser parser) {
        this.client = client;
        this.parser = parser;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        return client.getAccountProductsOfTypes(ProductType.LOAN).stream()
                .map(pe -> parser.parseLoanAccount(pe, client.fetchLoanDetails(pe.getNordeaAccountIdV2())))
                .collect(Collectors.toList());
    }
}
