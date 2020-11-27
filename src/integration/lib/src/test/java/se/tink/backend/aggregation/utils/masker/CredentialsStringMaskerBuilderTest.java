package se.tink.backend.aggregation.utils.masker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
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

    public static final String PASSWORD = "abc123!";
    public static final String ESCAPED_PASSWORD = "abc123%21";
    public static final String USER_ID = "ööö";
    public static final String USERNAME = "user@test.se";
    public static final String ESCAPED_USERNAME = "user%40test.se";
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
                        .put("key3", "https://www.swedbank.se")
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

        assertThat(
                        stringMasker.getValuesToMask().stream()
                                .map(Pattern::toString)
                                .collect(ImmutableList.toImmutableList()))
                .contains(PASSWORD, ESCAPED_PASSWORD);
    }

    @Test
    public void testApplyWithUserName() {
        CredentialsStringMaskerBuilder stringMasker =
                new CredentialsStringMaskerBuilder(
                        mockCredentials(),
                        ImmutableList.of(
                                CredentialsStringMaskerBuilder.CredentialsProperty.USERNAME));

        assertThat(
                        stringMasker.getValuesToMask().stream()
                                .map(Pattern::toString)
                                .collect(ImmutableList.toImmutableList()))
                .containsExactly(ESCAPED_USERNAME, USERNAME);
    }

    @Test
    public void testApplyWithAllProperties() {
        CredentialsStringMaskerBuilder credentialsStringMaskerBuilder =
                new CredentialsStringMaskerBuilder(
                        mockCredentials(),
                        ImmutableList.copyOf(
                                CredentialsStringMaskerBuilder.CredentialsProperty.values()));

        try {
            List<String> sensitiveValuesToCompareSorted =
                    getSensitiveValuesToCompareSorted(SENSITIVE_PAYLOAD, PASSWORD, USERNAME);

            assertThat(
                            credentialsStringMaskerBuilder.getValuesToMask().stream()
                                    .map(Pattern::toString)
                                    .collect(ImmutableList.toImmutableList()))
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
            List<String> sensitiveValuesToCompareSorted =
                    getSensitiveValuesToCompareSorted(SENSITIVE_PAYLOAD);
            assertThat(
                            credentialsStringMaskerBuilder.getValuesToMask().stream()
                                    .map(Pattern::toString)
                                    .collect(ImmutableList.toImmutableList()))
                    .containsExactly(
                            sensitiveValuesToCompareSorted.toArray(
                                    new String[sensitiveValuesToCompareSorted.size()]));
        } catch (IOException e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void testApplyWithNestedSensitivePayload() {
        ImmutableMap<String, String> nestedSensitivePayload = assembleNestedSensitivePayload();

        Credentials nestedMockCredentials = mock(Credentials.class);
        when(nestedMockCredentials.getSensitivePayloadAsMap()).thenReturn(nestedSensitivePayload);

        CredentialsStringMaskerBuilder credentialsStringMaskerBuilder =
                new CredentialsStringMaskerBuilder(
                        nestedMockCredentials,
                        ImmutableList.of(
                                CredentialsStringMaskerBuilder.CredentialsProperty
                                        .SENSITIVE_PAYLOAD));

        try {
            List<String> sensitiveValuesToCompareSorted =
                    getSensitiveValuesToCompareSorted(nestedSensitivePayload);
            assertThat(
                            credentialsStringMaskerBuilder.getValuesToMask().stream()
                                    .map(Pattern::toString)
                                    .collect(ImmutableList.toImmutableList()))
                    .containsExactly(
                            sensitiveValuesToCompareSorted.toArray(
                                    new String[sensitiveValuesToCompareSorted.size()]));
        } catch (IOException e) {
            Assert.fail(e.toString());
        }
    }

    private ImmutableMap<String, String> assembleNestedSensitivePayload() {
        PersistentStorage persistentStorage = new PersistentStorage();
        final ImmutableMap<String, String> sessionStorage =
                ImmutableMap.<String, String>builder()
                        .put("secret1", "sessionsecret1")
                        .put("secret2", "sessionsecret2")
                        .build();
        ImmutableMap<String, String> nestedSensitivePayload;

        PersistentStorage nestedPersistentStorage = new PersistentStorage();
        nestedPersistentStorage.put("secret3", "bebebebe");
        nestedPersistentStorage.put(
                "nestedToken",
                OAuth2Token.create(
                        "nestedTestType",
                        "nestedTestAccessToken",
                        "nestedTestRefreshToken",
                        9000,
                        12345));

        persistentStorage.put("secret1", "qweqweqwe");
        persistentStorage.put("secret2", "asdasdasd");
        persistentStorage.put(
                "token",
                OAuth2Token.create("testType", "testAccessToken", "testRefreshToken", 900, 1234));
        persistentStorage.put("nestedPersistentStorage", nestedPersistentStorage);
        nestedSensitivePayload =
                ImmutableMap.<String, String>builder()
                        .put("key1", "value1")
                        .put("key2", "value2")
                        .put("key3", "https://www.swedbank.se")
                        .put(
                                "key4",
                                "AmexSession type=\"AmexSession\",value=\"eyJzZWN1cml0eXNlc3Npb25kYXRhIjpbeyJibHVlYm94dmFsdWVzIjoiZjUzMjRmYzEtNWRlNDdkMjYtOWQyYjE3OTktZmY1ZTJmMWYifSx7InRlbXBzZXNzaW9uIjoiIn0seyJhbWV4c2Vzc2lvbmNvb2tpZSI6IiJ9LHsiVXNlcklEIjoidmluaGtoYTE0In1dLCJleHBlY3RlZCI6WyJTZWNvbmRhcnlBdXRoZW50aWNhdGlvbkNvb2tpZSIsInNhYyJdLCJhZGRpdGlvbmFsc2Vzc2lvbmRhdGEiOlt7ImdhdGVrZWVwZXIiOiIwMjYxN0Q4NzBBNTNFRDdCNjFBMzYzOTQwMTczNTkwNTIzM0FDNDZFNjAyRDlENEM4NTJCNDE0MTYyOEM5OUIyMjgwNDU1Qjc3NTA1NTAzNkFGRDI5REZGRUQ5QTJFQUVFQjYxRUFDODBBNj\"")
                        .put(
                                Key.PERSISTENT_STORAGE.getFieldKey(),
                                Objects.requireNonNull(
                                        SerializationUtils.serializeToString(persistentStorage)))
                        .put(
                                Key.SESSION_STORAGE.getFieldKey(),
                                Objects.requireNonNull(
                                        SerializationUtils.serializeToString(sessionStorage)))
                        .build();
        return nestedSensitivePayload;
    }

    private List<String> getSensitiveValuesToCompareSorted(
            ImmutableMap<String, String> sensitivePayload, String... extraValues)
            throws IOException {
        String sensitivePayloadAsString = OBJECT_MAPPER.writeValueAsString(sensitivePayload);
        Map<String, String> sensitiveValuesOriginalMap =
                JsonFlattener.flattenJsonToMap(sensitivePayloadAsString);
        Set<String> sensitiveValuesToCompare = new HashSet<>(sensitiveValuesOriginalMap.values());
        Arrays.stream(extraValues).forEach(sensitiveValuesToCompare::add);
        List<String> escapedValues =
                sensitiveValuesToCompare.stream()
                        .map(this::getEscapedString)
                        .filter(s -> !sensitiveValuesToCompare.contains(s))
                        .collect(Collectors.toList());

        sensitiveValuesToCompare.addAll(escapedValues);
        return sensitiveValuesToCompare.stream()
                .sorted(MaskingConstants.SENSITIVE_VALUES_SORTING_COMPARATOR)
                .collect(Collectors.toList());
    }

    private String getEscapedString(final String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Credentials mockCredentials() {

        Credentials mock = mock(Credentials.class);
        when(mock.getPassword()).thenReturn(PASSWORD);
        when(mock.getSensitivePayloadAsMap()).thenReturn(SENSITIVE_PAYLOAD);
        when(mock.getUserId()).thenReturn(USER_ID);
        when(mock.getUsername()).thenReturn(USERNAME);

        return mock;
    }
}
