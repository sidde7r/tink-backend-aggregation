package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator.rpc.RedirectEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.configuration.SparebankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.utils.SparebankUtils;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SparebankApiClient {

    protected final TinkHttpClient client;
    protected final SessionStorage sessionStorage;
    protected SparebankConfiguration configuration;

    public SparebankApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public URL getAuthorizeUrl(String state) {
        final URL redirectUrl =
                new URL(getConfiguration().getRedirectUrl()).queryParam("state", state);
        final String baseUrl = getConfiguration().getBaseUrl();
        RedirectEntity redirectEntity = null;
        try {
            String response =
                    createRequest(new URL(baseUrl + Urls.CONSENTS), null, redirectUrl.toString())
                            .post(String.class);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == 401) {
                redirectEntity = e.getResponse().getBody(RedirectEntity.class);
                if (redirectEntity.getLinks() == null
                        || redirectEntity.getLinks().getScaRedirect() == null
                        || redirectEntity.getLinks().getScaRedirect().getHref() == null) {
                    throw e;
                }
            } else {
                throw e;
            }
        }

        return new URL(redirectEntity.getLinks().getScaRedirect().getHref());
    }

    public AccountResponse fetchAccounts() {
        final String baseUrl = getConfiguration().getBaseUrl();
        return createRequest(
                        new URL(baseUrl + SparebankConstants.Urls.FETCH_ACCOUNTS),
                        getPsuId(),
                        getConfiguration().getRedirectUrl())
                .queryParam(QueryKeys.WITH_BALANCE, QueryValues.TRUE)
                .header(HeaderKeys.TPP_SESSION_ID, getTppSessionId())
                .header(HeaderKeys.PSU_ID, getPsuId())
                .get(AccountResponse.class);
    }

    public TransactionResponse fetchTransactions(String resourceId, String offset, Integer limit) {
        final String baseUrl = getConfiguration().getBaseUrl();
        return createRequest(
                        new URL(String.format(baseUrl + Urls.FETCH_TRANSACTIONS, resourceId)),
                        getPsuId(),
                        getConfiguration().getRedirectUrl())
                .queryParam(SparebankConstants.QueryKeys.LIMIT, Integer.toString(limit))
                .queryParam(SparebankConstants.QueryKeys.OFFSET, offset)
                .queryParam(
                        SparebankConstants.QueryKeys.BOOKING_STATUS,
                        SparebankConstants.QueryValues.BOOKING_STATUS)
                .header(HeaderKeys.TPP_SESSION_ID, getTppSessionId())
                .header(HeaderKeys.PSU_ID, getPsuId())
                .get(TransactionResponse.class);
    }

    protected RequestBuilder createRequest(URL url, String psuId, String redirectUri) {
        final String xRequestId = getXRequestId().toString();
        final String certificatePath = getConfiguration().getClientSigningCertificatePath();
        final String date = ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME);
        final String keyId = getConfiguration().getKeyId();
        final String keyPath = getConfiguration().getClientSigningKeyPath();
        final String tppId = getConfiguration().getTppId();
        final String psuIpAddress = getConfiguration().getPsuIpAdress();

        return client.request(url)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.DATE, date)
                .header(HeaderKeys.TPP_ID, tppId)
                .header(HeaderKeys.X_REQUEST_ID, xRequestId)
                .header(HeaderKeys.TPP_REDIRECT_URI, redirectUri)
                .header(
                        HeaderKeys.TPP_SIGNATURE_CERTIFICATE,
                        SparebankUtils.getCertificateEncoded(certificatePath))
                .header(
                        HeaderKeys.SIGNATURE,
                        SparebankUtils.getSignature(
                                xRequestId, date, psuId, redirectUri, keyId, keyPath))
                .header(HeaderKeys.PSU_IP_ADDRESS, psuIpAddress);
    }

    protected SparebankConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(SparebankConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setUpTppSessionIdAndPsuId(String tppSessionId, String psuId) {
        sessionStorage.put(StorageKeys.TPP_SESSION_ID, tppSessionId);
        sessionStorage.put(StorageKeys.PSU_ID, psuId);
    }

    protected UUID getXRequestId() {
        return UUID.randomUUID();
    }

    protected String getPsuId() {
        return sessionStorage.get(StorageKeys.PSU_ID);
    }

    protected String getTppSessionId() {
        return sessionStorage.get(StorageKeys.TPP_SESSION_ID);
    }
}
