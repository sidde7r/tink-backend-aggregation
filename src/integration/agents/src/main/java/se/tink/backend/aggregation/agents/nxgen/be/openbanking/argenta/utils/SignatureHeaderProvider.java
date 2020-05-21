package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.utils;

import static se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants.*;
import static se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants.HeaderValues.*;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.configuration.ArgentaConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;

@RequiredArgsConstructor
public final class SignatureHeaderProvider {

    private final ArgentaConfiguration argentaConfiguration;
    private final QsealcSigner qsealcSigner;

    public String generateSignatureHeader(Map<String, Object> headers) {

        String signedHeaders =
                Arrays.stream(HeadersToSign.values())
                        .map(HeadersToSign::getHeader)
                        .filter(headers::containsKey)
                        .map(String::toLowerCase)
                        .collect(Collectors.joining(" "));

        String signedHeadersWithValues =
                Arrays.stream(HeadersToSign.values())
                        .map(HeadersToSign::getHeader)
                        .filter(headers::containsKey)
                        .map(header -> String.format("%s: %s", header, headers.get(header)))
                        .collect(Collectors.joining("\n"));

        String signature = qsealcSigner.getSignatureBase64(signedHeadersWithValues.getBytes());

        return String.format(
                SIGNATURE_HEADER, argentaConfiguration.getKeyId(), signedHeaders, signature);
    }
}
