package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb;

import static org.apache.commons.lang3.CharEncoding.UTF_8;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.CredentialsKeys;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.entity.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.entity.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.configuration.BerlinGroupConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.TransactionsKeyPaginatorBaseResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class DnbApiClient extends BerlinGroupApiClient<BerlinGroupConfiguration> {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final Credentials credentials;

    public DnbApiClient(
            final TinkHttpClient client,
            final SessionStorage sessionStorage,
            final Credentials credentials) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.credentials = credentials;
    }

    @Override
    public AccountsResponse fetchAccounts() {
        return createRequest(new URL(getConfiguration().getBaseUrl() + Urls.ACCOUNTS))
                .header(HeaderKeys.CONSENT_ID, getConsentId())
                .get(AccountsResponse.class);
    }

    @Override
    public TransactionsKeyPaginatorBaseResponse fetchTransactions(final String accountId) {
        return createRequest(
                        new URL(
                                getConfiguration().getBaseUrl()
                                        + String.format(Urls.TRANSACTIONS, accountId)))
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                .header(HeaderKeys.CONSENT_ID, getConsentId())
                .get(TransactionsKeyPaginatorBaseResponse.class);
    }

    @Override
    public String getConsentId() {
        return getConsent().getConsentId();
    }

    @Override
    public OAuth2Token refreshToken(final String token) {
        throw new IllegalStateException(ErrorMessages.OAUTH_TOKEN_ERROR);
    }

    @Override
    public OAuth2Token getToken(final String code) {
        throw new IllegalStateException(ErrorMessages.OAUTH_TOKEN_ERROR);
    }

    @Override
    public URL getAuthorizeUrl(final String state) {
        sessionStorage.put(StorageKeys.STATE, state);
        final ConsentResponse consentResponse = getConsent();
        return new URL(consentResponse.getScaRedirectLink()).queryParam(QueryKeys.STATE, state);
    }

    public ConsentResponse getConsent() {
        return sessionStorage
                .get(StorageKeys.CONSENT_OBJECT, ConsentResponse.class)
                .orElseGet(
                        () -> {
                            final ConsentResponse consentResponse = fetchConsent();
                            sessionStorage.put(StorageKeys.CONSENT_OBJECT, consentResponse);
                            return consentResponse;
                        });
    }

    private ConsentResponse fetchConsent() {
        final ConsentRequest consentsRequest = new ConsentRequest(5);
        final URL url = new URL(getConfiguration().getBaseUrl() + Urls.CONSENTS);

        return createRequestWithRedirectStateAndCode(url)
                .body(consentsRequest.toData())
                .post(ConsentResponse.class);
    }

    public BalancesResponse fetchBalance(final String accountId) {
        return createRequest(
                        new URL(
                                getConfiguration().getBaseUrl()
                                        + String.format(Urls.BALANCES, accountId)))
                .header(HeaderKeys.CONSENT_ID, getConsentId())
                .get(BalancesResponse.class);
    }

    private RequestBuilder createRequestWithoutRedirectHeader(final URL url) {
        final String requestId = UUID.randomUUID().toString();

        return client.request(url)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.X_REQUEST_ID, requestId)
                .header(
                        DnbConstants.HeaderKeys.PSU_ID,
                        credentials.getField(CredentialsKeys.PSU_ID));
    }

    private RequestBuilder createRequest(final URL url) {
        return createRequestWithoutRedirectHeader(url)
                .header(HeaderKeys.TPP_REDIRECT_URI, getConfiguration().getRedirectUrl());
    }

    private RequestBuilder createRequestWithRedirectStateAndCode(final URL url) {
        final URL redirectUrl =
                new URL(getConfiguration().getRedirectUrl())
                        .queryParam(QueryKeys.STATE, sessionStorage.get(StorageKeys.STATE))
                        .queryParam(QueryKeys.CODE, "123456");

        return createRequestWithoutRedirectHeader(url)
                .header(HeaderKeys.TPP_REDIRECT_URI, decodeUrl(redirectUrl));
    }

    private String decodeUrl(final URL url) {
        try {
            return URLDecoder.decode(url.toString(), UTF_8);
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalArgumentException(ErrorMessages.URL_ENCODING_ERROR);
        }
    }
}
