package se.tink.backend.system.rpc;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.UUID;
import org.junit.Test;
import se.tink.backend.core.product.ProductPropertyKey;

public class UpdateProductInformationRequestTest {
    @Test
    public void serializeDeserialize() throws IOException {
        // Setup data
        UUID productInstanceId = UUID.randomUUID();
        UpdateProductInformationRequest updateRequest = new UpdateProductInformationRequest(
                "userid",
                productInstanceId,
                Maps.newHashMap(ImmutableMap.of(ProductPropertyKey.INTEREST_RATE, (Object) 0.0035)));

        // Mapper for conversion
        ObjectMapper objectMapper = new ObjectMapper();

        // Serialize
        String json = objectMapper.writeValueAsString(updateRequest);

        // Deserialize
        UpdateProductInformationRequest deserializedParameters = objectMapper
                .readValue(json, UpdateProductInformationRequest.class);

        assertThat(deserializedParameters.getProductInstanceId()).isEqualTo(productInstanceId);
        assertThat(deserializedParameters.getProductProperties())
                .isNotNull()
                .isNotEmpty()
                .containsKey(ProductPropertyKey.INTEREST_RATE);
        assertThat(deserializedParameters.getProductProperties().get(ProductPropertyKey.INTEREST_RATE))
                .isEqualTo(0.0035);
    }

    @Test
    public void serializedJsonUsesEnumKeysInsteadOfName() throws JsonProcessingException {
        // Setup data
        UUID productInstanceId = UUID.randomUUID();
        UpdateProductInformationRequest updateRequest = new UpdateProductInformationRequest(
                "userid",
                productInstanceId,
                Maps.newHashMap(ImmutableMap.of(ProductPropertyKey.INTEREST_RATE, (Object) 0.0035)));

        // Mapper for conversion
        ObjectMapper objectMapper = new ObjectMapper();

        // Serialize
        String json = objectMapper.writeValueAsString(updateRequest);

        // Check enum key serialization
        assertThat(json)
                .doesNotContain(ProductPropertyKey.INTEREST_RATE.name())
                .contains(ProductPropertyKey.INTEREST_RATE.getKey());
    }
}