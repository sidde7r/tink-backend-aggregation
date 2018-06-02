package se.tink.backend.main.mappers;

import com.google.common.collect.Sets;
import org.junit.Test;
import se.tink.backend.core.oauth2.OAuth2WebHookEvent;
import se.tink.backend.rpc.webhook.OAuth2WebHook;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CoreOauth2WebHookMapperTest {

    @Test
    public void mappingFromMainToCore_givesCorrectValues() {
        OAuth2WebHook rpcWebhook = new OAuth2WebHook();
        rpcWebhook.setUrl("https://testing.com/webhook");
        rpcWebhook.setEvents(Sets.newHashSet(OAuth2WebHookEvent.SIGNABLE_OPERATION_UPDATE, OAuth2WebHookEvent.TRANSACTION_UPDATE));
        rpcWebhook.setSecret("testSecret");

        se.tink.backend.core.oauth2.OAuth2WebHook coreWebhook = CoreOauth2WebHookMapper.fromMainToCore(rpcWebhook);

        assertEquals(rpcWebhook.getUrl(), coreWebhook.getUrl());
        assertTrue(rpcWebhook.getEvents().contains(OAuth2WebHookEvent.SIGNABLE_OPERATION_UPDATE));
        assertTrue(rpcWebhook.getEvents().contains(OAuth2WebHookEvent.TRANSACTION_UPDATE));
        assertEquals(rpcWebhook.getSecret(), coreWebhook.getSecret());
    }
}
