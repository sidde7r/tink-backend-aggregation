package se.tink.backend.rpc.application;

import org.junit.Test;
import se.tink.backend.core.User;
import se.tink.libraries.uuid.UUIDUtils;
import static org.assertj.core.api.Assertions.assertThat;

public class GetEligibleApplicationTypesCommandTest {
    @Test
    public void testPropertyMapping() {
        User user = new User();

        GetEligibleApplicationTypesCommand command = new GetEligibleApplicationTypesCommand(user.getId());

        assertThat(command.getUserId()).isEqualTo(UUIDUtils.fromTinkUUID(user.getId()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoUser() {
        new GetEligibleApplicationTypesCommand(null);
    }
}
