package se.tink.backend.aggregation.api;

import static org.assertj.core.api.Assertions.assertThat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.UUID;
import org.junit.Test;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.backend.aggregation.rpc.RefreshApplicationRequest;
import se.tink.backend.aggregation.rpc.User;
import se.tink.backend.core.application.RefreshApplicationParameterKey;
import se.tink.backend.aggregation.rpc.ProductType;

public class RefreshApplicationRequestTest {
    @Test
    public void serializeDeserialize() throws IOException {
        // Setup data
        RefreshApplicationRequest productInformationParameters = new RefreshApplicationRequest(
                new User(),
                new Provider(),
                new Credentials(),
                ProductType.MORTGAGE,
                UUID.randomUUID(),
                Maps.newHashMap(ImmutableMap.of(
                        RefreshApplicationParameterKey.EXTERNAL_ID, (Object) "abc-123")));

        // Mapper for conversion
        ObjectMapper objectMapper = new ObjectMapper();

        // Serialize
        String json = objectMapper.writeValueAsString(productInformationParameters);

        // Deserialize
        RefreshApplicationRequest deserializedParameters = objectMapper
                .readValue(json, RefreshApplicationRequest.class);

        assertThat(deserializedParameters.getProductType()).isEqualTo(ProductType.MORTGAGE);
        assertThat(deserializedParameters.getParameters())
                .isNotNull()
                .isNotEmpty()
                .containsKey(RefreshApplicationParameterKey.EXTERNAL_ID);
        assertThat(deserializedParameters.getParameters().get(RefreshApplicationParameterKey.EXTERNAL_ID))
                .isEqualTo("abc-123");
    }

    @Test
    public void serializedJsonUsesEnumKeysInsteadOfName() throws JsonProcessingException {
        // Setup data
        RefreshApplicationRequest productInformationParameters = new RefreshApplicationRequest(
                new User(),
                new Provider(),
                new Credentials(),
                ProductType.MORTGAGE,
                UUID.randomUUID(),
                Maps.newHashMap(ImmutableMap.of(
                        RefreshApplicationParameterKey.EXTERNAL_ID, (Object) "abc-123")));

        // Mapper for conversion
        ObjectMapper objectMapper = new ObjectMapper();

        // Serialize
        String json = objectMapper.writeValueAsString(productInformationParameters);

        // Check enum key serialization
        assertThat(json).contains(ProductType.MORTGAGE.name());

        assertThat(json)
                .doesNotContain(RefreshApplicationParameterKey.EXTERNAL_ID.name())
                .contains(RefreshApplicationParameterKey.EXTERNAL_ID.getKey());
    }
}
