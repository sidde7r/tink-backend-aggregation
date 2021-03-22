package se.tink.backend.aggregation.workers.encryption;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.BaseEncoding;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.storage.database.models.CryptoConfiguration;
import se.tink.backend.aggregation.storage.database.models.CryptoConfigurationId;
import se.tink.backend.aggregation.wrappers.CryptoWrapper;
import se.tink.libraries.cache.FakeCacheClient;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.encryptedpayload.EncryptedPayloadHead;
import se.tink.libraries.encryptedpayload.EncryptedPayloadV1;
import se.tink.libraries.encryptedpayload.EncryptedPayloadV2;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.user.rpc.User;

public class CredentialsCryptoTest {

    public static final String KEY = "nyckelnyckelnyckelnyckelnyckel32";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String USERNAME_VALUE = "britta";
    public static final String PASSWORD_VALUE = "banan456";

    private CredentialsCrypto crypto;
    private Credentials credentials;
    private Provider provider;

    @Before
    public void setUp() throws Exception {
        CryptoWrapper fakeCryptoWrapper = mock(CryptoWrapper.class);

        crypto =
                new CredentialsCrypto(
                        new FakeCacheClient(),
                        mock(ControllerWrapper.class),
                        fakeCryptoWrapper,
                        new MetricRegistry());

        credentials = new Credentials();
        credentials.addSensitivePayload(
                ImmutableMap.of("superSecret", "secret payload", "sessionID", "ab2b348c7218bafe3"));
        credentials.setFields(
                ImmutableMap.of(
                        USERNAME, USERNAME_VALUE,
                        PASSWORD, PASSWORD_VALUE));

        final Field username =
                Field.builder().name(USERNAME).sensitive(false).description("Username").build();
        final Field password =
                Field.builder().name(PASSWORD).sensitive(true).description("Password").build();

        provider = new Provider();
        provider.setName("handelsbanken-bankid");
        provider.setFields(Lists.newArrayList(username, password));

        when(fakeCryptoWrapper.getCryptoKeyByKeyId(anyInt())).thenReturn(KEY.getBytes());

        final CryptoConfiguration config = new CryptoConfiguration();
        config.setBase64encodedkey(BaseEncoding.base64().encode(KEY.getBytes()));
        config.setCryptoConfigurationId(CryptoConfigurationId.of(1, "oxford"));
        when(fakeCryptoWrapper.getLatestCryptoConfiguration()).thenReturn(Optional.of(config));
    }

    @Test
    @Ignore("CredentialsCrypto will not encrypt to v1 anymore. Keep this test for reference.")
    public void v1ToV1() {
        final User user = new User();
        user.setFlags(ImmutableList.of());

        CredentialsRequest request = requestFrom(credentials, user, provider);
        assertTrue(crypto.encrypt(request, true, StandardCharsets.UTF_8));

        final Credentials clone = credentials.clone();
        clone.clearSensitiveInformation(provider);
        clone.setSensitivePayloadAsMap(Maps.newHashMap());

        EncryptedPayloadHead head =
                SerializationUtils.deserializeFromString(
                        clone.getSensitiveDataSerialized(), EncryptedPayloadHead.class);
        assertEquals(1, head.getVersion());

        EncryptedPayloadV1 v1 =
                SerializationUtils.deserializeFromString(
                        clone.getSensitiveDataSerialized(), EncryptedPayloadV1.class);

        CredentialsRequest decryptRequest = requestFrom(clone, user, provider);
        assertNotNull(v1);
        assertEquals(1, v1.getKeyId());
        assertTrue(crypto.decrypt(decryptRequest, StandardCharsets.UTF_8));

        final Map<String, String> decryptedPayload = clone.getSensitivePayloadAsMap();
        assertEquals("secret payload", decryptedPayload.get("superSecret"));
        assertEquals("ab2b348c7218bafe3", decryptedPayload.get("sessionID"));
        assertEquals(PASSWORD_VALUE, clone.getField(PASSWORD));
        assertEquals(USERNAME_VALUE, clone.getField(USERNAME));
    }

