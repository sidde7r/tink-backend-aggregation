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
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
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
import se.tink.libraries.encryptedpayload.EncryptedPayloadV1;
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
                        new FakeCacheClient(), mock(ControllerWrapper.class), fakeCryptoWrapper);

        credentials = new Credentials();
        credentials.addSensitivePayload(
                ImmutableMap.of("superSecret", "secret payload", "sessionID", "ab2b348c7218bafe3"));
        credentials.setFields(
                ImmutableMap.of(
                        USERNAME, USERNAME_VALUE,
                        PASSWORD, PASSWORD_VALUE));

        final Field username =
                Field.builder().name(USERNAME).masked(false).description("Username").build();
        final Field password =
                Field.builder().name(PASSWORD).masked(true).description("Password").build();

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
    public void v1_to_v1() {
        final User user = new User();
        user.setFlags(ImmutableList.of());

        CredentialsRequest request = new RefreshInformationRequest();
        request.setCredentials(credentials);
        request.setUser(user);
        request.setProvider(provider);

        assertTrue(crypto.encrypt(request, true));

        credentials.clearSensitiveInformation(provider);
        credentials.setSensitivePayloadAsMap(Maps.newHashMap());
        System.out.println(credentials.getSensitiveDataSerialized());

        EncryptedPayloadV1 v1 =
                SerializationUtils.deserializeFromString(
                        credentials.getSensitiveDataSerialized(), EncryptedPayloadV1.class);

        assertNotNull(v1);
        assertEquals(1, v1.getKeyId());

        assertTrue(crypto.decrypt(request));

        final Map<String, String> decryptedPayload = credentials.getSensitivePayloadAsMap();
        assertEquals("secret payload", decryptedPayload.get("superSecret"));
        assertEquals("ab2b348c7218bafe3", decryptedPayload.get("sessionID"));
        assertEquals(PASSWORD_VALUE, credentials.getField(PASSWORD));
        assertEquals(USERNAME_VALUE, credentials.getField(USERNAME));
        System.out.println(decryptedPayload);
    }
}
