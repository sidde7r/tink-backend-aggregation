package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShareDepotResponse {
    private ShareDepotWrapper shareDepotWrapper;

    public ShareDepotWrapper getShareDepotWrapper() {
        return shareDepotWrapper;
    }

    public void setShareDepotWrapper(ShareDepotWrapper shareDepotWrapper) {
        this.shareDepotWrapper = shareDepotWrapper;
    }
}