    @Test
    public void v1ToV2Migration() {
        final User user = new User();

        CredentialsRequest request = requestFrom(credentials, user, provider);
        assertTrue(crypto.encrypt(request, true, StandardCharsets.UTF_8));

        final Credentials clone = credentials.clone();
        clone.clearSensitiveInformation(provider);
        clone.setSensitivePayloadAsMap(Maps.newHashMap());

        EncryptedPayloadHead head =
                SerializationUtils.deserializeFromString(
                        clone.getSensitiveDataSerialized(), EncryptedPayloadHead.class);
        assertEquals(2, head.getVersion());

        EncryptedPayloadV2 v2 =
                SerializationUtils.deserializeFromString(
                        clone.getSensitiveDataSerialized(), EncryptedPayloadV2.class);

        CredentialsRequest decryptRequest = requestFrom(clone, user, provider);
        assertNotNull(v2);
        assertEquals(0, v2.getKeyId());
        assertEquals(1, v2.getFields().getKeyId());
        assertEquals(1, v2.getPayload().getKeyId());
        assertTrue(crypto.decrypt(decryptRequest, StandardCharsets.UTF_8));

        final Map<String, String> decryptedPayload = clone.getSensitivePayloadAsMap();
        assertEquals("secret payload", decryptedPayload.get("superSecret"));
        assertEquals("ab2b348c7218bafe3", decryptedPayload.get("sessionID"));
        assertEquals(PASSWORD_VALUE, clone.getField(PASSWORD));
        assertEquals(USERNAME_VALUE, clone.getField(USERNAME));
    }

    @Test
    public void v2ToV2() {
        final User user = new User();
        user.setFlags(ImmutableList.of());

        CredentialsRequest request = requestFrom(credentials, user, provider);
        request.getCredentials()
                .setSensitiveDataSerialized(
                        "{\"version\":2,\"timestamp\":1599042304623,\"keyId\":1,\"fields\":"
                                + "\"{\\\"version\\\":2,\\\"timestamp\\\":\\\"2020-09-02T10:25:04.623Z\\\","
                                + "\\\"keyId\\\":1,\\\"payload\\\":\\\"{\\\\\\\"iv\\\\\\\":\\\\\\\"ibJjOEQpWfK1jWj3"
                                + "\\\\\\\",\\\\\\\"data\\\\\\\":\\\\\\\"15CoJNNmmmD5A1t5iCJujiOgsr+7k6C"
                                + "+N+iOnDoRET9RUAyz6D9S\\\\\\\"}\\\"}\",\"payload\":\"{\\n  \\\"version\\\""
                                + ": 2,\\n  \\\"timestamp\\\": \\\"2020-09-02T10:25:04.623Z\\\",\\n  "
                                + "\\\"keyId\\\": 1,\\n  \\\"payload\\\": \\\"{\\\\\\\"iv\\\\\\\":\\\\"
                                + "\\\"dim2Ijea+z8eJjFu\\\\\\\",\\\\\\\"data\\\\\\\":\\\\\\\"MzyOo6P4t"
                                + "byGEyxwYXyKMmhsZPJ0ofLawhE5z+5H5A3txrZYKHvwFPEw5rQYW/qRfBV+kaKX"
                                + "cJoTB4jqni/ceXbk9ZFFrWLgUSVSOMpJIxE=\\\\\\\"}\\\"\\n}\"}");

        EncryptedPayloadHead head =
                SerializationUtils.deserializeFromString(
                        credentials.getSensitiveDataSerialized(), EncryptedPayloadHead.class);
        assertEquals(2, head.getVersion());

        EncryptedPayloadV2 v2 =
                SerializationUtils.deserializeFromString(
                        credentials.getSensitiveDataSerialized(), EncryptedPayloadV2.class);
        assertNotNull(v2);
        assertTrue(crypto.encrypt(request, true, StandardCharsets.UTF_8));

        final Credentials clone = credentials.clone();
        clone.clearSensitiveInformation(provider);
        clone.setSensitivePayloadAsMap(Maps.newHashMap());

        CredentialsRequest decryptRequest = requestFrom(clone, user, provider);

        assertEquals(1, v2.getKeyId());
        assertTrue(crypto.decrypt(decryptRequest, StandardCharsets.UTF_8));

        final Map<String, String> decryptedPayload = clone.getSensitivePayloadAsMap();
        assertEquals("secret payload", decryptedPayload.get("superSecret"));
        assertEquals("ab2b348c7218bafe3", decryptedPayload.get("sessionID"));
        assertEquals(PASSWORD_VALUE, clone.getField(PASSWORD));
        assertEquals(USERNAME_VALUE, clone.getField(USERNAME));
    }

    private static CredentialsRequest requestFrom(
            Credentials credentials, User user, Provider provider) {
        CredentialsRequest request = new RefreshInformationRequest();
        request.setCredentials(credentials);
        request.setUser(user);
        request.setProvider(provider);
        return request;
    }
}
