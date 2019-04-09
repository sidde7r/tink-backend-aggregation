package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator;

import com.google.common.base.Strings;
import java.nio.charset.Charset;
import java.util.UUID;
import org.apache.commons.codec.binary.Base64;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcPersistentStorage;

// handle device pinning, i.e. enable client for SDC backend
public class SdcDevice {
    private final SdcPersistentStorage persistentStorage;
    private final SignKeys signKeys;

    public SdcDevice(SdcPersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
        this.signKeys = new SignKeys(persistentStorage);
    }

    public boolean needsPinning() {
        return Strings.isNullOrEmpty(this.persistentStorage.getSignedDeviceId());
    }

    public SignKeys getSignKeys() {
        return signKeys;
    }

    public String getPublicKey() {
        return signKeys.getPublicKey();
    }

    public String getSignedDeviceId() {
        return this.persistentStorage.getSignedDeviceId();
    }

    public String getDeviceId() {
        String deviceId = this.persistentStorage.getDeviceId();
        if (Strings.isNullOrEmpty(deviceId)) {
            String androidId = UUID.randomUUID().toString().toUpperCase();
            String serial = "UNKNOWN";
            deviceId =
                    Base64.encodeBase64String(
                            (androidId
                                            + "|"
                                            + serial
                                            + "|"
                                            + SdcConstants.Session.MODEL
                                            + "|"
                                            + SdcConstants.Session.MANUFACTURER
                                            + "|"
                                            + SdcConstants.Session.DEVICE)
                                    .getBytes(Charset.forName("UTF-8")));

            this.persistentStorage.putDeviceId(deviceId);
        }

        return deviceId;
    }
}
