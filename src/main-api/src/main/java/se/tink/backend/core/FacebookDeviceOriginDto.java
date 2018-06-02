package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.protostuff.Tag;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FacebookDeviceOriginDto {
    @Tag(1)
    private String campaignId;
    @Tag(2)
    private String adGroupId;
    @Tag(3)
    private String adGroupName;
    @Tag(4)
    private String adSetId;
    @Tag(5)
    private String adSetName;
    @Tag(6)
    private String adId;

    public String getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    public String getAdGroupId() {
        return adGroupId;
    }

    public void setAdGroupId(String adGroupId) {
        this.adGroupId = adGroupId;
    }

    public String getAdGroupName() {
        return adGroupName;
    }

    public void setAdGroupName(String adGroupName) {
        this.adGroupName = adGroupName;
    }

    public String getAdSetId() {
        return adSetId;
    }

    public void setAdSetId(String adSetId) {
        this.adSetId = adSetId;
    }

    public String getAdSetName() {
        return adSetName;
    }

    public void setAdSetName(String adSetName) {
        this.adSetName = adSetName;
    }

    public String getAdId() {
        return adId;
    }

    public void setAdId(String adId) {
        this.adId = adId;
    }
}
