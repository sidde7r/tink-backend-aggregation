package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.steps;

import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngComponents;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.RemoteProfileMeansResponse;

public class MpinAgreementStep extends AbstractAgreementStep {

    public static final String STEP_ID = "MPIN_AGREEMENT";

    public MpinAgreementStep(IngComponents ingComponents) {
        super(STEP_ID, ingComponents);
    }

    @Override
    protected RemoteProfileMeansResponse getRemoteProfileMeans(String mobileAppId) {
        return ingDirectApiClient.getMpinProfileMeans(mobileAppId);
    }

    @Override
    protected String getSalt() {
        return ingStorage.getPermanent(Storage.MPIN_SALT);
    }

    @Override
    protected int getRequiredLevelOfAssurance() {
        return 5;
    }
}
