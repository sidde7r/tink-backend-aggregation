package se.tink.backend.rpc.application;

import java.util.Optional;
import java.util.UUID;
import org.junit.Test;
import se.tink.backend.core.User;
import se.tink.libraries.uuid.UUIDUtils;
import static org.assertj.core.api.Assertions.assertThat;

public class GetApplicationCommandTest {
    private final static String userAgent = "Tink/4.0.0 (iOS; 8.1, iPhone Simulator)";

    @Test
    public void testPropertyMapping() {
        final User user = new User();
        final UUID applicationId = UUID.randomUUID();

        GetApplicationCommand command = new GetApplicationCommand(applicationId.toString(), user,
                Optional.of(userAgent));

        assertThat(command.getUserId()).isEqualTo(UUIDUtils.fromTinkUUID(user.getId()));
        assertThat(command.getApplicationId()).isEqualTo(applicationId);
    }
}
