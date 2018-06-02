package se.tink.backend.connector.mapper;

import com.google.common.collect.Sets;
import org.junit.Test;
import se.tink.backend.connector.rpc.WebhookEntity;
import se.tink.backend.core.oauth2.OAuth2WebHook;
import se.tink.backend.core.oauth2.OAuth2WebHookEvent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CoreWebhookMapperTest {

    @Test
    public void mappingFromCoreToConnector_givesCorrectValues() {
        OAuth2WebHook webhook = new OAuth2WebHook();
        webhook.setUrl("https://testing.com/webhook");
        webhook.setEvents(Sets.newHashSet(OAuth2WebHookEvent.SIGNABLE_OPERATION_UPDATE, OAuth2WebHookEvent.TRANSACTION_UPDATE));
        webhook.setSecret("testSecret");
        WebhookEntity webhookEntity = CoreWebhookMapper.fromCoreToConnector(webhook);

        assertEquals(webhook.getUrl(), webhookEntity.getUrl());
        assertTrue(webhook.getEvents().contains(OAuth2WebHookEvent.SIGNABLE_OPERATION_UPDATE));
        assertTrue(webhook.getEvents().contains(OAuth2WebHookEvent.TRANSACTION_UPDATE));
        assertEquals(webhook.getSecret(), webhookEntity.getSecret());
    }
}
