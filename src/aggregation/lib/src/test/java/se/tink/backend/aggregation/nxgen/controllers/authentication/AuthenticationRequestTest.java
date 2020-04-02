package se.tink.backend.aggregation.nxgen.controllers.authentication;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;

public class AuthenticationRequestTest {

    @Test
    public void shouldCreateObjectWithCredentials() {
        // given
        Credentials credentials = Mockito.mock(Credentials.class);
        AuthenticationRequest objectUnderTest = new AuthenticationRequest(credentials);
        // when
        Credentials result = objectUnderTest.getCredentials();
        // then
        Assert.assertEquals(credentials, result);
    }

    @Test
    public void shouldCreateObjectWithUderInputs() {
        // given
        Credentials credentials = Mockito.mock(Credentials.class);
        AuthenticationRequest objectUnderTest = new AuthenticationRequest(credentials);
        Map<String, String> userInputs = new HashMap<>();
        userInputs.put("key", "value");
        objectUnderTest.withUserInputs(userInputs);
        // when
        Map<String, String> result = objectUnderTest.getUserInputs();
        // then
        Assert.assertTrue(result.containsKey("key"));
        Assert.assertEquals("value", result.get("key"));
    }

    @Test
    public void shouldCreateObjectWithCallbackData() {
        // given
        Credentials credentials = Mockito.mock(Credentials.class);
        AuthenticationRequest objectUnderTest = new AuthenticationRequest(credentials);
        Map<String, String> callbackData = new HashMap<>();
        callbackData.put("key", "value");
        objectUnderTest.withCallbackData(callbackData);
        // when
        Map<String, String> result = objectUnderTest.getCallbackData();
        // then
        Assert.assertTrue(result.containsKey("key"));
        Assert.assertEquals("value", result.get("key"));
    }
}
