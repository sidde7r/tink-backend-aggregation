package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.entity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request.dto.RegistrationResponseDTO;

public class BPostBankAuthContext {

    private String pin;
    private String csrfToken;
    private String subscriptionNo;
    private String deviceRootedHash;
    private String email;
    private String challengeCode;
    private String orderReference;
    private String deviceUniqueId;
    private String deviceInstallationId;
    private String sessionToken;

    public String getCsrfToken() {
        return csrfToken;
    }

    public void setCsrfToken(String csrfToken) {
        this.csrfToken = csrfToken;
    }

    public String getLogin() {
        return subscriptionNo.substring(0, 8);
    }

    public String getDeviceRootedHash() {
        return deviceRootedHash;
    }

    String generateRandomDeviceRootedHash() {
        return (UUID.randomUUID().toString() + UUID.randomUUID().toString()).replace("-", "");
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getChallengeCode() {
        return challengeCode;
    }

    public String getOrderReference() {
        return orderReference;
    }

    public void initRegistration(
            final RegistrationResponseDTO registrationResponse, final Credentials credentials) {
        this.challengeCode = registrationResponse.getChallengeCode();
        this.orderReference = registrationResponse.getOrderReference();
        this.subscriptionNo = credentials.getField(Field.Key.USERNAME);
        this.email = credentials.getField(Field.Key.EMAIL);
        deviceRootedHash = generateRandomDeviceRootedHash();
        deviceUniqueId = generateRandomDeviceRootedHash();
    }

    public void completeRegistration(RegistrationResponseDTO registrationResponse) {
        this.deviceInstallationId = registrationResponse.getDeviceInstallationID();
        this.challengeCode = null;
    }

    public boolean isRegistrationCompleted() {
        return deviceInstallationId != null;
    }

    public boolean isRegistrationInitialized() {
        return challengeCode != null;
    }

    public void clearRegistrationData() {
        this.orderReference = null;
        this.subscriptionNo = null;
        this.email = null;
        deviceRootedHash = null;
        deviceUniqueId = null;
        pin = null;
    }

    public String getDeviceUniqueId() {
        return deviceUniqueId;
    }

    public String getDeviceCredential() {
        StringBuilder sb = new StringBuilder(getLogin()).append(getPin());
        return encryptWithSHA256AndConvertToHex(sb.toString());
    }

    public String getDeviceInstallationId() {
        return deviceInstallationId;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getDataMapCode() {
        StringBuilder sb = new StringBuilder(getLogin()).append(getPin());
        sb = new StringBuilder(encryptWithSHA256AndConvertToHex(sb.toString()));
        sb.append(sessionToken);
        return encryptWithSHA256AndConvertToHex(sb.toString());
    }

    private String encryptWithSHA256AndConvertToHex(String value) {
        try {
            MessageDigest instance = MessageDigest.getInstance("SHA-256");
            return bytesToHex(instance.digest(value.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    void setSubscriptionNo(String subscriptionNo) {
        this.subscriptionNo = subscriptionNo;
    }
}
