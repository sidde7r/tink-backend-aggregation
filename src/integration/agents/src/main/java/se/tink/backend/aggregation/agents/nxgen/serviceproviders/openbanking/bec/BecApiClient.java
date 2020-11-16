package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.ApiService;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.HeadersToSign;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.configuration.BecConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.fetcher.transactionalaccount.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.QsealcSignerImpl;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RequiredArgsConstructor
public final class BecApiClient {
    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final BecApiConfiguration apiConfiguration;
    private String state;
    private BecConfiguration becConfiguration;
    private String redirectUrl;
    private AgentsServiceConfiguration config;
    private EidasIdentity eidasIdentity;

    public void setConfiguration(
            AgentConfiguration<BecConfiguration> agentConfiguration,
            final AgentsServiceConfiguration configuration,
            EidasIdentity eidasIdentity) {
        this.becConfiguration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        this.config = configuration;
        this.eidasIdentity = eidasIdentity;
    }

    private Map<String, Object> getHeaders(String requestId, String digest) {
        String tppRedirectUrl = new URL(redirectUrl).queryParam(QueryKeys.STATE, state).toString();
        Map<String, Object> headers = new HashMap<>();
        headers.put(HeaderKeys.ACCEPT, MediaType.APPLICATION_JSON);
        headers.put(HeaderKeys.CONSENT_ID, persistentStorage.get(StorageKeys.CONSENT_ID));
        headers.put(HeaderKeys.X_REQUEST_ID, requestId);
        headers.put(HeaderKeys.TPP_REDIRECT_URI, tppRedirectUrl);
        headers.put(HeaderKeys.TPP_NOK_REDIRECT_URI, tppRedirectUrl);
        headers.put(HeaderKeys.DIGEST, digest);
        headers.put(HeaderKeys.TPP_SIGNATURE_CERTIFICATE, becConfiguration.getQsealCertificate());
        if (apiConfiguration.isManual()) {
            headers.put(HeaderKeys.PSU_IP, apiConfiguration.getUserIp());
        }
        return headers;
    }

    private Map<String, Object> getPisHeaders(String requestId, String digest) {
        String tppRedirectUrl = new URL(redirectUrl).queryParam(QueryKeys.STATE, state).toString();

        Map<String, Object> headers = new HashMap<>();
        headers.put(HeaderKeys.ACCEPT, MediaType.APPLICATION_JSON);
        headers.put(HeaderKeys.X_REQUEST_ID, requestId);
        headers.put(HeaderKeys.TPP_REDIRECT_URI, tppRedirectUrl);
        headers.put(HeaderKeys.TPP_NOK_REDIRECT_URI, tppRedirectUrl);
        headers.put(HeaderKeys.DIGEST, digest);
        headers.put(HeaderKeys.TPP_SIGNATURE_CERTIFICATE, becConfiguration.getQsealCertificate());

        return headers;
    }

    public RequestBuilder createRequest(URL url, String requestBody) {
        String requestId = UUID.randomUUID().toString();
        String digest = createDigest(requestBody);
        Map<String, Object> headers = getHeaders(requestId, digest);

        return client.request(url)
                .type(MediaType.APPLICATION_JSON)
                .headers(headers)
                .header(HeaderKeys.SIGNATURE, generateSignatureHeader(headers));
    }

    public RequestBuilder createPisRequest(URL url, String requestBody) {
        String requestId = UUID.randomUUID().toString();
        String digest = createDigest(requestBody);
        Map<String, Object> headers = getPisHeaders(requestId, digest);

        return client.request(url)
                .type(MediaType.APPLICATION_JSON)
                .headers(headers)
                .header(HeaderKeys.SIGNATURE, generateSignatureHeader(headers));
    }

    public RequestBuilder createRequest(URL url) {
        return createRequest(url, FormValues.EMPTY_STRING);
    }

    public RequestBuilder createPisRequest(URL url) {
        return createPisRequest(url, FormValues.EMPTY_STRING);
    }

    public ConsentResponse getConsent(String state) throws HttpResponseException {
        this.state = state;
        ConsentRequest body = createConsentRequestBody();

        ConsentResponse response =
                createRequest(
                                new URL(apiConfiguration.getUrl().concat(ApiService.GET_CONSENT)),
                                SerializationUtils.serializeToString(body))
                        .body(body)
                        .post(ConsentResponse.class);
        persistentStorage.put(StorageKeys.CONSENT_ID, response.getConsentId());
        return response;
    }

    public ConsentResponse getConsentStatus() {
        return createRequest(
                        new URL(apiConfiguration.getUrl().concat(ApiService.GET_CONSENT_STATUS))
                                .parameter(
                                        StorageKeys.CONSENT_ID,
                                        persistentStorage.get(StorageKeys.CONSENT_ID)))
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .get(ConsentResponse.class);
    }

    public ConsentRequest createConsentRequestBody() {
        return new ConsentRequest(
                new AccessEntity(FormValues.ACCESS_TYPE),
                FormValues.FALSE,
                LocalDate.now().plusDays(FormValues.NUMBER_OF_VALID_DAYS).toString(),
                FormValues.TRUE,
                FormValues.FREQUENCY_PER_DAY);
    }

    public GetAccountsResponse getAccounts() {
        return createRequest(new URL(apiConfiguration.getUrl().concat(ApiService.GET_ACCOUNTS)))
                .get(GetAccountsResponse.class);
    }

    public BalancesResponse getBalances(AccountEntity account) {
        return createRequest(
                        new URL(apiConfiguration.getUrl().concat(ApiService.GET_BALANCES))
                                .parameter(IdTags.ACCOUNT_ID, account.getResourceId()))
                .get(BalancesResponse.class);
    }

    public PaginatorResponse getTransactions(
            TransactionalAccount account, Date fromDate, Date toDate) {
        final URL url =
                new URL(apiConfiguration.getUrl().concat(ApiService.GET_TRANSACTIONS))
                        .parameter(IdTags.ACCOUNT_ID, account.getApiIdentifier());

        return createRequest(url)
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                .queryParam(
                        QueryKeys.DATE_FROM, ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(QueryKeys.DATE_TO, ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .get(GetTransactionsResponse.class);
    }

    private String generateSignatureHeader(Map<String, Object> headers) {
        QsealcSigner signer =
                QsealcSignerImpl.build(
                        config.getEidasProxy().toInternalConfig(),
                        QsealcAlg.EIDAS_RSA_SHA256,
                        eidasIdentity);

        String signedHeaders =
                Arrays.stream(HeadersToSign.values())
                        .map(HeadersToSign::getHeader)
                        .filter(headers::containsKey)
                        .collect(Collectors.joining(" "));

        String signedHeadersWithValues =
                Arrays.stream(HeadersToSign.values())
                        .map(HeadersToSign::getHeader)
                        .filter(headers::containsKey)
                        .map(header -> String.format("%s: %s", header, headers.get(header)))
                        .collect(Collectors.joining("\n"));

        String signature = signer.getSignatureBase64(signedHeadersWithValues.getBytes());

        return String.format(
                BecConstants.HeaderValues.SIGNATURE_HEADER,
                becConfiguration.getKeyId(),
                signedHeaders,
                signature);
    }

    private String createDigest(String body) {
        return String.format(
                HeaderValues.SHA_256.concat("%s"),
                Base64.getEncoder().encodeToString(Hash.sha256(body)));
    }
}
