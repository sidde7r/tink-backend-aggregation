package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.base64;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.FinTsRequest;

public class FinTsBase64 {
    private static final Pattern REPLACE_PATTERN = Pattern.compile("\\R");

    public static String decodeResponseFromBase64(String b64EncodedResponse) {
        String responseToDecode = REPLACE_PATTERN.matcher(b64EncodedResponse).replaceAll("");
        return new String(
                Base64.getDecoder().decode(responseToDecode), StandardCharsets.ISO_8859_1);
    }

    public static String encodeRequestToBase64(FinTsRequest request) {
        return Base64.getEncoder().encodeToString(request.toFinTsFormat().getBytes());
    }
}
