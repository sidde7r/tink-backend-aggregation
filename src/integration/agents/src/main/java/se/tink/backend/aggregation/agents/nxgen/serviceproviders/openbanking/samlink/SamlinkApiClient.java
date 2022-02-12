package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.JWTClaimsSet;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.FormKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseResponseWithoutHref;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.TokenBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.AccountEntityBaseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.BalanceBaseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.AccountsBaseResponseBerlinGroup;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.SamlinkConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.configuration.SamlinkAgentsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.configuration.SamlinkConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.fetcher.creditcard.rpc.CardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.fetcher.creditcard.rpc.CardsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.filter.SamlinkSessionErrorFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.provider.SamlinkAuthorisationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.provider.SamlinkSignatureEntity;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.cryptography.hash.Hash;

@Slf4j
public class SamlinkApiClient extends BerlinGroupApiClient<SamlinkConfiguration> {

    private final QsealcSigner qsealcSigner;
    private final AgentConfiguration configuration;
    private final SamlinkAgentsConfiguration agentConfiguration;
    private final SystemUpdater systemUpdater;
    private final Credentials credentials;
    private String organizationIdentifier;
    private final AgentComponentProvider componentProvider;

    public SamlinkApiClient(
            final TinkHttpClient client,
            final PersistentStorage persistentStorage,
            final QsealcSigner qsealcSigner,
            final AgentConfiguration configuration,
            final SamlinkConfiguration samlinkConfiguration,
            final CredentialsRequest request,
            final SamlinkAgentsConfiguration agentConfiguration,
            final LogMasker logMasker,
            final AgentComponentProvider componentProvider) {
        super(
                client,
                persistentStorage,
                logMasker,
                samlinkConfiguration,
                request,
                configuration.getRedirectUrl(),
                configuration.getQsealc());

        this.agentConfiguration = agentConfiguration;
        this.configuration = configuration;
        this.qsealcSigner = qsealcSigner;
        this.organizationIdentifier = getOrganizationIdentifier();
        this.systemUpdater = componentProvider.getSystemUpdater();
        this.componentProvider = componentProvider;
        this.credentials = componentProvider.getCredentialsRequest().getCredentials();
        client.addFilter(new SamlinkSessionErrorFilter());
    }

    public URL getAuthorizeUrl(final String state) {
        final String consentId = getConsentId();

        // TODO - LLAMAS-52: Temporary log to verify consent Id
        persistentStorage.put(StorageKeys.CONSENT_ID, consentId);
        log.info("Consent-ID to persist: {}", Hash.sha256AsHex(consentId));

        final String authUrl = agentConfiguration.getBaseOauthUrl() + Urls.AUTH;
        return getAuthorizeUrlWithCode(
                        authUrl, state, consentId, organizationIdentifier, getRedirectUrl())
                .getUrl();
    }

    // The "withBalance" URL parameter is not supported by SamLink
    @Override
    public AccountsBaseResponseBerlinGroup fetchAccounts() {

        try {
            AccountsBaseResponseBerlinGroup response =
                    createRequestInSession(
                                    new URL(agentConfiguration.getBaseUrl()).concat(Urls.ACCOUNTS),
                                    StringUtils.EMPTY)
                            .get(AccountsBaseResponseBerlinGroup.class);

            final List<AccountEntityBaseEntity> accountsWithBalances =
                    response.getAccounts().stream()
                            .map(this::fetchBalances)
                            .collect(Collectors.toList());

            return new AccountsBaseResponseBerlinGroup(accountsWithBalances);
        } catch (HttpResponseException e) {
            final HttpResponse httpResponse = e.getResponse();
            handleApiKeyError(httpResponse);
            SamlinkSessionErrorFilter.throwIfConsentError(httpResponse);
            throw e;
        }
    }

    private void handleApiKeyError(HttpResponse httpResponse) {
        if (httpResponse.getBody(String.class).contains(SamlinkConstants.ErrorMessage.NO_API_KEY)
                && credentials.getType() == CredentialsTypes.PASSWORD) {
            credentials.setType(CredentialsTypes.THIRD_PARTY_APP);
            systemUpdater.updateCredentialsExcludingSensitiveInformation(credentials, false);
        }
    }

    private AccountEntityBaseEntity fetchBalances(final AccountEntityBaseEntity accountBaseEntity) {
        final URL url =
                new URL(
                        agentConfiguration
                                .getBaseUrl()
                                .concat(accountBaseEntity.getBalancesLink()));
        final List<BalanceBaseEntity> balances =
                createRequestInSession(url, StringUtils.EMPTY)
                        .get(AccountEntityBaseEntity.class)
                        .getBalances();
        accountBaseEntity.setBalances(balances);

        return accountBaseEntity;
    }

    private RequestBuilder buildRequestWithSignature(final URL url, final String body) {
        final String digest = generateDigest(body);
        final String requestId = UUID.randomUUID().toString();

        try {
            return client.request(url)
                    .header(Psd2Headers.Keys.PSU_IP_ADDRESS, "0.0.0.0")
                    .header(HeaderKeys.X_REQUEST_ID, requestId)
                    .header(HeaderKeys.DIGEST, digest)
                    .header(HeaderKeys.TPP_REDIRECT_URI, getRedirectUrl())
                    .header(
                            SamlinkConstants.HeaderKeys.TPP_SIGNATURE_CERTIFICATE,
                            CertificateUtils.getDerEncodedCertFromBase64EncodedCertificate(
                                    configuration.getQsealc()))
                    .header(
                            SamlinkConstants.HeaderKeys.SIGNATURE,
                            getAuthorization(digest, requestId))
                    .header(SamlinkConstants.HeaderKeys.API_KEY, getConfiguration().getApiKey());
        } catch (CertificateException e) {
            throw new IllegalStateException("Invalid qsealc detected", e);
        }
    }

