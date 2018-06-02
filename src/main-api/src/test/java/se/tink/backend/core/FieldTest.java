package se.tink.backend.core;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Enclosed.class)
public class FieldTest {
    /**
     * Tests that make sure we never change these keys without notice
     */
    public static class KeyTest {
        @Test
        public void userName_hasCorrectKey() {
            assertThat(
                    Field.Key.USERNAME.getFieldKey())
                    .isEqualTo("username");
        }

        @Test
        public void password_hasCorrectKey() {
            assertThat(
                    Field.Key.PASSWORD.getFieldKey())
                    .isEqualTo("password");
        }

        @Test
        public void additionalInformation_hasCorrectKey() {
            assertThat(
                    Field.Key.ADDITIONAL_INFORMATION.getFieldKey())
                    .isEqualTo("additionalInformation");
        }

        @Test
        public void persistentLoginSessionName_hasCorrectKey() {
            assertThat(
                    Field.Key.PERSISTENT_LOGIN_SESSION_NAME.getFieldKey())
                    .isEqualTo("persistent-login-session");
        }
    }
}