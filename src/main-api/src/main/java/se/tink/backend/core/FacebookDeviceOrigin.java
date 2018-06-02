package se.tink.backend.core;

public class FacebookDeviceOrigin {
    private String campaignId;
    private String adGroupId;
    private String adGroupName;
    private String adSetId;
    private String adSetName;
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
