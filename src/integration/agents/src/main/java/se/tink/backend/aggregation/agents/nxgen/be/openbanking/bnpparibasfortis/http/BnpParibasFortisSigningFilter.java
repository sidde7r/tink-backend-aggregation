package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.http;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
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
public class BnpParibasFortisSigningFilter extends Filter {

    private static final String SIGNATURE_HEADER_FORMAT =
            "keyId=\"%s\",algorithm=\"RS256\",headers=\""
                    + "(request-target) (created) (expires) host x-request-id digest"
                    + "\",(created)=\"%s\",(expires)=\"%s\",signature=\"%s\"";
    private static final String DIGEST_INPUT_FORMAT = "SHA-256=%s";
    private static final String SIGNATURE_INPUT_FORMAT =
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
        String digest = computeDigest(body);
        String signature = computeSignature(httpRequest, now, now + 60, requestId, digest);

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
        return String.format(SIGNATURE_HEADER_FORMAT, keyId, now, expires, signatureValue);
    }

    private static String computeDigest(String body) {
        String digestValue = Hash.sha256Base64(body.getBytes());
        return String.format(DIGEST_INPUT_FORMAT, digestValue);
    }

    private static byte[] computeSignatureInput(
            HttpRequest request, Long now, Long expires, String requestId, String digest) {
        return String.format(
                        SIGNATURE_INPUT_FORMAT,
                        request.getMethod().name().toLowerCase(),
                        request.getURI().getPath(),
                        now,
                        expires,
                        request.getURI().getHost(),
                        requestId,
                        digest)
                .getBytes();
    }
}
