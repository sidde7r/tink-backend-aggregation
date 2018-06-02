package se.tink.backend.core;

public class DeviceOrigin {
    private boolean organic;
    private String serviceName;
    private String externalServiceId;
    private String mediaSource;
    private String campaign;
    private String agency;
    private long clickTime;
    private long installTime;
    private AppsFlyerDeviceOrigin appsFlyer;
    private FacebookDeviceOrigin facebook;

    public boolean isOrganic() {
        return organic;
    }

    public void setOrganic(boolean organic) {
        this.organic = organic;
    }

    public String getExternalServiceId() {
        return externalServiceId;
    }

    public void setExternalServiceId(String externalServiceId) {
        this.externalServiceId = externalServiceId;
    }

    public String getMediaSource() {
        return mediaSource;
    }

    public void setMediaSource(String mediaSource) {
        this.mediaSource = mediaSource;
    }

    public String getCampaign() {
        return campaign;
    }

    public void setCampaign(String campaign) {
        this.campaign = campaign;
    }

    public String getAgency() {
        return agency;
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public long getClickTime() {
        return clickTime;
    }

    public void setClickTime(long clickTime) {
        this.clickTime = clickTime;
    }

    public long getInstallTime() {
        return installTime;
    }

    public void setInstallTime(long installTime) {
        this.installTime = installTime;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public AppsFlyerDeviceOrigin getAppsFlyer() {
        return appsFlyer;
    }

    public void setAppsFlyer(AppsFlyerDeviceOrigin appsFlyer) {
        this.appsFlyer = appsFlyer;
    }

    public FacebookDeviceOrigin getFacebook() {
        return facebook;
    }

    public boolean isFacebook() {
        return getFacebook() != null;
    }

    public void setFacebook(FacebookDeviceOrigin facebook) {
        this.facebook = facebook;
    }
}
