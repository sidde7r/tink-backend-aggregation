package se.tink.backend.aggregation.api;

import static org.assertj.core.api.Assertions.assertThat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.UUID;
import org.junit.Test;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.rpc.FetchProductInformationParameterKey;
import se.tink.backend.aggregation.rpc.ProductInformationRequest;
import se.tink.backend.agents.rpc.User;
import se.tink.backend.aggregation.rpc.ProductType;

public class ProductInformationRequestTest {
    @Test
    public void serializeDeserialize() throws IOException {
        // Setup data
        ProductInformationRequest productInformationParameters = new ProductInformationRequest(
                new User(),
                new Provider(),
                ProductType.MORTGAGE,
                UUID.randomUUID(),
                Maps.newHashMap(ImmutableMap.of(
                        FetchProductInformationParameterKey.MORTGAGE_AMOUNT, (Object)1000000.00)));

        // Mapper for conversion
        ObjectMapper objectMapper = new ObjectMapper();

        // Serialize
        String json = objectMapper.writeValueAsString(productInformationParameters);

        // Deserialize
        ProductInformationRequest deserializedParameters = objectMapper
                .readValue(json, ProductInformationRequest.class);

        assertThat(deserializedParameters.getProductType()).isEqualTo(ProductType.MORTGAGE);
        assertThat(deserializedParameters.getParameters())
                .isNotNull()
                .isNotEmpty()
                .containsKey(FetchProductInformationParameterKey.MORTGAGE_AMOUNT);
        assertThat(deserializedParameters.getParameters().get(FetchProductInformationParameterKey.MORTGAGE_AMOUNT))
                .isEqualTo(1000000.00);
    }

    @Test
    public void serializedJsonUsesEnumKeysInsteadOfName() throws JsonProcessingException {
        // Setup data
        ProductInformationRequest productInformationParameters = new ProductInformationRequest(
                new User(),
                new Provider(),
                ProductType.MORTGAGE,
                UUID.randomUUID(),
                Maps.newHashMap(ImmutableMap.of(
                        FetchProductInformationParameterKey.MORTGAGE_AMOUNT, (Object)1000000.00)));

        // Mapper for conversion
        ObjectMapper objectMapper = new ObjectMapper();

        // Serialize
        String json = objectMapper.writeValueAsString(productInformationParameters);

        // Check enum key serialization
        assertThat(json).contains(ProductType.MORTGAGE.name());

        assertThat(json)
                .doesNotContain(FetchProductInformationParameterKey.MORTGAGE_AMOUNT.name())
                .contains(FetchProductInformationParameterKey.MORTGAGE_AMOUNT.getKey());
    }
}
