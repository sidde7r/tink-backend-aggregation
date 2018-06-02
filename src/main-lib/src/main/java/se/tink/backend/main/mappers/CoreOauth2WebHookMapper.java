package se.tink.backend.main.mappers;

import se.tink.backend.core.oauth2.OAuth2WebHook;

public class CoreOauth2WebHookMapper {

    public static OAuth2WebHook fromMainToCore(se.tink.backend.rpc.webhook.OAuth2WebHook rpcWebHook) {
        OAuth2WebHook coreWebHook = new OAuth2WebHook();
        coreWebHook.setSecret(rpcWebHook.getSecret());
        coreWebHook.setUrl(rpcWebHook.getUrl());
        coreWebHook.setEvents(rpcWebHook.getEvents());

        return coreWebHook;
    }
}
