package se.tink.backend.common.workers.activity.renderers.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class RateThisAppContent {

    private String deepLinkIgnore;
    private String deepLinkRateInStore;

    public String getDeepLinkIgnore() {
        return deepLinkIgnore;
    }

    public void setDeepLinkIgnore(String deepLinkIgnore) {
        this.deepLinkIgnore = deepLinkIgnore;
    }

    public String getDeepLinkRateInStore() {
        return deepLinkRateInStore;
    }

    public void setDeepLinkRateInStore(String deepLinkRateInStore) {
        this.deepLinkRateInStore = deepLinkRateInStore;
    }

    @JsonIgnore
    public String getIgnoreDeepLinkMethodName() {
        return convertUrlToMethodName(deepLinkIgnore);
    }

    @JsonIgnore
    public String getRateDeepLinkMethodName() {
        return convertUrlToMethodName(deepLinkRateInStore);
    }

    private String convertUrlToMethodName(String url) {
        return url == null ? "" : url.replaceAll(":|/|\\?|=|\\s|-|&", "");
    }
}
