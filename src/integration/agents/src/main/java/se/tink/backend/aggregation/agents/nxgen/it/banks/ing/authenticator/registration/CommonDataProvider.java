package se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration;

import com.sun.jersey.core.header.OutBoundHeaders;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.core.MultivaluedMap;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.ConfigurationProvider;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.Cryptor;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;

@AllArgsConstructor
public class CommonDataProvider {

    private static final String FINGERPRINT_SUFFIX = "LL4jVBCmcZKXbIP7";
    private static final String FPE_SEPARATOR = "|";

    private Cryptor cryptor;
    private ConfigurationProvider configurationProvider;

    String prepareFingerprint(String deviceId) {
        return Optional.of(deviceId)
                .map(this::removeDashes)
                .map(x -> x + FINGERPRINT_SUFFIX)
                .map(Hash::sha256)
                .map(cryptor::encodeBase64)
                .orElseThrow(IllegalArgumentException::new);
    }

    private String removeDashes(String deviceId) {
        return deviceId.replace("-", "");
    }

    MultivaluedMap<String, Object> getStaticHeaders() {
        OutBoundHeaders headers = new OutBoundHeaders();
        headers.putSingle("X-OTML-PROFILE", "appstore");
        headers.putSingle("X-OTML-NONCE", 1L);
        headers.putSingle(
                "User-Agent",
                "Mozilla/5.0 (iPhone; U; CPU OS 3_2 like Mac OS X; en-us) "
                        + "AppleWebKit/531.21.10 (KHTML, like Gecko) "
                        + "Version/4.0.4 Mobile/7B334b Safari/531.21.10");
        headers.putSingle("X-OTMLID", "1.07");
        headers.putSingle("X-OTML-ADVANCED-MANIFEST", true);
        headers.putSingle("X-APPID", "iPhone_Ing_41_3.0.15");
        headers.putSingle("X-OTML-PLATFORM", "ios");
        headers.putSingle("X-OTML-CLUSTER", "{750, 1334}");
        headers.putSingle("Accept-Language", "it-IT, it-IT;q=0.5");
        headers.putSingle("Accept", "*/*");
        headers.putSingle("Accept-Encoding", "br, gzip, deflate");
        return headers;
    }

    String prepareFpe(String deviceId) {
        byte[] aesKey = cryptor.generateRandomAesKey();
        byte[] aesIv = cryptor.generateRandomAesIv();
        return Stream.of(
                        cryptor.rsaEncrypt(configurationProvider.getRsaExternalPublicKey(), aesKey),
                        cryptor.rsaEncrypt(configurationProvider.getRsaExternalPublicKey(), aesIv),
                        cryptor.aesEncrypt(
                                aesKey,
                                aesIv,
                                prepareFingerprint(deviceId).getBytes(StandardCharsets.UTF_8)))
                .map(cryptor::encodeBase64)
                .collect(Collectors.joining(FPE_SEPARATOR));
    }
}
