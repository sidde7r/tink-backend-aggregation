package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.apache.commons.lang3.time.DateUtils;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankUtils;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class VolksbankTransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {

    private final VolksbankApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public VolksbankTransactionFetcher(
            final VolksbankApiClient apiClient, final PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        Date now = new Date();
        final Date maxDate =
                DateUtils.addDays(now, VolksbankConstants.Transaction.DEFAULT_HISTORY_DAYS);
        if (fromDate.compareTo(maxDate) < 0) {
            return PaginatorResponseImpl.createEmpty(false);
        }

        final String consentId = persistentStorage.get(Storage.CONSENT);

        final OAuth2Token oauthToken =
                persistentStorage
                        .get(Storage.OAUTH_TOKEN, OAuth2Token.class)
                        .orElseThrow(() -> new NoSuchElementException("Missing Oauth token!"));

        List<TransactionResponse> responseList = new ArrayList<>();
        TransactionResponse response =
                apiClient.readTransactionsWithDates(
                        account, fromDate, toDate, consentId, oauthToken);
        responseList.add(response);
        String link = response.getNextLink();
        while (link != null) {
            Map<String, String> urlParams = VolksbankUtils.splitURLQuery(link);
            if (VolksbankUtils.IsEntryReferenceFromAfterDate(
                    urlParams.get("entryReferenceFrom"), toDate)) {
                break;
            }
            response =
                    apiClient.readTransactionsWithLink(account, urlParams, consentId, oauthToken);
            link = response.getNextLink();
            responseList.add(response);
        }
        return new TransactionsResponse(responseList, toDate);
    }
}
