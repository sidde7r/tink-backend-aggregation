package se.tink.backend.aggregation.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CredentialsStringMaskerTest {

    public static final String PASSWORD = "abc123";
    public static final String USER_ID = "ööö";
    public static final String USERNAME = "user@test.se";
    public static final ImmutableMap<String, String> SENSITIVE_PAYLOAD = ImmutableMap.<String, String>builder()
            .put("key1", "value1")
            .put("key2", "value2")
            .build();

    @Test
    public void testApplyWithPassword() {
        CredentialsStringMasker stringMasker = new CredentialsStringMasker(mockCredentials(),
                ImmutableList.of(CredentialsStringMasker.CredentialsProperty.PASSWORD));

        String masked = stringMasker.getMasked("test me " + PASSWORD + " for some data");

        assertThat(masked).doesNotContain(PASSWORD);
    }

    @Test
    public void testApplyWithUserName() {
        CredentialsStringMasker stringMasker = new CredentialsStringMasker(mockCredentials(),
                ImmutableList.of(CredentialsStringMasker.CredentialsProperty.USERNAME));

        String masked = stringMasker.getMasked("test me " + USERNAME + " for some data");

        assertThat(masked).doesNotContain(USERNAME);
    }

    @Test
    public void testApplyWithAllProperties() {
        CredentialsStringMasker stringMasker = new CredentialsStringMasker(mockCredentials(),
                ImmutableList.copyOf(CredentialsStringMasker.CredentialsProperty.values()));

        String unmasked = "username: " + USERNAME +
                ", userid: " + USER_ID +
                ", password: " + PASSWORD +
                ", sensitive: " + SENSITIVE_PAYLOAD.toString();

        String masked = stringMasker.getMasked(unmasked);
        assertThat(masked).contains(USER_ID);
        assertThat(masked).doesNotContain(USERNAME);
        assertThat(masked).doesNotContain(PASSWORD);
        for (String value : SENSITIVE_PAYLOAD.values()) {
            assertThat(masked).doesNotContain(value);
        }
    }

    @Test
    public void testApplyWithSensitivePayload() {
        CredentialsStringMasker stringMasker = new CredentialsStringMasker(mockCredentials(),
                ImmutableList.of(CredentialsStringMasker.CredentialsProperty.SENSITIVE_PAYLOAD));

        String masked = stringMasker.getMasked("test me " + SENSITIVE_PAYLOAD.toString() + " for some data");

        for (String value : SENSITIVE_PAYLOAD.values()) {
            assertThat(masked).doesNotContain(value);
        }
    }

    @Test
    public void testThatWeDontRemoveAnythingButMaskedProperty() {
        CredentialsStringMasker stringMasker = new CredentialsStringMasker(mockCredentials(),
                ImmutableList.of(CredentialsStringMasker.CredentialsProperty.USERNAME));

        String masked = stringMasker.getMasked("test me " + USERNAME + " for some data");

        assertThat(masked).isEqualTo("test me ***MASKED*** for some data");
    }

    private static Credentials mockCredentials() {

        Credentials mock = mock(Credentials.class);
        when(mock.getPassword())
                .thenReturn(PASSWORD);
        when(mock.getSensitivePayload())
                .thenReturn(SENSITIVE_PAYLOAD);
        when(mock.getUserId())
                .thenReturn(USER_ID);
        when(mock.getUsername())
                .thenReturn(USERNAME);

        return mock;
    }
}
