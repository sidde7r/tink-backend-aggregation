package se.tink.backend.aggregation.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.JsonFlattener;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CredentialsStringMaskerBuilderTest {

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
    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

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
                        .put("key3", "value3")
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
        CredentialsStringMaskerBuilder stringMasker =
                new CredentialsStringMaskerBuilder(
                        mockCredentials(),
                        ImmutableList.of(
                                CredentialsStringMaskerBuilder.CredentialsProperty.PASSWORD));

        assertThat(stringMasker.getValuesToMask()).containsExactly(PASSWORD);
    }

    @Test
    public void testApplyWithUserName() {
        CredentialsStringMaskerBuilder stringMasker =
                new CredentialsStringMaskerBuilder(
                        mockCredentials(),
                        ImmutableList.of(
                                CredentialsStringMaskerBuilder.CredentialsProperty.USERNAME));

        assertThat(stringMasker.getValuesToMask()).containsExactly(USERNAME);
    }

    @Test
    public void testApplyWithAllProperties() {
        CredentialsStringMaskerBuilder credentialsStringMaskerBuilder =
                new CredentialsStringMaskerBuilder(
                        mockCredentials(),
                        ImmutableList.copyOf(
                                CredentialsStringMaskerBuilder.CredentialsProperty.values()));

        try {
            String sensitivePayloadAsString = OBJECT_MAPPER.writeValueAsString(SENSITIVE_PAYLOAD);
            Map<String, String> sensitiveValuesOriginalMap =
                    JsonFlattener.flattenJsonToMap(
                            JsonFlattener.ROOT_PATH,
                            OBJECT_MAPPER.readTree(sensitivePayloadAsString));
            List<String> sensitiveValuesToCompare =
                    new ArrayList<>(sensitiveValuesOriginalMap.values());
            sensitiveValuesToCompare.add(PASSWORD);
            sensitiveValuesToCompare.add(USERNAME);
            List<String> sensitiveValuesToCompareSorted =
                    sensitiveValuesToCompare.stream()
                            .sorted(
                                    Comparator.comparing(String::length)
                                            .reversed()
                                            .thenComparing(Comparator.naturalOrder()))
                            .collect(Collectors.toList());
            assertThat(credentialsStringMaskerBuilder.getValuesToMask())
                    .containsExactly(
                            sensitiveValuesToCompareSorted.toArray(
                                    new String[sensitiveValuesToCompareSorted.size()]));
        } catch (IOException e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void testApplyWithSensitivePayload() {
        CredentialsStringMaskerBuilder credentialsStringMaskerBuilder =
                new CredentialsStringMaskerBuilder(
                        mockCredentials(),
                        ImmutableList.of(
                                CredentialsStringMaskerBuilder.CredentialsProperty
                                        .SENSITIVE_PAYLOAD));

        try {
            String sensitivePayloadAsString = OBJECT_MAPPER.writeValueAsString(SENSITIVE_PAYLOAD);
            Map<String, String> sensitiveValuesOriginalMap =
                    JsonFlattener.flattenJsonToMap(
                            JsonFlattener.ROOT_PATH,
                            OBJECT_MAPPER.readTree(sensitivePayloadAsString));
            List<String> sensitiveValuesToCompare =
                    new ArrayList<>(sensitiveValuesOriginalMap.values());
            List<String> sensitiveValuesToCompareSorted =
                    sensitiveValuesToCompare.stream()
                            .sorted(
                                    Comparator.comparing(String::length)
                                            .reversed()
                                            .thenComparing(Comparator.naturalOrder()))
                            .collect(Collectors.toList());
            assertThat(credentialsStringMaskerBuilder.getValuesToMask())
                    .containsExactly(
                            sensitiveValuesToCompareSorted.toArray(
                                    new String[sensitiveValuesToCompareSorted.size()]));
        } catch (IOException e) {
            Assert.fail(e.toString());
        }
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
