package se.tink.backend.common.providers.booli.entities.request;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.codec.binary.Hex;
import se.tink.backend.common.config.BooliIntegrationConfiguration;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.utils.StringUtils;

public class AuthParameters {
    private final String callerId;
    private final String privateKey;

    @Inject
    public AuthParameters(BooliIntegrationConfiguration configuration) {
        this(configuration != null ? configuration.getCallerId() : null,
                configuration != null ? configuration.getPrivateKey() : null);
    }

    public AuthParameters(String callerId, String privateKey) {
        this.callerId = callerId;
        this.privateKey = privateKey;
    }

    public Map<String, String> getQueryParameters() {
        String time = String.valueOf(System.currentTimeMillis() / 1000); // 1488963163
        String unique = UUIDUtils.toTinkUUID(UUID.randomUUID());
        String hash = sha1sum(callerId, time, privateKey, unique);

        return ImmutableMap.<String, String>builder()
                .put("callerId", callerId)
                .put("time", time)
                .put("unique", unique)
                .put("hash", hash).build();
    }

    private static String sha1sum(String... strings) {
        StringBuilder content = new StringBuilder();

        for (String s : strings) {
            content.append(s);
        }

        return new String(Hex.encodeHex(StringUtils.hashSHA1(content.toString())));
    }
}
