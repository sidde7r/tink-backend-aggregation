package se.tink.backend.rpc;

import java.util.List;
import se.tink.backend.core.oauth2.OAuth2WebHook;

public class OAuth2WebHookResponse {
    private List<OAuth2WebHook> webHooks;

    public List<OAuth2WebHook> getWebHooks() {
        return webHooks;
    }

    public void setWebHooks(List<OAuth2WebHook> webHooks) {
        this.webHooks = webHooks;
    }
}
