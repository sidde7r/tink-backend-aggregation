package se.tink.backend.rpc.application;

import java.util.Optional;
import java.util.UUID;
import org.junit.Test;
import se.tink.backend.core.User;
import se.tink.libraries.uuid.UUIDUtils;
import static org.assertj.core.api.Assertions.assertThat;

public class SubmitApplicationFormCommandTest {
    private final static String userAgent = "Tink/4.0.0 (iOS; 8.1, iPhone Simulator)";

    @Test
    public void testPropertyMapping() {
        final User user = new User();
        final UUID applicationId = UUID.randomUUID();

        SubmitApplicationFormCommand command = new SubmitApplicationFormCommand(applicationId.toString(), user,
                Optional.of(userAgent));

        assertThat(command.getUser()).isEqualTo(user);
        assertThat(command.getUserId()).isEqualTo(UUIDUtils.fromTinkUUID(user.getId()));
        assertThat(command.getApplicationId()).isEqualTo(applicationId);
        assertThat(command.getTinkUserAgent()).isNotNull();
    }

    @Test(expected = NullPointerException.class)
    public void testNoUser() {
        new SubmitApplicationFormCommand(UUID.randomUUID().toString(), null, Optional.of(userAgent));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoApplicationId() {
        new SubmitApplicationFormCommand(null, new User(), Optional.of(userAgent));
    }
}