    private RequestBuilder createRequestInSession(URL url, String body) {
        final OAuth2Token token = getTokenFromSession(StorageKeys.OAUTH_TOKEN);

        // TODO - LLAMAS-52: Temporary log to verify consent Id
        log.info(
                "Persisted Consent-ID: {}",
                Hash.sha256AsHex(persistentStorage.get(StorageKeys.CONSENT_ID)));

        return buildRequestWithSignature(url, body)
                .addBearerToken(token)
                .header(HeaderKeys.CONSENT_ID, persistentStorage.get(StorageKeys.CONSENT_ID));
    }

    // The bookingStatus parameter "both" is not supported by Samlink
    @Override
    public TransactionsResponse fetchTransactions(final String url) {
        return createRequestInSession(new URL(url), StringUtils.EMPTY)
                .get(TransactionsResponse.class);
    }

    @Override
    public OAuth2Token getToken(final String code) {
        final String codeVerifier =
                persistentStorage.get(BerlinGroupConstants.StorageKeys.CODE_VERIFIER);
        final String body =
                Form.builder()
                        .put(FormKeys.GRANT_TYPE, FormValues.AUTHORIZATION_CODE)
                        .put(FormKeys.CLIENT_ID, organizationIdentifier)
                        .put(FormKeys.REDIRECT_URI, getRedirectUrl())
                        .put(FormKeys.CODE_VERIFIER, codeVerifier)
                        .put(FormKeys.CODE, code)
                        .put(
                                "client_assertion_type",
                                "urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
                        .put("client_assertion", buildSignedClientAssertion())
                        .build()
                        .serialize();
        return buildRequestWithSignature(
                        new URL(agentConfiguration.getBaseOauthUrl()).concat(Urls.TOKEN), body)
                .body(body, MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenBaseResponse.class)
                .toTinkToken();
    }

    @Override
    public String getConsentId() {

        AccessEntity accessEntity = new AccessEntity.Builder().build();
        final ConsentBaseRequest consentsRequest =
                new ConsentBaseRequest(
                        componentProvider
                                .getLocalDateTimeSource()
                                .now(ZoneOffset.UTC)
                                .toLocalDate());
        consentsRequest.setAccess(accessEntity);

        return buildRequestWithSignature(
                        new URL(agentConfiguration.getBaseUrl()).concat(Urls.CONSENT),
                        consentsRequest.toData())
                .body(consentsRequest.toData(), MediaType.APPLICATION_JSON_TYPE)
                .post(ConsentBaseResponseWithoutHref.class)
                .getConsentId();
    }

    public CardsResponse fetchCardAccounts() {
        return createRequestInSession(
                        new URL(agentConfiguration.getBaseUrl()).concat(Urls.CARD_ACCOUNTS),
                        StringUtils.EMPTY)
                .get(CardsResponse.class);
    }

    public CardTransactionsResponse fetchCardAccountTransactions(String url) {
        return createRequestInSession(new URL(url), StringUtils.EMPTY)
                .get(CardTransactionsResponse.class);
    }

    @Override
    public OAuth2Token refreshToken(final String token) {
        return null;
    }

    private String getAuthorization(final String digest, String requestId) {
        final String keyId = Psd2Headers.getTppCertificateKeyId(getX509Certificate());
        return new SamlinkAuthorisationEntity(keyId, getSignature(digest, requestId)).toString();
    }

    private String generateDigest(final String data) {
        return SamlinkConstants.HeaderKeys.DIGEST_PREFIX + Psd2Headers.calculateDigest(data);
    }

    private String getSignature(final String digest, String requestId) {
        final SamlinkSignatureEntity signatureEntity =
                new SamlinkSignatureEntity(digest, requestId, getRedirectUrl());

        return qsealcSigner.getSignatureBase64(signatureEntity.toString().getBytes());
    }

    private String buildSignedClientAssertion() {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(TimeUnit.MINUTES.toSeconds(10));

        JWTClaimsSet claimsSet =
                new JWTClaimsSet.Builder()
                        .issuer(organizationIdentifier)
                        .subject(organizationIdentifier)
                        .audience(agentConfiguration.getBaseOauthUrl() + (Urls.TOKEN))
                        .issueTime(Date.from(issuedAt))
                        .expirationTime(Date.from(expiresAt))
                        .jwtID(UUID.randomUUID().toString())
                        .build();

        Payload payload = new Payload(claimsSet.toJSONObject());

        JWSHeader header =
                new JWSHeader.Builder(JWSAlgorithm.parse("RS256")).type(JOSEObjectType.JWT).build();

        JWSObject jws = new JWSObject(header, payload);

        final String signature =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(qsealcSigner.getSignature(jws.getSigningInput()));

        return new String(jws.getSigningInput()) + "." + signature;
    }

    private X509Certificate getX509Certificate() {
        try {
            return CertificateUtils.getX509CertificatesFromBase64EncodedCert(getQSealc()).stream()
                    .findFirst()
                    .get();
        } catch (CertificateException ce) {
            throw new SecurityException("Certificate error", ce);
        }
    }

    private String getOrganizationIdentifier() {
        if (organizationIdentifier == null) {
            try {
                organizationIdentifier =
                        CertificateUtils.getOrganizationIdentifier(configuration.getQsealc());
            } catch (CertificateException ce) {
                throw new IllegalStateException("Could not extract organization identifier!", ce);
            }
        }
        return organizationIdentifier;
    }
}
