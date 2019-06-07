package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAbstractApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.rpc.FetchCardAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.rpc.FetchCardAccountsTransactions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SebAccountsAndCardsApiClient extends SebAbstractApiClient {

    public SebAccountsAndCardsApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        super(client, sessionStorage);
    }

    public FetchAccountResponse fetchAccounts() {

        URL url = new URL(configuration.getBaseUrl() + SebAccountsAndCardsConstants.Urls.ACCOUNTS);

        FetchAccountResponse response =
                client.request(url)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(SebCommonConstants.HeaderKeys.X_REQUEST_ID, getRequestId())
                        .addBearerToken(getTokenFromSession())
                        .queryParam(
                                SebCommonConstants.QueryKeys.WITH_BALANCE,
                                SebCommonConstants.QueryValues.WITH_BALANCE)
                        .get(FetchAccountResponse.class);

        return response;
    }

    public FetchTransactionsResponse fetchTransactions(
            String urlAddress, boolean appendQueryParams) {

        URL url = new URL(configuration.getBaseUrl() + urlAddress);

        RequestBuilder requestBuilder =
                client.request(url)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(SebCommonConstants.HeaderKeys.X_REQUEST_ID, getRequestId())
                        .addBearerToken(getTokenFromSession());

        if (appendQueryParams) {
            requestBuilder.queryParam(
                    SebCommonConstants.QueryKeys.BOOKING_STATUS,
                    SebCommonConstants.QueryValues.BOOKED_TRANSACTIONS);
        }

        FetchTransactionsResponse response = requestBuilder.get(FetchTransactionsResponse.class);

        return response;
    }

    public FetchTransactionsResponse fetchTransactions(TransactionalAccount account, String key) {

        URL url =
                Optional.ofNullable(key)
                        .map(
                                k ->
                                        new URL(
                                                SebAccountsAndCardsConstants.Urls
                                                                .TRANSACTIONS_NEXT_PAGE_URL_PREFIX
                                                        + k))
                        .orElse(
                                new URL(SebAccountsAndCardsConstants.Urls.TRANSACTIONS)
                                        .parameter(
                                                SebCommonConstants.IdTags.ACCOUNT_ID,
                                                account.getApiIdentifier()));

        return fetchTransactions(url.toString(), key == null);
    }

    public FetchTransactionsResponse fetchTransactions(TransactionalAccount account) {

        String url =
                new URL(SebAccountsAndCardsConstants.Urls.TRANSACTIONS)
                        .parameter(SebCommonConstants.IdTags.ACCOUNT_ID, account.getApiIdentifier())
                        .toString();

        return fetchTransactions(url, true);
    }

    public Collection<CreditCardAccount> fetchCardAccounts() {
        URL url =
                new URL(
                        configuration.getBaseUrl()
                                + SebAccountsAndCardsConstants.Urls.CREDIT_CARD_ACCOUNTS);

        FetchCardAccountResponse response =
                client.request(url)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(SebCommonConstants.HeaderKeys.X_REQUEST_ID, getRequestId())
                        .addBearerToken(getTokenFromSession())
                        .get(FetchCardAccountResponse.class);

        return response.getTransactions();
    }

    public FetchCardAccountsTransactions fetchCardTransactions(
            String accountId, LocalDate fromDate, LocalDate toDate) {

        URL url =
                new URL(
                                configuration.getBaseUrl()
                                        + SebAccountsAndCardsConstants.Urls
                                                .CREDIT_CARD_TRANSACTIONS)
                        .parameter(SebCommonConstants.IdTags.ACCOUNT_ID, accountId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(SebCommonConstants.DATE_FORMAT);

        FetchCardAccountsTransactions response =
                client.request(url)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(SebCommonConstants.HeaderKeys.X_REQUEST_ID, getRequestId())
                        .addBearerToken(getTokenFromSession())
                        .queryParam(
                                SebCommonConstants.QueryKeys.DATE_FROM, fromDate.format(formatter))
                        .queryParam(SebCommonConstants.QueryKeys.DATE_TO, toDate.format(formatter))
                        .get(FetchCardAccountsTransactions.class);

        return response;
    }
}
