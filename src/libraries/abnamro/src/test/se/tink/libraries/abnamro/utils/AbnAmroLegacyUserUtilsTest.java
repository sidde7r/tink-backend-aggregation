package se.tink.libraries.abnamro.utils;

import java.util.Optional;
import org.junit.Test;
import se.tink.libraries.user.rpc.User;
import static org.assertj.core.api.Assertions.assertThat;

public class AbnAmroLegacyUserUtilsTest {

    @Test
    public void testNormalBcNumber() {
        String expected = "1000010101";

        User user = new User();
        user.setUsername(AbnAmroLegacyUserUtils.getUsername(expected));

        assertThat(AbnAmroLegacyUserUtils.isValidUsername(user.getUsername())).isTrue();

        Optional<String> actual = AbnAmroLegacyUserUtils.getBcNumber(user);

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void testBcNumberWithLeadingZeros() {
        String expected = "001000010101";

        User user = new User();
        user.setUsername(AbnAmroLegacyUserUtils.getUsername(expected));

        assertThat(AbnAmroLegacyUserUtils.isValidUsername(user.getUsername())).isTrue();

        Optional<String> actual = AbnAmroLegacyUserUtils.getBcNumber(user);

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
    }
}
