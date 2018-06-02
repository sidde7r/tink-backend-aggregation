package se.tink.backend.core.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Test;
import se.tink.backend.core.enums.ApplicationStatusKey;
import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationStateTest {
    @Test
    public void serializeDeserialize_withNewStatus() throws IOException {
        ApplicationState applicationState = new ApplicationState(ApplicationStatusKey.SIGNED);
        applicationState.setApplicationProperty(ApplicationPropertyKey.EXTERNAL_STATUS, "Hey!");

        ObjectMapper objectMapper = new ObjectMapper();

        String json = objectMapper.writeValueAsString(applicationState);

        ApplicationState deserialized = objectMapper.readValue(json, ApplicationState.class);
        assertThat(deserialized.getNewApplicationStatus().orElse(null)).isEqualTo(ApplicationStatusKey.SIGNED);
        assertThat(deserialized.getApplicationProperties())
                .containsEntry(ApplicationPropertyKey.EXTERNAL_STATUS, "Hey!");
    }

    @Test
    public void serializeDeserialize_noNewStatus() throws IOException {
        ApplicationState applicationState = new ApplicationState();
        applicationState.setApplicationProperty(ApplicationPropertyKey.EXTERNAL_STATUS, "Hey!");

        ObjectMapper objectMapper = new ObjectMapper();

        String json = objectMapper.writeValueAsString(applicationState);

        ApplicationState deserialized = objectMapper.readValue(json, ApplicationState.class);
        assertThat(deserialized.getNewApplicationStatus().isPresent()).isFalse();
        assertThat(deserialized.getApplicationProperties())
                .containsEntry(ApplicationPropertyKey.EXTERNAL_STATUS, "Hey!");
    }

    @Test
    public void containsCorrectEnumKeys() throws JsonProcessingException {
        ApplicationState applicationState = new ApplicationState(ApplicationStatusKey.SIGNED);
        applicationState.setApplicationProperty(ApplicationPropertyKey.EXTERNAL_STATUS, "Hey!");

        ObjectMapper objectMapper = new ObjectMapper();

        String json = objectMapper.writeValueAsString(applicationState);

        assertThat(json)
                .doesNotContain(ApplicationPropertyKey.EXTERNAL_STATUS.name())
                .contains(ApplicationPropertyKey.EXTERNAL_STATUS.getKey());

        assertThat(json)
                .contains(ApplicationStatusKey.SIGNED.name());
    }
}
