package se.tink.backend.aggregation.nxgen.http_api_client.variable_detection.storage;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.libraries.aggregation_agent_api_client.src.variable.InMemoryVariableStore;
import se.tink.libraries.aggregation_agent_api_client.src.variable.VariableKey;

public class TokenDetectorTest {
    private InMemoryVariableStore variableStore;
    private final TokenDetector tokenDetector = new TokenDetector();
    private Logger logger;
    private ListAppender<ILoggingEvent> logAppender;

    @Before
    public void setup() {
        this.variableStore = new InMemoryVariableStore();
        logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logAppender = new ListAppender<>();
        logger.addAppender(logAppender);
        logAppender.start();
    }

    @After
    public void tearDown() {
        logAppender.stop();
        logger.detachAppender(logAppender);
    }

    @Test
    public void testTokenWithRefreshTokenInsertionIsDetected() {
        boolean detected =
                tokenDetector.detectVariableFromInsertion(
                        variableStore, "oauth2_token", createOauthTokenWithRefreshToken());

        Assert.assertTrue(detected);
        Assert.assertEquals(
                "Bearer dummyAccessToken",
                variableStore.getVariable(VariableKey.AUTHORIZATION).orElse(null));
        Assert.assertEquals(
                "dummyAccessToken",
                variableStore.getVariable(VariableKey.ACCESS_TOKEN).orElse(null));
        Assert.assertEquals(
                "dummyRefreshToken",
                variableStore.getVariable(VariableKey.REFRESH_TOKEN).orElse(null));
    }

    @Test
    public void testTokenWithoutRefreshTokenInsertionIsDetected() {
        boolean detected =
                tokenDetector.detectVariableFromInsertion(
                        variableStore, "oauth2_token", createOauthTokenWithoutRefreshToken());

        Assert.assertTrue(detected);
        Assert.assertEquals(
                "Bearer dummyAccessToken",
                variableStore.getVariable(VariableKey.AUTHORIZATION).orElse(null));
        Assert.assertEquals(
                "dummyAccessToken",
                variableStore.getVariable(VariableKey.ACCESS_TOKEN).orElse(null));
        Assert.assertNull(variableStore.getVariable(VariableKey.REFRESH_TOKEN).orElse(null));
    }

    @Test
    public void testNonTokenInsertionIsNotDetected() {
        DummyClass dummyClass = new DummyClass();
        boolean detected =
                tokenDetector.detectVariableFromInsertion(variableStore, "storageKey", dummyClass);

        Assert.assertFalse(detected);
        Assert.assertNull(variableStore.getVariable(VariableKey.AUTHORIZATION).orElse(null));
        Assert.assertNull(variableStore.getVariable(VariableKey.ACCESS_TOKEN).orElse(null));
        Assert.assertNull(variableStore.getVariable(VariableKey.REFRESH_TOKEN).orElse(null));
    }

    @Test
    public void testTokenWithRefreshTokenFromStorageIsDetected() {
        String serializedOauth2Token = getSerializedOauth2TokenWithRefreshToken();
        boolean detected =
                tokenDetector.detectVariableFromStorage(
                        variableStore, "oauth2_token", serializedOauth2Token);

        Assert.assertTrue(detected);
        Assert.assertEquals(
                "Bearer dummyAccessToken",
                variableStore.getVariable(VariableKey.AUTHORIZATION).orElse(null));
        Assert.assertEquals(
                "dummyAccessToken",
                variableStore.getVariable(VariableKey.ACCESS_TOKEN).orElse(null));
        Assert.assertEquals(
                "dummyRefreshToken",
                variableStore.getVariable(VariableKey.REFRESH_TOKEN).orElse(null));
    }

    @Test
    public void testTokenWithoutRefreshTokenFromStorageIsDetected() {
        String serializedOauth2Token = getSerializedOauth2TokenWithoutRefreshToken();
        boolean detected =
                tokenDetector.detectVariableFromStorage(
                        variableStore, "oauth2_token", serializedOauth2Token);

        Assert.assertTrue(detected);
        Assert.assertEquals(
                "Bearer dummyAccessToken",
                variableStore.getVariable(VariableKey.AUTHORIZATION).orElse(null));
        Assert.assertEquals(
                "dummyAccessToken",
                variableStore.getVariable(VariableKey.ACCESS_TOKEN).orElse(null));
        Assert.assertNull(variableStore.getVariable(VariableKey.REFRESH_TOKEN).orElse(null));
    }

    @Test
    public void testNonTokenFromStorageIsNotDetected() {
        boolean detected =
                tokenDetector.detectVariableFromStorage(
                        variableStore, "storageKey", getSerializedObject());

        Assert.assertFalse(detected);
        Assert.assertNull(variableStore.getVariable(VariableKey.AUTHORIZATION).orElse(null));
        Assert.assertNull(variableStore.getVariable(VariableKey.ACCESS_TOKEN).orElse(null));
        Assert.assertNull(variableStore.getVariable(VariableKey.REFRESH_TOKEN).orElse(null));
    }

    @Test
    public void testNonDeserializableStorageDoesNotLogError() {
        boolean detected =
                tokenDetector.detectVariableFromStorage(
                        variableStore, "storageKey", "dummyConsentId");

        Assert.assertTrue(logAppender.list.isEmpty());
        Assert.assertFalse(detected);
        Assert.assertNull(variableStore.getVariable(VariableKey.AUTHORIZATION).orElse(null));
        Assert.assertNull(variableStore.getVariable(VariableKey.ACCESS_TOKEN).orElse(null));
        Assert.assertNull(variableStore.getVariable(VariableKey.REFRESH_TOKEN).orElse(null));
    }

    private OAuth2Token createOauthTokenWithRefreshToken() {
        return OAuth2Token.create("bearer", "dummyAccessToken", "dummyRefreshToken", 1500);
    }

    private OAuth2Token createOauthTokenWithoutRefreshToken() {
        return OAuth2Token.create("bearer", "dummyAccessToken", null, 1500);
    }

    private String getSerializedOauth2TokenWithRefreshToken() {
        return "{\"tokenType\":\"bearer\",\"accessToken\":\"dummyAccessToken\",\"refreshToken\":\"dummyRefreshToken\",\"idToken\":null,\"expiresInSeconds\":1500,\"refreshExpiresInSeconds\":0,\"issuedAt\":1624108858}";
    }

    private String getSerializedOauth2TokenWithoutRefreshToken() {
        return "{\"tokenType\":\"bearer\",\"accessToken\":\"dummyAccessToken\",\"refreshToken\":null,\"idToken\":null,\"expiresInSeconds\":1500,\"refreshExpiresInSeconds\":0,\"issuedAt\":1624109046}";
    }

    private String getSerializedObject() {
        return "{\"accountId\":\"dummy accountId\",\"randomNumber\":42,\"accessToken\":\"dummyAccessToken\"}";
    }

    private static class DummyClass {
        private final String accountId = "dummy accountId";
        private final int randomNumber = 42;
        private final String accessToken = "dummyAccessToken";
    }
}
