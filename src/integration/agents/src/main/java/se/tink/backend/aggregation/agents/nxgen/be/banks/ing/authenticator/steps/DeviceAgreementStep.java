package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.steps;

import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.RemoteProfileMeansResponse;

public class DeviceAgreementStep extends AbstractAgreementStep {

    public static final String STEP_ID = "DEVICE_AGREEMENT";

    public DeviceAgreementStep(IngConfiguration ingConfiguration) {
        super(STEP_ID, ingConfiguration);
    }

    @Override
    protected RemoteProfileMeansResponse getRemoteProfileMeans(String mobileAppId) {
        return ingDirectApiClient.getDeviceProfileMeans(mobileAppId);
    }

    @Override
    protected String getSalt() {
        return ingStorage.getDeviceSalt();
    }

    @Override
    protected int getRequiredLevelOfAssurance() {
        return 3;
    }
}
