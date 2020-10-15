package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.steps;

import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.RemoteProfileMeansResponse;

public class MpinAgreementStep extends AbstractAgreementStep {

    public static final String STEP_ID = "MPIN_AGREEMENT";

    public MpinAgreementStep(IngConfiguration ingConfiguration) {
        super(STEP_ID, ingConfiguration);
    }

    @Override
    protected RemoteProfileMeansResponse getRemoteProfileMeans(String mobileAppId) {
        return ingDirectApiClient.getMpinProfileMeans(mobileAppId);
    }

    @Override
    protected String getSalt() {
        return ingStorage.getMpinSalt();
    }

    @Override
    protected int getRequiredLevelOfAssurance() {
        return 5;
    }
}
