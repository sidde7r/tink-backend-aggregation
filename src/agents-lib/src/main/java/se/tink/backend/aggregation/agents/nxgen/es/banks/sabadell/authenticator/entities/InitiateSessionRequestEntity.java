package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringEscapeUtils;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellCryptoUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitiateSessionRequestEntity {
    private String deviceInfo;
    private String lastRequestDate;
    private String brand;
    private String geolocationData;
    private boolean newDevice;
    private String requestId;
    private String userName;
    private int loginType;
    private String compilationType;
    private String password;
    private String contract;
    private List<ParametersBSEntity> parametersBS;
    private String devicePrint;
    private int lastKnownBrand;
    private String csid;
    private String trusteer;

    @JsonIgnore
    public static InitiateSessionRequestEntity build(String username, String password) {
        InitiateSessionRequestEntity initiateSessionRequestEntity = new InitiateSessionRequestEntity();

        initiateSessionRequestEntity.deviceInfo = SabadellConstants.InitiateSessionRequest.DEVICE_INFO;
        initiateSessionRequestEntity.lastRequestDate = SabadellConstants.InitiateSessionRequest.LAST_REQUEST_DATE;
        initiateSessionRequestEntity.brand = SabadellConstants.InitiateSessionRequest.BRAND;
        initiateSessionRequestEntity.geolocationData = SabadellConstants.InitiateSessionRequest.GEO_LOCATION_DATA;
        initiateSessionRequestEntity.newDevice = SabadellConstants.InitiateSessionRequest.NEW_DEVICE;
        initiateSessionRequestEntity.requestId = SabadellConstants.InitiateSessionRequest.REQUEST_ID;
        initiateSessionRequestEntity.userName = username;
        initiateSessionRequestEntity.loginType = SabadellConstants.InitiateSessionRequest.LOGIN_TYPE;
        initiateSessionRequestEntity.compilationType = SabadellConstants.InitiateSessionRequest.COMPILATION_TYPE;
        initiateSessionRequestEntity.password = password;
        initiateSessionRequestEntity.contract = SabadellConstants.InitiateSessionRequest.CONTRACT;
        initiateSessionRequestEntity.parametersBS = getParametersBS(username, password);
        initiateSessionRequestEntity.devicePrint = SabadellConstants.InitiateSessionRequest.DEVICE_PRINT;
        initiateSessionRequestEntity.lastKnownBrand = SabadellConstants.InitiateSessionRequest.LAST_KNOWN_BRAND;
        initiateSessionRequestEntity.csid = UUID.randomUUID().toString();
        initiateSessionRequestEntity.trusteer = SabadellConstants.InitiateSessionRequest.TRUSTEER;

        return initiateSessionRequestEntity;
    }

    @JsonIgnore
    private static List<ParametersBSEntity> getParametersBS(String username, String password) {
        ParametersBSEntity usernameParam = new ParametersBSEntity()
                .setKey(SabadellConstants.InitiateSessionRequest.USERNAME_BS_KEY)
                .setValue(StringEscapeUtils.escapeJson(
                        SabadellCryptoUtils.getEncryptedParamAsB64String(username))
                );

        ParametersBSEntity passwordParam = new ParametersBSEntity()
                .setKey(SabadellConstants.InitiateSessionRequest.PASSWORD_BS_KEY)
                .setValue(StringEscapeUtils.escapeJson(
                        SabadellCryptoUtils.getEncryptedParamAsB64String(password))
                );

        return Arrays.asList(usernameParam, passwordParam);
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getLastRequestDate() {
        return lastRequestDate;
    }

    public void setLastRequestDate(String lastRequestDate) {
        this.lastRequestDate = lastRequestDate;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getGeolocationData() {
        return geolocationData;
    }

    public void setGeolocationData(String geolocationData) {
        this.geolocationData = geolocationData;
    }

    public boolean isNewDevice() {
        return newDevice;
    }

    public void setNewDevice(boolean newDevice) {
        this.newDevice = newDevice;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getLoginType() {
        return loginType;
    }

    public void setLoginType(int loginType) {
        this.loginType = loginType;
    }

    public String getCompilationType() {
        return compilationType;
    }

    public void setCompilationType(String compilationType) {
        this.compilationType = compilationType;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getContract() {
        return contract;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }

    public List<ParametersBSEntity> getParametersBS() {
        return parametersBS;
    }

    public InitiateSessionRequestEntity setParametersBS(
            List<ParametersBSEntity> parametersBS) {
        this.parametersBS = parametersBS;
        return this;
    }

    public String getDevicePrint() {
        return devicePrint;
    }

    public void setDevicePrint(String devicePrint) {
        this.devicePrint = devicePrint;
    }

    public int getLastKnownBrand() {
        return lastKnownBrand;
    }

    public void setLastKnownBrand(int lastKnownBrand) {
        this.lastKnownBrand = lastKnownBrand;
    }

    public String getCsid() {
        return csid;
    }

    public void setCsid(String csid) {
        this.csid = csid;
    }

    public String getTrusteer() {
        return trusteer;
    }

    public void setTrusteer(String trusteer) {
        this.trusteer = trusteer;
    }
}
