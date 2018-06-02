package se.tink.backend.rpc.credentials;

import java.util.Map;
import org.assertj.core.util.Maps;
import org.junit.Test;
import se.tink.backend.core.SupplementalStatus;
import static org.assertj.core.api.Assertions.assertThat;

public class SupplementalInformationCommandTest {

    @Test
    public void testSupplementalInformationAsString() {
        SupplementalInformationCommand command = SupplementalInformationCommand.builder()
                .withUserId("userId")
                .withCredentialsId("credentialsId")
                .withSupplementalInformation("supplemental-information")
                .build();

        assertThat(command.getUserId()).isEqualTo("userId");
        assertThat(command.getCredentialsId()).isEqualTo("credentialsId");
        assertThat(command.getSupplementalInformation()).isEqualTo("supplemental-information");
    }

    @Test
    public void testSupplementalInformationAsMap() {
        Map<String, String> fields = Maps.newHashMap();
        fields.put("big", "biceps");

        SupplementalInformationCommand command = SupplementalInformationCommand.builder()
                .withUserId("userId")
                .withCredentialsId("credentialsId")
                .withSupplementalInformation(fields)
                .build();

        assertThat(command.getUserId()).isEqualTo("userId");
        assertThat(command.getCredentialsId()).isEqualTo("credentialsId");
        assertThat(command.getSupplementalInformation()).isEqualTo("{\"big\":\"biceps\"}");
    }

    @Test
    public void testStatusCancelled() {
        SupplementalInformationCommand command = SupplementalInformationCommand.builder()
                .withUserId("userId")
                .withCredentialsId("credentialsId")
                .withSupplementalInformation("supplemental-information")
                .withStatus(SupplementalStatus.CANCELLED)
                .build();

        assertThat(command.getUserId()).isEqualTo("userId");
        assertThat(command.getCredentialsId()).isEqualTo("credentialsId");
        assertThat(command.getSupplementalInformation()).isNull();
    }

    @Test
    public void testStatusOk() {
        Map<String, String> fields = Maps.newHashMap();
        fields.put("big", "triceps");

        SupplementalInformationCommand command = SupplementalInformationCommand.builder()
                .withUserId("userId")
                .withCredentialsId("credentialsId")
                .withSupplementalInformation(fields)
                .withStatus(SupplementalStatus.OK)
                .build();

        assertThat(command.getUserId()).isEqualTo("userId");
        assertThat(command.getCredentialsId()).isEqualTo("credentialsId");
        assertThat(command.getSupplementalInformation()).isEqualTo("{\"big\":\"triceps\"}");
    }

    @Test(expected = IllegalStateException.class)
    public void testMissingUserId() {
        SupplementalInformationCommand.builder()
                .withUserId(null)
                .withCredentialsId("credentialsId")
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void testMissingCredentialsId() {
        SupplementalInformationCommand.builder()
                .withUserId("userId")
                .withCredentialsId(null)
                .build();
    }
}
