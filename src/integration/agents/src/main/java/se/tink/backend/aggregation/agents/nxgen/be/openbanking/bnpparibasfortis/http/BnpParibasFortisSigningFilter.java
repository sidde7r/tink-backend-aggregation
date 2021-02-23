package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.http;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterOrder;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterPhases;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@FilterOrder(category = FilterPhases.REQUEST_HANDLE, order = Integer.MIN_VALUE)
@RequiredArgsConstructor
@Slf4j
public class BnpParibasFortisSigningFilter extends Filter {

    private static final String HEADERS =
            "(request-target) (created) (expires) host x-request-id digest";
    private static final String SIGNATURE =
            "keyId=\"%s\",algorithm=\"RS256\",headers=\""
                    + HEADERS
                    + "\",(created)=\"%s\",(expires)=\"%s\",signature=\"%s\"";
    private static final String DIGEST = "SHA-256=%s";
    private static final String SIGNATURE_INPUT =
            "(request-target): %s %s\n"
                    + "(created): %s\n"
                    + "(expires): %s\n"
                    + "host: %s\n"
                    + "x-request-id: %s\n"
                    + "digest: %s";

    private final String keyId;
    private final LocalDateTimeSource localDateTimeSource;
    private final QsealcSigner signer;

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        Long now = localDateTimeSource.getInstant().getEpochSecond();
        String requestId = UUID.randomUUID().toString();
        String body = Optional.ofNullable((String) httpRequest.getBody()).orElse("");
        log.info("body:\n{}", body);
        String digest = computeDigest(body);
        log.info("digest:={}", digest);
        String signature = computeSignature(httpRequest, now, now + 60, requestId, digest);
        log.info("signatureHeader={}", signature);

        httpRequest.getHeaders().add(BnpParibasFortisConstants.HeaderKeys.REQUEST_ID, requestId);
        httpRequest.getHeaders().add(BnpParibasFortisConstants.HeaderKeys.DIGEST, digest);
        httpRequest.getHeaders().add(BnpParibasFortisConstants.HeaderKeys.SIGNATURE, signature);

        return nextFilter(httpRequest);
    }

    private String computeSignature(
            HttpRequest request, Long now, Long expires, String requestId, String digest) {
        String signatureValue =
                signer.getSignatureBase64(
                        computeSignatureInput(request, now, expires, requestId, digest));
        log.info("signatureVal={}", signatureValue);
        return String.format(SIGNATURE, keyId, now, expires, signatureValue);
    }

    private static String computeDigest(String body) {
        String digestValue = Hash.sha256Base64(body.getBytes());
        return String.format(DIGEST, digestValue);
    }

    private static byte[] computeSignatureInput(
            HttpRequest request, Long now, Long expires, String requestId, String digest) {
        byte[] value =
                String.format(
                                SIGNATURE_INPUT,
                                request.getMethod().name().toLowerCase(),
                                request.getURI().getPath(),
                                now,
                                expires,
                                request.getURI().getHost(),
                                requestId,
                                digest)
                        .getBytes();
        log.info("computed signature input b64={}", EncodingUtils.encodeAsBase64String(value));
        return value;
    }
}
