package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.macgenerator;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmericanExpressConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexGrantType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.configuration.AmexConfiguration;
import se.tink.backend.aggregation.nxgen.core.authentication.HmacToken;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class AmexMacGenerator {

    private static final String SERVER_PORT = "443";

    private final AmexConfiguration amexConfiguration;
    private final MacSignatureCreator macSignatureCreator;
    private final Clock clock;

    public String generateAuthMacValue(AmexGrantType grantType) {
        final String timestamp = getUnixTimestamp();
        final String nonce = macSignatureCreator.createNonce();
        final String baseString = createBaseAuthString(timestamp, nonce, grantType);
        final String signatureString =
                macSignatureCreator.createSignature(
                        amexConfiguration.getClientSecret(), baseString);

        return String.format(
                "MAC id=\"%s\",ts=\"%s\",nonce=\"%s\",mac=\"%s\"",
                amexConfiguration.getClientId(), timestamp, nonce, signatureString);
    }

    public String generateDataMacValue(String resourcePath, HmacToken hmacToken) {
        final String timestamp = getUnixTimestamp();
        final String nonce = macSignatureCreator.createNonce();
        final String baseString = createBaseDataString(timestamp, nonce, resourcePath);
        final String signatureString =
                macSignatureCreator.createSignature(hmacToken.getMacKey(), baseString);

        return String.format(
                "MAC id=\"%s\",ts=\"%s\",nonce=\"%s\",mac=\"%s\"",
                hmacToken.getAccessToken(), timestamp, nonce, signatureString);
    }

    private String createBaseAuthString(String timestamp, String nonce, AmexGrantType grantType) {
        return String.join(
                "\n", amexConfiguration.getClientId(), timestamp, nonce, grantType.getType(), "");
    }

    private String createBaseDataString(String timestamp, String nonce, String resourcePath) {
        return String.join(
                "\n",
                timestamp,
                nonce,
                HttpMethod.GET.name(),
                URL.urlEncode(resourcePath),
                Urls.SERVER_URL.toUri().getHost(),
                SERVER_PORT,
                "",
                "");
    }

    private String getUnixTimestamp() {
        return String.valueOf(clock.millis() / 1000L);
    }
}
