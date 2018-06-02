package se.tink.libraries.abnamro.utils;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.Test;
import se.tink.backend.core.Notification;
import se.tink.backend.core.User;
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

    @Test
    public void testReplaceGripPrefix_forNonMigratedUsers() {
        String username = "abnamro-123";
        Notification notification = new Notification.Builder()
                .userId(username)
                .title("Test")
                .url("grip://open")
                .date(DateTime.now().toDate())
                .type("Test").build();

        AbnAmroLegacyUserUtils.replaceGripPrefixForLegacyUsers(username, Lists.newArrayList(notification));
        assertThat(notification.getUrl()).isEqualTo("tink://open");
    }

    @Test
    public void testDontReplaceGripPrefix_forMigratedUsers() {
        String username = "+46700000000";
        Notification notification = new Notification.Builder()
                .userId(username)
                .title("Test")
                .url("grip://open")
                .date(DateTime.now().toDate())
                .type("Test").build();

        AbnAmroLegacyUserUtils.replaceGripPrefixForLegacyUsers(username, Lists.newArrayList(notification));
        assertThat(notification.getUrl()).isEqualTo("grip://open");
    }
}
