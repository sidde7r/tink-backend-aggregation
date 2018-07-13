package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.loan;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.NordeaV20ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.parsers.NordeaV20Parser;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.NordeaV20Constants.ProductType;

public class NordeaV20LoanFetcher implements AccountFetcher<LoanAccount> {
    private final NordeaV20ApiClient client;
    private final NordeaV20Parser parser;

    public NordeaV20LoanFetcher(NordeaV20ApiClient client, NordeaV20Parser parser) {
        this.client = client;
        this.parser = parser;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        Collection<LoanAccount> loans = Lists.newArrayList();

        loans.addAll(client.getAccountProductsOfTypes(ProductType.LOAN).stream()
                .map(pe -> {
                    // Fetch transactions for blanco loans for logging purposes
                    client.fetchTransactions(pe.getNordeaAccountIdV2(), null);
                    return parser.parseBlancoLoan(pe);
                }).collect(Collectors.toList()));

        // Fetch mortgages
        loans.addAll(client.getAccountProductsOfTypes(ProductType.MORTGAGE).stream()
                .map(pe -> parser.parseMortgage(pe, client.fetchLoanDetails(pe.getNordeaAccountIdV2())))
                .collect(Collectors.toList()));

        return loans;
    }
}
