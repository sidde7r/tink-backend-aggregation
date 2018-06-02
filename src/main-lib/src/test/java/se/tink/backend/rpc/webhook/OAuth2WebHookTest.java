package se.tink.backend.rpc.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.core.oauth2.OAuth2Client;
import se.tink.backend.core.oauth2.OAuth2WebHookEvent;

public class OAuth2WebHookTest {

    private OAuth2Client client;

    @Before
    public void setUp() throws JsonProcessingException {

        client = new OAuth2Client();
        client.setPayloadSerialized(new ObjectMapper().writeValueAsString(ImmutableMap.of(
                OAuth2Client.PayloadKey.WEBHOOK_DOMAINS, "https://www.tink.se,https://tjoflöjt.tink.se/")));
    }

    @Test
    public void validateWorkingHook1() {
        OAuth2WebHook workingHook1 = new OAuth2WebHook();
        workingHook1.setUrl("https://www.tink.se/my-path/");
        workingHook1.setSecret("my-secret");
        workingHook1.setEvents(Sets.newHashSet(OAuth2WebHookEvent.SIGNABLE_OPERATION_UPDATE));

        Assert.assertTrue(workingHook1.validate(client));
    }

    @Test
    public void validateWorkingHook2() {
        OAuth2WebHook workingHook2 = new OAuth2WebHook();
        workingHook2.setUrl("https://tjoflöjt.tink.se/hej/23123124916249127/afaijfaw?userid=24nfi1hf217gf128gf");
        workingHook2.setSecret("my-secret");
        workingHook2.setEvents(Sets.newHashSet(OAuth2WebHookEvent.SIGNABLE_OPERATION_UPDATE));

        Assert.assertTrue(workingHook2.validate(client));
    }

    @Test
    public void validateHookWithoutEvent() {
        OAuth2WebHook hookWithoutEvent = new OAuth2WebHook();
        hookWithoutEvent.setUrl("https://www.tink.se/my-path/");
        hookWithoutEvent.setSecret("my-secret");

        Assert.assertFalse(hookWithoutEvent.validate(client));
    }

    @Test
    public void validateHookWithInvalidEvents() {
        OAuth2WebHook hookWithInvalidEvents = new OAuth2WebHook();
        hookWithInvalidEvents.setUrl("https://www.tink.se/my-path/");
        hookWithInvalidEvents.setSecret("my-secret");
        hookWithInvalidEvents.setEvents(Sets.newHashSet("event-that-doesnt-exist", null));

        Assert.assertFalse(hookWithInvalidEvents.validate(client));
    }

    @Test
    public void validateHookWithoutSecret() {
        OAuth2WebHook hookWithoutSecret = new OAuth2WebHook();
        hookWithoutSecret.setUrl("https://www.tink.se/my-path/");
        hookWithoutSecret.setEvents(Sets.newHashSet(OAuth2WebHookEvent.SIGNABLE_OPERATION_UPDATE));

        Assert.assertFalse(hookWithoutSecret.validate(client));
    }

    @Test
    public void validateHookWithoutUrl() {
        OAuth2WebHook hookWithoutUrl = new OAuth2WebHook();
        hookWithoutUrl.setSecret("my-secret");
        hookWithoutUrl.setEvents(Sets.newHashSet(OAuth2WebHookEvent.SIGNABLE_OPERATION_UPDATE));

        Assert.assertFalse(hookWithoutUrl.validate(client));
    }

    @Test
    public void validateHookWithHttpUrl() {
        OAuth2WebHook hookWithHttpUrl = new OAuth2WebHook();
        hookWithHttpUrl.setUrl("http://www.tink.se/my-path/");
        hookWithHttpUrl.setSecret("my-secret");
        hookWithHttpUrl.setEvents(Sets.newHashSet(OAuth2WebHookEvent.SIGNABLE_OPERATION_UPDATE));

        Assert.assertFalse(hookWithHttpUrl.validate(client));
    }

    @Test
    public void validateHookWithInvalidUrl() {
        OAuth2WebHook hookWithInvalidUrl = new OAuth2WebHook();
        hookWithInvalidUrl.setUrl("https://_@©$©@€12#!.tink.se/my-path/");
        hookWithInvalidUrl.setSecret("my-secret");
        hookWithInvalidUrl.setEvents(Sets.newHashSet(OAuth2WebHookEvent.SIGNABLE_OPERATION_UPDATE));

        Assert.assertFalse(hookWithInvalidUrl.validate(client));
    }

    @Test
    public void validateHookWithWrongDomain() {
        OAuth2WebHook hookWithWrongDomain = new OAuth2WebHook();
        hookWithWrongDomain.setUrl("https://this.is.an.ok.domain.se/but/it/doesnt/exist/on/client");
        hookWithWrongDomain.setSecret("my-secret");
        hookWithWrongDomain.setEvents(Sets.newHashSet(OAuth2WebHookEvent.SIGNABLE_OPERATION_UPDATE));

        Assert.assertFalse(hookWithWrongDomain.validate(client));
    }

    @Test
    public void validateHookWithMaliciousDomain() {
        OAuth2WebHook hookWithMaliciousDomain = new OAuth2WebHook();
        hookWithMaliciousDomain.setUrl("https://www.tink.se.malicious.com/but/it/doesnt/exist/on/client");
        hookWithMaliciousDomain.setSecret("my-secret");
        hookWithMaliciousDomain.setEvents(Sets.newHashSet(OAuth2WebHookEvent.SIGNABLE_OPERATION_UPDATE));

        Assert.assertFalse(hookWithMaliciousDomain.validate(client));
    }
}
