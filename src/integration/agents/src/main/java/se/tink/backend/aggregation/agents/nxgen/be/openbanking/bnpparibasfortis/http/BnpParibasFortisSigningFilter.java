package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.http;


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
import se.tink.libraries.serialization.utils.SerializationUtils;

import java.util.UUID;

@FilterOrder(category = FilterPhases.SEND)
@RequiredArgsConstructor
@Slf4j
public class BnpParibasFortisSigningFilter extends Filter {

    private static final String HEADERS = "(request-target) (created) (expires) host x-request-id digest";

    private final String keyId;
    private final LocalDateTimeSource localDateTimeSource;
    private final QsealcSigner signer;


    @Override
    public HttpResponse handle(HttpRequest httpRequest) throws HttpClientException, HttpResponseException {
        Long now = localDateTimeSource.getInstant().getEpochSecond();
        String requestId = UUID.randomUUID().toString();
        //this is to ensure that the body is binary the same as the value used to calculate digest
        serializeBodyToString(httpRequest);
        log.info("body:\n{}", httpRequest.getBody());
        String digest = computeDigest(httpRequest);
        log.info("digest:={}", digest);
        String signature = computeSignature(httpRequest, now, now + 60, requestId, digest);
        log.info("signatureHeader={}", signature);

        httpRequest.getHeaders().putSingle(BnpParibasFortisConstants.HeaderKeys.REQUEST_ID, requestId);
        httpRequest.getHeaders().putSingle(BnpParibasFortisConstants.HeaderKeys.DIGEST, digest);
        httpRequest.getHeaders().putSingle(BnpParibasFortisConstants.HeaderKeys.SIGNATURE, signature);

        return nextFilter(httpRequest);

    }

    private String computeSignature(HttpRequest request, Long now, Long expires, String requestId, String digest) {
        String signatureValue = signer.getSignatureBase64(computeSignatureInput(request, now, expires, requestId, digest));
        log.info("signatureVal={}", signatureValue);
        return "keyId=\"" + keyId + "\",algorithm=\"RS256\",headers=\"" + HEADERS + "\",(created)=\"" + now + "\",(expires)=\"" + expires + "\", signature=\"" + signatureValue + "\"";
    }

    private static void serializeBodyToString(HttpRequest request) {
        request.setBody(SerializationUtils.serializeToString(request.getBody()));
    }


    private static String computeDigest(HttpRequest request) {
        String digestValue = Hash.sha256Base64(((String) request.getBody()).getBytes());
        return "SHA-256=" + digestValue;
    }

    private static byte[] computeSignatureInput(HttpRequest request, Long now, Long expires, String requestId, String digest) {
        byte[] value =  ("(request-target): " + request.getMethod().name().toLowerCase() + " " + request.getURI().getPath() + "\n" +
                "(created): \"" + now + "\"\n" +
                "(expires): \"" + expires + "\"\n" +
                "host: " + request.getURI().getHost() + "\n" +
                "x-request-id: " + requestId + "\n" +
                "digest: " + digest)
                .getBytes();
        log.info("computed signature input b64={}", EncodingUtils.encodeAsBase64String(value));
        return value;
    }

}
