package se.tink.backend.webhook.rpc;

import se.tink.backend.core.oauth2.OAuth2WebHook;

public class WebHookRequest {
    private Object content;
    private String event;
    private OAuth2WebHook webhook;

    public WebHookRequest(OAuth2WebHook webHook, String event, Object content) {
        this.webhook = webHook;
        this.event = event;
        this.content = content;
    }

    public WebHookRequest() {
    }

    public Object getContent() {
        return content;
    }

    public String getEvent() {
        return event;
    }

    public OAuth2WebHook getWebhook() {
        return webhook;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public void setWebhook(OAuth2WebHook webhook) {
        this.webhook = webhook;
    }
}
