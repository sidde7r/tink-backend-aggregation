package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.detail;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.FiduciaSignatureHeaderGenerator;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.utils.SignatureUtils;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
@Slf4j
public class FiduciaRequestBuilder {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final FiduciaSignatureHeaderGenerator signatureHeaderGenerator;
    private final FiduciaHeaderValues headerValues;

    public RequestBuilder createRequestInSession(URL url, String consentId, String digest) {
        return createRequest(url, digest).header(HeaderKeys.CONSENT_ID, consentId);
    }

    public RequestBuilder createRequest(URL url, String body) {
        Map<String, Object> headers = getHeaders(body);

        log.info(
                String.format(
                        "[FIDUCIA]: REQUEST: TPP_SIGNATURE_CERTIFICATE: %s",
                        headers.getOrDefault(HeaderKeys.TPP_SIGNATURE_CERTIFICATE, "")));

        return client.request(url)
                .headers(headers)
                .header(
                        HeaderKeys.SIGNATURE,
                        signatureHeaderGenerator.generateSignatureHeader(headers));
    }

    private Map<String, Object> getHeaders(String body) {
        String digest = SignatureUtils.createDigest(body);

        String tppRedirectUrl =
                new URL(headerValues.getRedirectUrl())
                        .queryParam("state", sessionStorage.get(StorageKeys.STATE))
                        .toString();

        String requestId = UUID.randomUUID().toString();

        Map<String, Object> headers = new HashMap<>();

        headers.put(HeaderKeys.ACCEPT, MediaType.APPLICATION_JSON);
        headers.put(HeaderKeys.TPP_ID, headerValues.getTppOrganizationIdentifier());
        headers.put(
                HeaderKeys.DATE, ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
        headers.put(HeaderKeys.X_REQUEST_ID, requestId);
        headers.put(HeaderKeys.TPP_REDIRECT_URI, tppRedirectUrl);
        headers.put(HeaderKeys.TPP_SIGNATURE_CERTIFICATE, headerValues.getSealcDerBase64());
        headers.put(HeaderKeys.DIGEST, digest);

        headers.put(HeaderKeys.PSU_IP_ADDRESS, headerValues.getUserIp());
        Optional.ofNullable(sessionStorage.get(StorageKeys.PSU_ID))
                .ifPresent(psuId -> headers.put(HeaderKeys.PSU_ID, psuId));

        return headers;
    }

    public RequestBuilder createPaymentRequest(
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
                .header(HeaderKeys.PSU_IP_ADDRESS, headerValues.getUserIp())
                .header(HeaderKeys.DIGEST, digest)
                .header(HeaderKeys.SIGNATURE, signature)
                .header(HeaderKeys.TPP_SIGNATURE_CERTIFICATE, certificate)
                .header(HeaderKeys.DATE, date);
    }
}
