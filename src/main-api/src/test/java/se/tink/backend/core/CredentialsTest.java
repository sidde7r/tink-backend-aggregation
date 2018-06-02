package se.tink.backend.core;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class CredentialsTest {
    @Test
    public void getField_WithEnumKey() {
        ImmutableMap<String, String> fields = ImmutableMap.of("additionalInformation", "Info");

        Credentials credentials = new Credentials();
        credentials.setFields(fields);

        assertThat(credentials.getField(Field.Key.ADDITIONAL_INFORMATION)).isEqualTo("Info");
    }

    @Test
    public void setField_WithEnumKey() {
        Credentials credentials = new Credentials();
        credentials.setField(Field.Key.ADDITIONAL_INFORMATION, "Info");

        assertThat(credentials.getFields()).containsKey("additionalInformation");
        assertThat(credentials.getFields().get("additionalInformation")).isEqualTo("Info");
    }

    @Test
    public void getSensitivePayload_WithEnumKey() {
        ImmutableMap<String, String> sensitiveFields = ImmutableMap.of("persistent-login-session", "SessionName");

        Credentials credentials = new Credentials();
        credentials.setSensitivePayload(sensitiveFields);

        assertThat(credentials.getSensitivePayload(Field.Key.PERSISTENT_LOGIN_SESSION_NAME)).isEqualTo("SessionName");
    }

    @Test
    public void setSensitivePayload_WithEnumKey() {
        Credentials credentials = new Credentials();
        credentials.setSensitivePayload(Field.Key.PERSISTENT_LOGIN_SESSION_NAME, "SessionName");

        assertThat(credentials.getSensitivePayload()).containsKey("persistent-login-session");
        assertThat(credentials.getSensitivePayload().get("persistent-login-session")).isEqualTo("SessionName");
    }

    @Test
    public void getPersistentSession() {
        Credentials credentials = new Credentials();
        credentials.setSensitivePayload(ImmutableMap.of("persistent-login-session", "1234"));

        assertThat(credentials.getPersistentSession(Integer.class)).isEqualTo(1234);
    }

    @Test
    public void setPersistentSession() {
        Credentials credentials = new Credentials();
        credentials.setPersistentSession(1234);

        assertThat(credentials.getSensitivePayload().get("persistent-login-session")).isEqualTo("1234");
    }

    @Test
    @Deprecated // Keep a test for this until we decide to remove so that agents will continue to work
    public void getPassword() {
        Credentials credentials = new Credentials();
        credentials.setFields(ImmutableMap.of("password", "12345"));

        assertThat(credentials.getPassword()).isEqualTo("12345");
    }

    @Test
    @Deprecated // Keep a test for this until we decide to remove so that agents will continue to work
    public void setPassword() {
        Credentials credentials = new Credentials();
        credentials.setPassword("12345");

        assertThat(credentials.getFields().get("password")).isEqualTo("12345");
    }

    @Test
    @Deprecated // Keep a test for this until we decide to remove so that agents will continue to work
    public void getUsername() {
        Credentials credentials = new Credentials();
        credentials.setFields(ImmutableMap.of("username", "MyUser"));

        assertThat(credentials.getUsername()).isEqualTo("MyUser");
    }

    @Test
    @Deprecated // Keep a test for this until we decide to remove so that agents will continue to work
    public void setUsername() {
        Credentials credentials = new Credentials();
        credentials.setUsername("MyUser");

        assertThat(credentials.getFields().get("username")).isEqualTo("MyUser");
    }

    @Test
    @Deprecated // Keep a test for this until we decide to remove so that agents will continue to work
    public void getAdditionalInformation() {
        Credentials credentials = new Credentials();
        credentials.setFields(ImmutableMap.of("additionalInformation", "Info"));

        assertThat(credentials.getAdditionalInformation()).isEqualTo("Info");
    }

    @Test
    @Deprecated // Keep a test for this until we decide to remove so that agents will continue to work
    public void setAdditionalInformation() {
        Credentials credentials = new Credentials();
        credentials.setAdditionalInformation("Info");

        assertThat(credentials.getFields().get("additionalInformation")).isEqualTo("Info");
    }
}