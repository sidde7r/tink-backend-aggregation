package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.utils.VolksbankUtils;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class VolksbankTransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {

    private final VolksbankApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final LocalDateTimeSource localDateTimeSource;

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {

        if (isReachMaximumDate(fromDate)) {
            return PaginatorResponseImpl.createEmpty(false);
        }

        return getTransactionsResponseWithLink(account, fromDate, toDate);
    }

    private TransactionsResponse getTransactionsResponseWithLink(
            TransactionalAccount account, Date fromDate, Date toDate) {

        final OAuth2Token oauth2Token = VolksbankUtils.getOAuth2TokenFromStorage(persistentStorage);
        final String consentId = persistentStorage.get(Storage.CONSENT);

        List<TransactionResponse> responseList = new ArrayList<>();
        TransactionResponse response =
                apiClient.readTransactionsWithDates(
                        account, fromDate, toDate, consentId, oauth2Token);

        responseList.add(response);
        String link = response.getNextLink();

        while (Optional.ofNullable(link).isPresent()) {
            Map<String, String> urlParams = VolksbankUtils.convertURLQueryToMap(link);
            if (VolksbankUtils.isEntryReferenceFromAfterDate(
                    urlParams.get("entryReferenceFrom"), toDate)) {
                break;
            }
            response =
                    apiClient.readTransactionsWithLink(account, urlParams, consentId, oauth2Token);
            link = response.getNextLink();
            responseList.add(response);
        }

        return new TransactionsResponse(responseList, toDate);
    }

    private boolean isReachMaximumDate(Date fromDate) {
        final Date maxDate =
                DateUtils.addDays(
                        Date.from(localDateTimeSource.getInstant()),
                        VolksbankConstants.Transaction.DEFAULT_HISTORY_DAYS);
        return fromDate.compareTo(maxDate) < 0;
    }
}
