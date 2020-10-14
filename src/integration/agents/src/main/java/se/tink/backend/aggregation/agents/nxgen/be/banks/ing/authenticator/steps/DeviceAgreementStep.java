package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.steps;

import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngComponents;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.RemoteProfileMeansResponse;

public class DeviceAgreementStep extends AbstractAgreementStep {

    public static final String STEP_ID = "DEVICE_AGREEMENT";

    public DeviceAgreementStep(IngComponents ingComponents) {
        super(STEP_ID, ingComponents);
    }

    @Override
    protected RemoteProfileMeansResponse getRemoteProfileMeans(String mobileAppId) {
        return ingDirectApiClient.getDeviceProfileMeans(mobileAppId);
    }

    @Override
    protected String getSalt() {
        return ingStorage.getPermanent(Storage.DEVICE_SALT);
    }

    @Override
    protected int getRequiredLevelOfAssurance() {
        return 3;
    }
}
