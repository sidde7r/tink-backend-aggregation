package se.tink.backend.rpc.application;

import java.util.UUID;
import org.junit.Test;
import se.tink.libraries.uuid.UUIDUtils;
import static org.assertj.core.api.Assertions.assertThat;

public class DeleteApplicationCommandTest {

    @Test
    public void testUUIDv4() {
        final UUID userId = UUID.randomUUID();
        final UUID applicationId = UUID.randomUUID();

        DeleteApplicationCommand command = new DeleteApplicationCommand(userId.toString(), applicationId.toString());

        assertThat(command.getUserId()).isEqualTo(userId);
        assertThat(command.getApplicationId()).isEqualTo(applicationId);
    }

    @Test
    public void testTinkUUIDs() {
        final UUID userId = UUID.randomUUID();
        final UUID applicationId = UUID.randomUUID();

        DeleteApplicationCommand command = new DeleteApplicationCommand(UUIDUtils.toTinkUUID(userId),
                UUIDUtils.toTinkUUID(applicationId));

        assertThat(command.getUserId()).isEqualTo(userId);
        assertThat(command.getApplicationId()).isEqualTo(applicationId);
    }
}
