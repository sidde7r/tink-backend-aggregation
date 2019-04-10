package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.encryption.LibTFA;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities.PdeviceSignContainer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ActivateProfileRequest {

    private PdeviceSignContainer pdeviceSignContainer;
    private String pubkey;

    private ActivateProfileRequest() {}

    public static ActivateProfileRequest create(CreateProfileResponse createProfile, LibTFA tfa) {
        return new ActivateProfileRequest()
                .setPdeviceSignContainer(tfa.generatePDeviceSignContainer(createProfile))
                .setPubkey(tfa.getDeviceRsaPublicKey());
    }

    private ActivateProfileRequest setPdeviceSignContainer(
            PdeviceSignContainer pdeviceSignContainer) {
        this.pdeviceSignContainer = pdeviceSignContainer;
        return this;
    }

    private ActivateProfileRequest setPubkey(String pubkey) {
        this.pubkey = pubkey;
        return this;
    }
}
