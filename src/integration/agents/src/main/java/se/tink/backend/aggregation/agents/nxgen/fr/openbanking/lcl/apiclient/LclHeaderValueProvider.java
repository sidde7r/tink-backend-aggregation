package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.configuration.LclConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.signature.LclSignatureProvider;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RequiredArgsConstructor
public class LclHeaderValueProvider {

    private final LclSignatureProvider signatureProvider;
    private final LclConfiguration configuration;
    private final LocalDateTimeSource localDateTimeSource;

    public String getSignatureHeaderValue(String requestId, String date, String digest) {
        final String signature = signatureProvider.signRequest(requestId, date, digest);
        return String.join(
                ",",
                "keyId=" + '"' + configuration.getQsealcKeyId() + '"',
                "algorithm=\"rsa-sha256\"",
                "headers=\"x-request-id date digest\"",
                "signature=" + '"' + signature + '"');
    }

    public String getDigestHeaderValue(Object requestBody) {
        final String serializedBody = serializeBody(requestBody);
        return "SHA-256=" + Base64.getEncoder().encodeToString(Hash.sha256(serializedBody));
    }

    public String getDateHeaderValue() {
        return DateTimeFormatter.RFC_1123_DATE_TIME.format(
                localDateTimeSource.getInstant().atZone(ZoneId.of("CET")));
    }

    private String serializeBody(Object body) {
        return Objects.isNull(body) ? "" : SerializationUtils.serializeToString(body);
    }
}
