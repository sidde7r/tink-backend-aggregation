package se.tink.backend.aggregation.agents.nxgen.se.openbanking.corporate.seb;

import java.time.LocalDate;
import javax.ws.rs.core.MediaType;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.corporate.seb.fetcher.transactionalaccount.entities.TransactionDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.corporate.seb.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.corporate.seb.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.rpc.FetchCardAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.rpc.FetchCardAccountsTransactions;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SebCorporateApiClient extends SebBaseApiClient {
    private final Credentials credentials;

    public SebCorporateApiClient(
            TinkHttpClient client, PersistentStorage persistentStorage, Credentials credentials) {
        super(client, persistentStorage);
        this.credentials = credentials;
    }

    @Override
    public RequestBuilder getAuthorizeUrl() {
        return client.request(new URL(SebConstants.Urls.BASE_AUTH_URL));
    }

    @Override
    public OAuth2Token getToken(TokenRequest request) {
        return client.request(
                        new URL(SebCommonConstants.Urls.BASE_URL)
                                .concat(SebCommonConstants.Urls.TOKEN))
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class, request.toData())
                .toTinkToken();
    }

    public FetchAccountResponse fetchAccounts() {

        return createRequestInSession(
                        new URL(SebCommonConstants.Urls.BASE_URL)
                                .concat(SebConstants.Urls.ACCOUNTS))
                .queryParam(
                        SebCommonConstants.QueryKeys.WITH_BALANCE,
                        SebCommonConstants.QueryValues.WITH_BALANCE)
                .get(FetchAccountResponse.class);
    }

    public FetchTransactionsResponse fetchTransactions(
            String urlAddress, boolean appendQueryParams) {

        URL url = new URL(SebCommonConstants.Urls.BASE_URL).concat(urlAddress);

        RequestBuilder requestBuilder = createRequestInSession(url);

        if (appendQueryParams) {
            requestBuilder.queryParam(
                    SebCommonConstants.QueryKeys.BOOKING_STATUS,
                    SebCommonConstants.QueryValues.BOOKED_TRANSACTIONS);
        }

        FetchTransactionsResponse response = requestBuilder.get(FetchTransactionsResponse.class);

        return response;
    }

    public TransactionDetailsEntity fetchTransactionDetails(String urlAddress) {
        return createRequestInSession(
                        new URL(SebConstants.Urls.BASE_TRANSACTION_DETAILS).concat(urlAddress))
                .get(TransactionDetailsEntity.class);
    }

    public FetchCardAccountResponse fetchCardAccounts() {
        return createRequestInSession(
                        new URL(SebCommonConstants.Urls.BASE_URL)
                                .concat(SebConstants.Urls.CREDIT_CARD_ACCOUNTS))
                .get(FetchCardAccountResponse.class);
    }

    @Override
    public FetchCardAccountsTransactions fetchCardTransactions(
            String accountId, LocalDate fromDate, LocalDate toDate) {

        URL url =
                new URL(SebCommonConstants.Urls.BASE_URL)
                        .concat(SebConstants.Urls.CREDIT_CARD_TRANSACTIONS)
                        .parameter(SebCommonConstants.IdTags.ACCOUNT_ID, accountId);

        return buildCardTransactionsFetch(url, fromDate, toDate)
                .get(FetchCardAccountsTransactions.class);
    }

    @Override
    protected RequestBuilder createRequestInSession(URL url) {
        return createRequest(url)
                .header(SebCommonConstants.HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(
                        SebCommonConstants.HeaderKeys.PSU_IP_ADDRESS,
                        SebCommonConstants.getPsuIpAddress())
                .header(
                        SebCommonConstants.HeaderKeys.PSU_CORPORATE_ID,
                        credentials.getField(Key.CORPORATE_ID))
                .addBearerToken(getTokenFromStorage());
    }
}
