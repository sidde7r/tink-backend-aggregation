package se.tink.backend.rpc.application;

import java.util.Optional;
import java.util.UUID;
import org.junit.Test;
import se.tink.backend.core.User;
import se.tink.libraries.uuid.UUIDUtils;
import static org.assertj.core.api.Assertions.assertThat;

public class SubmitApplicationCommandTest {
    private final static String userAgent = "Tink/4.0.0 (iOS; 8.1, iPhone Simulator)";
    private final static String ip = "127.0.0.1";

    @Test
    public void testPropertyMapping() {
        final User user = new User();
        final UUID applicationId = UUID.randomUUID();

        SubmitApplicationCommand command = new SubmitApplicationCommand(applicationId.toString(), user,
                Optional.of(userAgent), Optional.of(ip));

        assertThat(command.getUser()).isEqualTo(user);
        assertThat(command.getUserId()).isEqualTo(UUIDUtils.fromTinkUUID(user.getId()));
        assertThat(command.getApplicationId()).isEqualTo(applicationId);
        assertThat(command.getTinkUserAgent()).isNotNull();
        assertThat(command.getRemoteIp().orElse(null)).isEqualTo(ip);
    }

    @Test(expected = NullPointerException.class)
    public void testNoUser() {
        new SubmitApplicationCommand(UUID.randomUUID().toString(), null, Optional.of(userAgent), Optional.of(ip));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoApplicationId() {
        new SubmitApplicationCommand(null, new User(), Optional.of(userAgent), Optional.of(ip));
    }
}
