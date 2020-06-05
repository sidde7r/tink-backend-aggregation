package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.fetcher.transactionalaccounts;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.fetcher.transactionalaccounts.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.fetcher.transactionalaccounts.entities.OperationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.rpc.DefaultResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.rpc.ErrorEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class CreditAgricoleTransactionalAccountsFetcher
        implements AccountFetcher<TransactionalAccount>, TransactionFetcher<TransactionalAccount> {

    private final CreditAgricoleApiClient apiClient;

    public CreditAgricoleTransactionalAccountsFetcher(CreditAgricoleApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public List<TransactionalAccount> fetchAccounts() {
        return checkResponseErrors(apiClient.contracts()).getAccounts().stream()
                .filter(AccountEntity::isKnownAccountType)
                .map(AccountEntity::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        return checkResponseErrors(apiClient.operations(account.getAccountNumber())).getOperations()
                .stream()
                .map(OperationEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    private <T extends DefaultResponse> T checkResponseErrors(T response) {
        if (response.getErrors().size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (ErrorEntity e : response.getErrors()) {
                sb.append(e.toString());
            }
            throw new RuntimeException(sb.toString());
        }
        return response;
    }
}
