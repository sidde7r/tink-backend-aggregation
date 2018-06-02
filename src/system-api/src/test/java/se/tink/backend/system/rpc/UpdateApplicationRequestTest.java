package se.tink.backend.system.rpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.UUID;
import org.junit.Test;
import se.tink.backend.core.application.ApplicationPropertyKey;
import se.tink.backend.core.application.ApplicationState;
import se.tink.backend.core.enums.ApplicationStatusKey;
import static org.assertj.core.api.Assertions.assertThat;

public class UpdateApplicationRequestTest {
    @Test
    public void serializeDeserialize() throws IOException {
        // Setup data
        UUID userId = UUID.randomUUID();
        UUID credentialsId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UpdateApplicationRequest updateRequest = new UpdateApplicationRequest(
                userId,
                credentialsId,
                applicationId,
                new ApplicationState(
                        ApplicationStatusKey.SIGNED,
                        Maps.newHashMap(
                                ImmutableMap.of(ApplicationPropertyKey.EXTERNAL_STATUS, (Object) "Test Status"))));

        // Mapper for conversion
        ObjectMapper objectMapper = new ObjectMapper();

        // Serialize
        String json = objectMapper.writeValueAsString(updateRequest);

        // Deserialize
        UpdateApplicationRequest deserializedParameters = objectMapper
                .readValue(json, UpdateApplicationRequest.class);

        assertThat(deserializedParameters.getApplicationId()).isEqualTo(applicationId);
        assertThat(deserializedParameters.getApplicationState()).isNotNull();
        assertThat(deserializedParameters.getApplicationState().getApplicationProperties())
                .isNotNull()
                .isNotEmpty()
                .containsEntry(ApplicationPropertyKey.EXTERNAL_STATUS, "Test Status");
    }

    @Test
    public void serializedJsonUsesEnumKeysInsteadOfName() throws JsonProcessingException {
        // Setup data
        UUID userId = UUID.randomUUID();
        UUID credentialsId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UpdateApplicationRequest updateRequest = new UpdateApplicationRequest(
                userId,
                credentialsId,
                applicationId,
                new ApplicationState(
                        ApplicationStatusKey.SIGNED,
                        Maps.newHashMap(
                                ImmutableMap.of(ApplicationPropertyKey.EXTERNAL_STATUS, (Object) "Test Status"))));

        // Mapper for conversion
        ObjectMapper objectMapper = new ObjectMapper();

        // Serialize
        String json = objectMapper.writeValueAsString(updateRequest);

        // Check enum key serialization
        assertThat(json).contains(ApplicationStatusKey.SIGNED.name());

        assertThat(json)
                .doesNotContain(ApplicationPropertyKey.EXTERNAL_STATUS.name())
                .contains(ApplicationPropertyKey.EXTERNAL_STATUS.getKey());
    }
}
