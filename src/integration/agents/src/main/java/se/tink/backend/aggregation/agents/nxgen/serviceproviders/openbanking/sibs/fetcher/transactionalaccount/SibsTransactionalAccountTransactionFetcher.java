package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.fetcher.transactionalaccount;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsUserState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.fetcher.SibsBaseTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SibsTransactionalAccountTransactionFetcher extends SibsBaseTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {

    public SibsTransactionalAccountTransactionFetcher(
            SibsBaseApiClient apiClient,
            CredentialsRequest credentialsRequest,
            SibsUserState userState) {
        super(apiClient, credentialsRequest, userState);
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        if (StringUtils.isNotEmpty(key)) {
            key = key.replace(StringUtils.SPACE, ENCODED_SPACE);
        }
        return Optional.ofNullable(key)
                .map(apiClient::getTransactionsForKey)
                .orElseGet(
                        () ->
                                apiClient.getAccountTransactions(
                                        account, getTransactionsFetchBeginDate(account)));
    }
}
