package se.tink.backend.core;

import java.util.Optional;
import com.google.common.base.Strings;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import se.tink.backend.auth.OAuth2ClientRequest;
import se.tink.backend.core.oauth2.OAuth2Client;
import se.tink.backend.core.oauth2.OAuth2Utils;

/**
 * UserOrigin describes where the user originates.
 * In the Tink Apps case we are handed this information from AppsFlyer and it can say if the user was an organic user
 * or not (if we payed (marketing campaign) to acquire this user or not).
 * In the OAuth2 client case we are handed this information by the client upon POST /user/register/anonymous/
 *
 */
@Entity
@Table(name = "users_origin")
public class UserOrigin {

    public static final String SERVICE_NAME_APPSFLYER = "appsflyer";
    public static final String SERVICE_NAME_TINK_OAUTH = "tink-oauth";

    @Id
    private String userId;
    @Creatable
    private String serviceName;
    @Creatable
    private boolean organic;
    @Creatable
    private String externalServiceId;
    @Creatable
    private String mediaSource;
    @Creatable
    private String campaign;
    @Creatable
    private String agency;
    @Creatable
    private long clickTime;
    @Creatable
    private long installTime;

    /* Might want to make the extraParams modifiable in order to set parameters later from back-end */
    @Creatable
    private String extraParam1;
    @Creatable
    private String extraParam2;
    @Creatable
    private String extraParam3;
    @Creatable
    private String extraParam4;
    @Creatable
    private String extraParam5;

    @Creatable
    private boolean facebook;
    @Creatable
    private String fbAdGroupId;
    @Creatable
    private String fbAdGroupName;
    @Creatable
    private String fbCampaignId;
    @Creatable
    private String fbAdSetId;
    @Creatable
    private String fbAdSetName;
    @Creatable
    private String fbAdId;

    @Creatable
    private String deviceType;

    public static UserOrigin fromLinkRequest(OAuth2ClientRequest oauth2ClientRequest, String origin) {
        UserOrigin userOrigin = new UserOrigin();

        userOrigin.setServiceName(SERVICE_NAME_TINK_OAUTH);
        userOrigin.setOrganic(false);
        userOrigin.setInstallTime(new Date().getTime());

        Optional<OAuth2Client> client = OAuth2Utils.getOAuth2Client(oauth2ClientRequest);
        if (client.isPresent()) {
            userOrigin.setExternalServiceId(client.get().getId());
        }

        if (!Strings.isNullOrEmpty(origin)) {
            userOrigin.setMediaSource(origin);
        }

        return userOrigin;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isOrganic() {
        return organic;
    }

    public String getExternalServiceId() {
        return externalServiceId;
    }

    public String getMediaSource() {
        return mediaSource;
    }

    public String getCampaign() {
        return campaign;
    }

    public String getAgency() {
        return agency;
    }

    public String getExtraParam1() {
        return extraParam1;
    }

    public String getExtraParam2() {
        return extraParam2;
    }

    public String getExtraParam3() {
        return extraParam3;
    }

    public String getExtraParam4() {
        return extraParam4;
    }

    public String getExtraParam5() {
        return extraParam5;
    }

    public long getClickTime() {
        return clickTime;
    }

    public long getInstallTime() {
        return installTime;
    }

    public boolean isFacebook() {
        return facebook;
    }

    public String getFbAdGroupId() {
        return fbAdGroupId;
    }

    public String getFbAdGroupName() {
        return fbAdGroupName;
    }

    public String getFbCampaignId() {
        return fbCampaignId;
    }

    public String getFbAdSetId() {
        return fbAdSetId;
    }

    public String getFbAdSetName() {
        return fbAdSetName;
    }

    public String getFbAdId() {
        return fbAdId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setOrganic(boolean organic) {
        this.organic = organic;
    }

    public void setExternalServiceId(String externalServiceId) {
        this.externalServiceId = externalServiceId;
    }

    public void setMediaSource(String mediaSource) {
        this.mediaSource = mediaSource;
    }

    public void setCampaign(String campaign) {
        this.campaign = campaign;
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public void setExtraParam1(String extraParam1) {
        this.extraParam1 = extraParam1;
    }

    public void setExtraParam2(String extraParam2) {
        this.extraParam2 = extraParam2;
    }

    public void setExtraParam3(String extraParam3) {
        this.extraParam3 = extraParam3;
    }

    public void setExtraParam4(String extraParam4) {
        this.extraParam4 = extraParam4;
    }

    public void setExtraParam5(String extraParam5) {
        this.extraParam5 = extraParam5;
    }

    public void setClickTime(long clickTime) {
        this.clickTime = clickTime;
    }

    public void setInstallTime(long installTime) {
        this.installTime = installTime;
    }

    public void setFacebook(boolean facebook) {
        this.facebook = facebook;
    }

    public void setFbAdGroupId(String fbAdGroupId) {
        this.fbAdGroupId = fbAdGroupId;
    }

    public void setFbAdGroupName(String fbAdGroupName) {
        this.fbAdGroupName = fbAdGroupName;
    }

    public void setFbCampaignId(String fbCampaignId) {
        this.fbCampaignId = fbCampaignId;
    }

    public void setFbAdSetId(String fbAdSetId) {
        this.fbAdSetId = fbAdSetId;
    }

    public void setFbAdSetName(String fbAdSetName) {
        this.fbAdSetName = fbAdSetName;
    }

    public void setFbAdId(String fbAdId) {
        this.fbAdId = fbAdId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
