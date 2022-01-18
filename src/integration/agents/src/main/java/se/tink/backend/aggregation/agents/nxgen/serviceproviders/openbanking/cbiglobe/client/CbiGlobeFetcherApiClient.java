package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client;

import java.time.LocalDate;
import java.util.OptionalInt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.RequestContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiUrlProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.InstrumentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
@Slf4j
public class CbiGlobeFetcherApiClient {

    private final CbiGlobeRequestBuilder cbiRequestBuilder;
    protected final CbiUrlProvider urlProvider;
    private final CbiStorage storage;

    protected RequestBuilder createFetchingRequest(URL url) {
        return cbiRequestBuilder.addPsuIpAddressHeader(
                cbiRequestBuilder
                        .createRequestInSession(url)
                        .header(HeaderKeys.CONSENT_ID, storage.getConsentId()));
    }

    // Currently, CBI is supporting either accounts or card accounts, in a weird way, of setting up
    // the api client in specific way. This isn't really utilized as of yet.
    protected InstrumentType getSupportedInstrumentType() {
        return InstrumentType.ACCOUNTS;
    }

    public AccountsResponse getAccounts() {
        return cbiRequestBuilder.makeRequest(
                createFetchingRequest(getAccountsUrl()),
                HttpMethod.GET,
                AccountsResponse.class,
                RequestContext.ACCOUNTS_GET,
                null);
    }

    private URL getAccountsUrl() {
        return InstrumentType.ACCOUNTS == getSupportedInstrumentType()
                ? urlProvider.getAccountsUrl()
                : urlProvider.getCardAccountsUrl();
    }

    public BalancesResponse getBalances(String resourceId) {
        return cbiRequestBuilder.makeRequest(
                createFetchingRequest(
                        getBalancesUrl().parameterNoEncoding(IdTags.ACCOUNT_ID, resourceId)),
                HttpMethod.GET,
                BalancesResponse.class,
                RequestContext.BALANCES_GET,
                null);
    }

    private URL getBalancesUrl() {
        return InstrumentType.ACCOUNTS == getSupportedInstrumentType()
                ? urlProvider.getBalancesUrl()
                : urlProvider.getCardBalancesUrl();
    }

    public TransactionsResponse getTransactions(
            String accountApiIdentifier,
            LocalDate fromDate,
            LocalDate toDate,
            String bookingType,
            int page) {
        HttpResponse response =
                cbiRequestBuilder.makeRequest(
                        createFetchingRequest(
                                        getTransactionsUrl()
                                                .parameterNoEncoding(
                                                        IdTags.ACCOUNT_ID, accountApiIdentifier))
                                .queryParam(QueryKeys.BOOKING_STATUS, bookingType)
                                .queryParam(QueryKeys.DATE_FROM, fromDate.toString())
                                .queryParam(QueryKeys.DATE_TO, toDate.toString())
                                .queryParam(QueryKeys.OFFSET, String.valueOf(page)),
                        HttpMethod.GET,
                        HttpResponse.class,
                        RequestContext.TRANSACTIONS_GET,
                        null);

        // CBI will only tell us how many pages are there on the very first page.
        // On later calls, we need to read that from storage to know when to stop
        TransactionsResponse transactionsResponse = response.getBody(TransactionsResponse.class);
        transactionsResponse.setPageRemaining(getTotalPages(response, accountApiIdentifier) > page);

        return transactionsResponse;
    }

    private URL getTransactionsUrl() {
        return InstrumentType.ACCOUNTS == getSupportedInstrumentType()
                ? urlProvider.getTransactionsUrl()
                : urlProvider.getCardTransactionsUrl();
    }

    private int getTotalPages(HttpResponse response, String accountApiIdentifier) {
        OptionalInt totalPagesFromStorage =
                storage.getNumberOfPagesForAccount(accountApiIdentifier);
        if (totalPagesFromStorage.isPresent()) {
            return totalPagesFromStorage.getAsInt();
        } else {
            String rawTotalPagesFromHeader = response.getHeaders().getFirst(QueryKeys.TOTAL_PAGES);

            int totalPagesFromHeader =
                    rawTotalPagesFromHeader == null ? 1 : Integer.parseInt(rawTotalPagesFromHeader);
            storage.saveNumberOfPagesForAccount(accountApiIdentifier, totalPagesFromHeader);

            return totalPagesFromHeader;
        }
    }
}
