package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.detail;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.FiduciaSignatureHeaderGenerator;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.configuration.FiduciaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.utils.SignatureUtils;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class FiduciaRequestBuilder {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final FiduciaConfiguration fiduciaConfiguration;
    private final String redirectUrl;
    private final FiduciaSignatureHeaderGenerator signatureHeaderGenerator;

    public FiduciaRequestBuilder(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            AgentConfiguration<FiduciaConfiguration> agentConfiguration,
            FiduciaSignatureHeaderGenerator signatureHeaderGenerator) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.fiduciaConfiguration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        this.signatureHeaderGenerator = signatureHeaderGenerator;
    }

    public RequestBuilder createRequest(URL url, String body) {
        Map<String, Object> headers = getHeaders(body);

        return client.request(url)
                .headers(headers)
                .header(
                        HeaderKeys.SIGNATURE,
                        signatureHeaderGenerator.generateSignatureHeader(headers));
    }

    public RequestBuilder createRequest(
            URL url,
            String reqId,
            String digest,
            String signature,
            String certificate,
            String date) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.X_REQUEST_ID, reqId)
                .header(HeaderKeys.DIGEST, digest)
                .header(HeaderKeys.SIGNATURE, signature)
                .header(HeaderKeys.TPP_SIGNATURE_CERTIFICATE, certificate)
                .header(HeaderKeys.DATE, date);
    }

    public RequestBuilder createRequestInSession(URL url, String consentId, String digest) {
        return createRequest(url, digest).header(HeaderKeys.CONSENT_ID, consentId);
    }

    private Map<String, Object> getHeaders(String body) {
        String digest = SignatureUtils.createDigest(body);

        String tppRedirectUrl =
                new URL(redirectUrl)
                        .queryParam("state", sessionStorage.get(StorageKeys.STATE))
                        .toString();

        String requestId = UUID.randomUUID().toString();

        Map<String, Object> headers = new HashMap<>();

        headers.put(HeaderKeys.ACCEPT, MediaType.APPLICATION_JSON);
        headers.put(HeaderKeys.TPP_ID, fiduciaConfiguration.getTppId());
        headers.put(
                HeaderKeys.DATE, ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
        headers.put(HeaderKeys.X_REQUEST_ID, requestId);
        headers.put(HeaderKeys.TPP_REDIRECT_URI, tppRedirectUrl);
        headers.put(HeaderKeys.TPP_SIGNATURE_CERTIFICATE, fiduciaConfiguration.getCertificate());
        headers.put(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.PSU_IP_ADDRESS);
        headers.put(HeaderKeys.DIGEST, digest);

        Optional.ofNullable(sessionStorage.get(StorageKeys.PSU_ID))
                .ifPresent(psuId -> headers.put(HeaderKeys.PSU_ID, psuId));

        return headers;
    }
}
