package se.tink.backend.aggregation.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Objects;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CredentialsStringMaskerTest {

    public static final String PASSWORD = "abc123";
    public static final String USER_ID = "ööö";
    public static final String USERNAME = "user@test.se";
    public static PersistentStorage persistentStorage;
    public static final ImmutableMap<String, String> sessionStorage =
            ImmutableMap.<String, String>builder()
                    .put("secret1", "sessionsecret1")
                    .put("secret2", "sessionsecret2")
                    .build();
    public static ImmutableMap<String, String> SENSITIVE_PAYLOAD;

    @Before
    public void setup() {
        persistentStorage = new PersistentStorage();
        persistentStorage.put("secret1", "qweqweqwe");
        persistentStorage.put("secret2", "asdasdasd");
        persistentStorage.put(
                "token",
                OAuth2Token.create("testType", "testAccessToken", "testRefreshToken", 900, 1234));
        SENSITIVE_PAYLOAD =
                ImmutableMap.<String, String>builder()
                        .put("key1", "value1")
                        .put("key2", "value2")
                        .put(
                                Key.PERSISTENT_STORAGE.getFieldKey(),
                                Objects.requireNonNull(
                                        SerializationUtils.serializeToString(persistentStorage)))
                        .put(
                                Key.SESSION_STORAGE.getFieldKey(),
                                Objects.requireNonNull(
                                        SerializationUtils.serializeToString(sessionStorage)))
                        .build();
    }

    @Test
    public void testApplyWithPassword() {
        CredentialsStringMasker stringMasker =
                new CredentialsStringMasker(
                        mockCredentials(),
                        ImmutableList.of(CredentialsStringMasker.CredentialsProperty.PASSWORD));

        String masked = stringMasker.getMasked("test me " + PASSWORD + " for some data");

        assertThat(masked).doesNotContain(PASSWORD);
    }

    @Test
    public void testApplyWithUserName() {
        CredentialsStringMasker stringMasker =
                new CredentialsStringMasker(
                        mockCredentials(),
                        ImmutableList.of(CredentialsStringMasker.CredentialsProperty.USERNAME));

        String masked = stringMasker.getMasked("test me " + USERNAME + " for some data");

        assertThat(masked).doesNotContain(USERNAME);
    }

    @Test
    public void testApplyWithAllProperties() {
        CredentialsStringMasker stringMasker =
                new CredentialsStringMasker(
                        mockCredentials(),
                        ImmutableList.copyOf(CredentialsStringMasker.CredentialsProperty.values()));

        String unmasked =
                "username: "
                        + USERNAME
                        + ", userid: "
                        + USER_ID
                        + ", password: "
                        + PASSWORD
                        + ", sensitive: "
                        + "value1asdasdvalue2"
                        + "qweqweqwe asdasdasdaoeiraoefjioaejaoifjsessionsecret1asodjaojefojioaefojasessionsecret2"
                        + "asdas testAccessToken testRefreshToken testType";

        String masked = stringMasker.getMasked(unmasked);
        assertThat(masked).contains(USER_ID);
        assertThat(masked).doesNotContain(USERNAME);
        assertThat(masked).doesNotContain(PASSWORD);
        for (String value : SENSITIVE_PAYLOAD.values()) {
            assertThat(masked).doesNotContain(value);
        }

        for (String secret : persistentStorage.values()) {
            assertThat(masked).doesNotContain(secret);
        }

        assertThat(masked).doesNotContain("testType");
        assertThat(masked).doesNotContain("testAccessToken");
        assertThat(masked).doesNotContain("testRefreshToken");

        for (String secret : sessionStorage.values()) {
            assertThat(masked).doesNotContain(secret);
        }
    }

    @Test
    public void testApplyWithSensitivePayload() {
        CredentialsStringMasker stringMasker =
                new CredentialsStringMasker(
                        mockCredentials(),
                        ImmutableList.of(
                                CredentialsStringMasker.CredentialsProperty.SENSITIVE_PAYLOAD));

        String masked =
                stringMasker.getMasked(
                        "test me " + SENSITIVE_PAYLOAD.toString() + " for some data");

        for (String value : SENSITIVE_PAYLOAD.values()) {
            assertThat(masked).doesNotContain(value);
        }
    }

    @Test
    public void testThatWeDontRemoveAnythingButMaskedProperty() {
        CredentialsStringMasker stringMasker =
                new CredentialsStringMasker(
                        mockCredentials(),
                        ImmutableList.of(CredentialsStringMasker.CredentialsProperty.USERNAME));

        String masked = stringMasker.getMasked("test me " + USERNAME + " for some data");

        assertThat(masked).isEqualTo("test me ***MASKED*** for some data");
    }

    private static Credentials mockCredentials() {

        Credentials mock = mock(Credentials.class);
        when(mock.getPassword()).thenReturn(PASSWORD);
        when(mock.getSensitivePayload()).thenReturn(SENSITIVE_PAYLOAD);
        when(mock.getUserId()).thenReturn(USER_ID);
        when(mock.getUsername()).thenReturn(USERNAME);

        return mock;
    }
}
