package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.commons.httpclient.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.configuration.SebConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.rpc.FetchCardAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.rpc.FetchCardAccountsTransactions;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public abstract class SebBaseApiClient {

    protected final TinkHttpClient client;
    protected final PersistentStorage persistentStorage;
    protected SebConfiguration configuration;

    public SebBaseApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    public void setConfiguration(SebConfiguration configuration) {
        this.configuration = configuration;
    }

    public abstract RequestBuilder getAuthorizeUrl();

    public abstract OAuth2Token getToken(TokenRequest request);

    public SebConfiguration getConfiguration() {
        return configuration;
    }

    protected OAuth2Token getTokenFromStorage() {
        return persistentStorage
                .get(OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException("Cannot find token!"));
    }

    protected String getRequestId() {
        return UUID.randomUUID().toString();
    }

    public abstract FetchCardAccountsTransactions fetchCardTransactions(
            String accountId, LocalDate fromDate, LocalDate toDate);

    public RequestBuilder buildCardTransactionsFetch(
            URL url, LocalDate fromDate, LocalDate toDate) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(SebCommonConstants.DATE_FORMAT);

        return createRequestInSession(url)
                .queryParam(SebCommonConstants.QueryKeys.DATE_FROM, fromDate.format(formatter))
                .queryParam(SebCommonConstants.QueryKeys.DATE_TO, toDate.format(formatter))
                .queryParam(
                        SebCommonConstants.QueryKeys.BOOKING_STATUS,
                        SebCommonConstants.QueryValues.PENDING_AND_BOOKED_TRANSACTIONS);
    }

    public abstract FetchCardAccountResponse fetchCardAccounts();

    protected RequestBuilder createRequestInSession(URL url) {
        return createRequest(url)
                .header(SebCommonConstants.HeaderKeys.X_REQUEST_ID, getRequestId())
                .header(
                        SebCommonConstants.HeaderKeys.PSU_IP_ADDRESS,
                        SebCommonConstants.getPsuIpAddress())
                .addBearerToken(getTokenFromStorage());
    }

    protected RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    public OAuth2Token refreshToken(String url, RefreshRequest request) throws SessionException {
        try {
            return client.request(new URL(url))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .post(TokenResponse.class, request.toData())
                    .toTinkToken();
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_UNAUTHORIZED) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
            throw e;
        }
    }
}
