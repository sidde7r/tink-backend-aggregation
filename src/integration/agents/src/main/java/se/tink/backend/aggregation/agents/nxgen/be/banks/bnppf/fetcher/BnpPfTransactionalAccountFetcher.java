package se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.fetcher;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.BnpPfApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.entities.PfmAccount;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class BnpPfTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>, TransactionFetcher<TransactionalAccount> {

    private final BnpPfApiClient apiClient;

    public BnpPfTransactionalAccountFetcher(BnpPfApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchPfmPreferences().getValue().getPfmPreference().getPfmAccounts()
                .stream()
                .filter(PfmAccount::getPfmOptInFlag)
                .map(PfmAccount::toTransactionalAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        String externalAccountId = account.getApiIdentifier();

        Optional<PfmAccount> pfmAccount =
                apiClient
                        .fetchPfmPreferences()
                        .getValue()
                        .getPfmPreference()
                        .getPfmAccountFor(externalAccountId);

        if (!pfmAccount.isPresent()) {
            return Collections.emptyList();
        }

        String accountId = pfmAccount.get().getIban();
        List<AggregationTransaction> transactionsAll = new ArrayList<>();
        String key = null;

        do {
            FetchTransactionsResponse response = apiClient.fetchTransactionsFor(accountId, key);

            List<AggregationTransaction> transactionsPart =
                    response != null && response.getData() != null
                            ? response.getData().getTinkTransactions(externalAccountId)
                            : Collections.emptyList();

            transactionsAll.addAll(transactionsPart);

            key =
                    response != null && response.getLinks() != null
                            ? response.getLinks().getNext()
                            : null;
        } while (!Strings.isNullOrEmpty(key));

        return transactionsAll;
    }
}
