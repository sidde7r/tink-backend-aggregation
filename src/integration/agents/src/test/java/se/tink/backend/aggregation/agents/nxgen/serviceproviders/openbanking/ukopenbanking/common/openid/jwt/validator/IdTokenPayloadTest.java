package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.validator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class IdTokenPayloadTest {

    private static final String ID_TOKEN_PAYLOAD_WITH_AUDIENCE_AS_STRING =
            "{\"sub\":\"1234567890\",\"name\":\"John Doe\",\"iat\":1516239022,\"aud\":\"11223344\",\"at_hash\":\"tiDPh518KVs7iueTHST6QQ\",\"openbanking_intent_id\":[\"555\",\"666\"]}";
    private static final String ID_TOKEN_PAYLOAD_WITH_AUDIENCE_AS_ARRAY =
            "{\"sub\":\"1234567890\",\"name\":\"John Doe\",\"iat\":1516239022,\"aud\":[\"11223344\",\"https://example.com\"],\"at_hash\":\"tiDPh518KVs7iueTHST6QQ\"}";

    @Test
    public void shouldDeserializeIdTokenPayloadWithAudienceAsString() {
        // when
        IdTokenPayload payload =
                SerializationUtils.deserializeFromString(
                        ID_TOKEN_PAYLOAD_WITH_AUDIENCE_AS_STRING, IdTokenPayload.class);
        // then
        assertThat(payload.getAud().get(0)).isEqualTo("11223344");
    }

    @Test
    public void shouldDeserializeIdTokenPayloadWithAudienceAsArray() {
        // when
        IdTokenPayload payload =
                SerializationUtils.deserializeFromString(
                        ID_TOKEN_PAYLOAD_WITH_AUDIENCE_AS_ARRAY, IdTokenPayload.class);
        // then
        assertThat(payload.getAud().get(0)).isEqualTo("11223344");
        assertThat(payload.getAud().get(1)).isEqualTo("https://example.com");
    }
}
