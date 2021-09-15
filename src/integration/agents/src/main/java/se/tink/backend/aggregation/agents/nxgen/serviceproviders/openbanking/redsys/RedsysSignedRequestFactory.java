package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.configuration.RedsysConfiguration;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidasidentity.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

class RedsysSignedRequestFactory {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private RedsysConfiguration configuration;
    private EidasProxyConfiguration eidasProxyConfiguration;
    private String clientSigningCertificate;
    private String signingKeyId;
    private String psuIpAddress;
    private final EidasIdentity eidasIdentity;

    RedsysSignedRequestFactory(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            EidasIdentity eidasIdentity,
            AgentComponentProvider componentProvider) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.eidasIdentity = eidasIdentity;
        this.psuIpAddress =
                componentProvider
                        .getCredentialsRequest()
                        .getUserAvailability()
                        .getOriginatingUserIp();
    }

    RequestBuilder createSignedRequest(
            String url, @Nullable Object payload, Map<String, Object> headers) {
        return createSignedRequest(url, payload, getTokenFromStorage(), headers);
    }

    RequestBuilder createSignedRequest(String url) {
        return createSignedRequest(url, null, getTokenFromStorage(), Maps.newHashMap());
    }

    private RequestBuilder createSignedRequest(
            String url, @Nullable Object payload, OAuth2Token token, Map<String, Object> headers) {
        String serializedPayload = "";
        if (payload != null) {
            serializedPayload = SerializationUtils.serializeToString(payload);
        }

        // construct headers
        final Map<String, Object> allHeaders = Maps.newHashMap(headers);
        allHeaders.put(RedsysConstants.HeaderKeys.IBM_CLIENT_ID, getConfiguration().getClientId());
        final String digest =
                RedsysConstants.Signature.DIGEST_PREFIX
                        + Base64.getEncoder().encodeToString(Hash.sha256(serializedPayload));
        allHeaders.put(RedsysConstants.HeaderKeys.DIGEST, digest);
        if (!allHeaders.containsKey(RedsysConstants.HeaderKeys.REQUEST_ID)) {
            final String requestID = UUID.randomUUID().toString().toLowerCase(Locale.ENGLISH);
            allHeaders.put(RedsysConstants.HeaderKeys.REQUEST_ID, requestID);
        }

        if (!Strings.isNullOrEmpty(psuIpAddress)) {
            allHeaders.put(RedsysConstants.HeaderKeys.PSU_IP_ADDRESS, psuIpAddress);
        }

        final String signature =
                RedsysUtils.generateRequestSignature(
                        signingKeyId, eidasProxyConfiguration, eidasIdentity, allHeaders);
        allHeaders.put(RedsysConstants.HeaderKeys.SIGNATURE, signature);
        allHeaders.put(
                RedsysConstants.HeaderKeys.TPP_SIGNATURE_CERTIFICATE, clientSigningCertificate);

        RequestBuilder builder =
                client.request(url)
                        .addBearerToken(token)
                        .headers(allHeaders)
                        .accept(MediaType.APPLICATION_JSON);

        if (payload != null) {
            builder = builder.body(serializedPayload, MediaType.APPLICATION_JSON);
        }

        return builder;
    }

    private OAuth2Token getTokenFromStorage() {
        return sessionStorage
                .get(RedsysConstants.StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    private RedsysConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        RedsysConstants.ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(
            AgentConfiguration<RedsysConfiguration> agentConfiguration,
            EidasProxyConfiguration eidasProxyConfiguration) {
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.eidasProxyConfiguration = eidasProxyConfiguration;
        try {
            this.clientSigningCertificate =
                    CertificateUtils.getDerEncodedCertFromBase64EncodedCertificate(
                            agentConfiguration.getQsealc());
            this.signingKeyId =
                    Psd2Headers.getTppCertificateKeyId(
                            agentConfiguration.getQsealc(),
                            16,
                            CertificateUtils.CANameEncoding.BASE64_IF_NOT_ASCII);
        } catch (CertificateException e) {
            throw new IllegalStateException("Could not read values from QsealC certificate", e);
        }

        if (eidasProxyConfiguration != null) {
            client.setEidasProxy(eidasProxyConfiguration);
        }
    }
}
