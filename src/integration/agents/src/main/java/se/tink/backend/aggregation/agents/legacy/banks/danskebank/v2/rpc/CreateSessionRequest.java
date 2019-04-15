package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.encryption.MobileBankingEncryptionHelper;
import se.tink.libraries.strings.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateSessionRequest {
    @JsonIgnore private static final String DEVICE_APP_VERSION = "3.18.1";
    @JsonIgnore private static final String DEVICE_MANUFACTURER = "LGE";
    @JsonIgnore private static final String DEVICE_MODEL = "Nexus 5X";
    @JsonIgnore private static final String DEVICE_OS = "Android";
    @JsonIgnore private static final String DEVICE_OS_VERSION = "6.0.1";

    private String appVersion;
    private String country;
    private String deviceId;
    private boolean isTablet;
    private boolean logSession;
    private String manufacturer;
    private String model;
    private String notificationId;
    private String os;
    private String osVersion;
    private String nssid;
    private String language;
    private List<String> stopOnErrors = Lists.newArrayList();

    public String getAppVersion() {
        return appVersion;
    }

    public String getCountry() {
        return country;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModel() {
        return model;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public String getOs() {
        return os;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public boolean isLogSession() {
        return logSession;
    }

    public boolean isTablet() {
        return isTablet;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setLogSession(boolean logSession) {
        this.logSession = logSession;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public void setTablet(boolean isTablet) {
        this.isTablet = isTablet;
    }

    public String getNssid() {
        return nssid;
    }

    public void setNssid(String nssid) {
        this.nssid = nssid;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<String> getStopOnErrors() {
        return stopOnErrors;
    }

    public void setStopOnErrors(List<String> stopOnErrors) {
        this.stopOnErrors = stopOnErrors;
    }

    public static CreateSessionRequest create(
            InitSessionResponse initSessionResponse,
            Credentials credentials,
            String country,
            String language,
            Optional<String> nssId) {
        String deviceId = createDeviceId(credentials);
        String notificationId =
                MobileBankingEncryptionHelper.encryptInitSessionToken(
                        initSessionResponse.getToken());

        CreateSessionRequest createSessionRequest = new CreateSessionRequest();

        createSessionRequest.setOs(DEVICE_OS);
        createSessionRequest.setManufacturer(DEVICE_MANUFACTURER);
        createSessionRequest.setAppVersion(DEVICE_APP_VERSION);
        createSessionRequest.setOsVersion(DEVICE_OS_VERSION);
        createSessionRequest.setModel(DEVICE_MODEL);
        createSessionRequest.setCountry(country);
        createSessionRequest.setLanguage(language);
        createSessionRequest.setDeviceId(deviceId);
        createSessionRequest.setNotificationId(notificationId);

        if (nssId.isPresent()) {
            createSessionRequest.setNssid(nssId.get());
        }

        return createSessionRequest;
    }

    private static String createDeviceId(Credentials credentials) {
        return StringUtils.hashAsUUID("TINK-" + credentials.getUsername());
    }
}
