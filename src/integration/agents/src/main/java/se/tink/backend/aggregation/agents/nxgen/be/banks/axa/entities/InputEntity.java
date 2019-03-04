package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.DeviceInfoEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class InputEntity {
    private DeviceInfoEntity deviceInfo;
    private String applCd;
    private String challenge;
    private String clientInitialVector;
    private String deviceBrand;
    private String deviceModel;
    private String encryptedClientPublicKeyAndNonce;
    private String language;
    private String panNumberFull;
    private String response;
    private String serialNo;
    private String derivationCd;
    private Integer customerId;

    @JsonProperty("UCRid")
    private String uCRid;
    private String encryptedServerNonce;

    public String getPanNumberFull() {
        return panNumberFull;
    }

    public void setPanNumberFull(String panNumberFull) {
        this.panNumberFull = panNumberFull;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getuCRid() {
        return uCRid;
    }

    public void setuCRid(String uCRid) {
        this.uCRid = uCRid;
    }

    public String getClientInitialVector() {
        return clientInitialVector;
    }

    public void setClientInitialVector(String clientInitialVector) {
        this.clientInitialVector = clientInitialVector;
    }

    public String getEncryptedClientPublicKeyAndNonce() {
        return encryptedClientPublicKeyAndNonce;
    }

    public void setEncryptedClientPublicKeyAndNonce(String encryptedClientPublicKeyAndNonce) {
        this.encryptedClientPublicKeyAndNonce = encryptedClientPublicKeyAndNonce;
    }

    public String getDeviceBrand() {
        return deviceBrand;
    }

    public void setDeviceBrand(String deviceBrand) {
        this.deviceBrand = deviceBrand;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public DeviceInfoEntity getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfoEntity deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getApplCd() {
        return applCd;
    }

    public void setApplCd(String applCd) {
        this.applCd = applCd;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public void setDerivationCd(String derivationCd) {
        this.derivationCd = derivationCd;
    }

    public void setEncryptedServerNonce(String encryptedServerNonce) {
        this.encryptedServerNonce = encryptedServerNonce;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }
}
