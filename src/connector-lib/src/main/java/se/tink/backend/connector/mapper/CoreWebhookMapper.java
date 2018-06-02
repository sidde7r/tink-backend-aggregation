package se.tink.backend.connector.mapper;

import com.google.common.collect.Lists;
import se.tink.backend.connector.rpc.WebhookEntity;
import se.tink.backend.core.oauth2.OAuth2WebHook;

public class CoreWebhookMapper {

    public static WebhookEntity fromCoreToConnector(OAuth2WebHook coreWebHook) {
        WebhookEntity rpcWebhook = new WebhookEntity();
        rpcWebhook.setId(coreWebHook.getId());
        rpcWebhook.setSecret(coreWebHook.getSecret());
        rpcWebhook.setUrl(coreWebHook.getUrl());
        rpcWebhook.setEvents(Lists.newArrayList(coreWebHook.getEvents()));
        return rpcWebhook;
    }
}
