package se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.seb;

import java.time.LocalDate;
import javax.ws.rs.core.MediaType;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.seb.fetcher.transactionalaccount.entities.TransactionDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.seb.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.seb.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.AuthorizeResponse;
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
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SebCorporateApiClient extends SebBaseApiClient {
    private final Credentials credentials;

    public SebCorporateApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            CredentialsRequest credentialsRequest) {
        super(client, persistentStorage, credentialsRequest.isManual());
        this.credentials = credentialsRequest.getCredentials();
    }

    @Override
    public RequestBuilder getAuthorizeUrl() {
        return client.request(new URL(Urls.OAUTH));
    }

    @Override
    public AuthorizeResponse getAuthorization(String clientId, String redirectUri) {
        return client.request(getAuthorizeUrl().getUrl())
                .accept(MediaType.APPLICATION_JSON)
                .queryParam(QueryKeys.CLIENT_ID, clientId)
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.RESPONSE_TYPE_TOKEN)
                .queryParam(QueryKeys.SCOPE, QueryValues.SCOPE)
                .queryParam(QueryKeys.REDIRECT_URI, redirectUri)
                .get(AuthorizeResponse.class);
    }

    public AuthorizeResponse postAuthorization(final String requestForm) {
        return client.request(Urls.OAUTH)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .body(requestForm)
                .post(AuthorizeResponse.class);
    }

    @Override
    public OAuth2Token getToken(TokenRequest request) {
        return client.request(new URL(Urls.TOKEN))
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class, request.toData())
                .toTinkToken();
    }

    public FetchAccountResponse fetchAccounts() {
        return createRequestInSession(new URL(Urls.BASE_URL).concat(SebConstants.Urls.ACCOUNTS))
                .queryParam(QueryKeys.WITH_BALANCE, QueryValues.WITH_BALANCE)
                .get(FetchAccountResponse.class);
    }

    public FetchTransactionsResponse fetchTransactions(
            String urlAddress, boolean appendQueryParams) {

        URL url = new URL(Urls.BASE_URL).concat(urlAddress);

        RequestBuilder requestBuilder = createRequestInSession(url);

        if (appendQueryParams) {
            requestBuilder.queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOOKED_TRANSACTIONS);
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
                .header(HeaderKeys.PSU_CORPORATE_ID, credentials.getField("psu-corporate-id"))
                .addBearerToken(getTokenFromStorage());
    }
}
