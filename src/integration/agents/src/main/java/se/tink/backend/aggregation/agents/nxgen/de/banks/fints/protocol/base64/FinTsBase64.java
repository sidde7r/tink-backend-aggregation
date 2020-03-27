package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.base64;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.FinTsRequest;

public class FinTsBase64 {

    public static String decodeResponseFromBase64(String b64EncodedResponse) {
        return new String(
                Base64.getDecoder().decode(b64EncodedResponse.replaceAll("\\R", "")),
                StandardCharsets.ISO_8859_1);
    }

    public static String encodeRequestToBase64(FinTsRequest request) {
        return Base64.getEncoder().encodeToString(request.toFinTsFormat().getBytes());
    }
}
