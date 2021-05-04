package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.steps;

import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.RemoteProfileMeansResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class DeviceAgreementStep extends AbstractAgreementStep {

    public static final String STEP_ID = "DEVICE_AGREEMENT";

    public DeviceAgreementStep(IngConfiguration ingConfiguration) {
        super(STEP_ID, ingConfiguration);
    }

    @Override
    protected RemoteProfileMeansResponse getRemoteProfileMeans(String mobileAppId) {
        try {
            return ingDirectApiClient.getDeviceProfileMeans(mobileAppId);
        } catch (HttpResponseException ex) {
            if (isProfileBlockedException(ex)) {
                throw AuthorizationError.ACCOUNT_BLOCKED.exception(
                        "Profile blocked. Please contact with bank.");
            }
            throw ex;
        }
    }

    @Override
    protected String getSalt() {
        return ingStorage.getDeviceSalt();
    }

    @Override
    protected int getRequiredLevelOfAssurance() {
        return 3;
    }

    private boolean isProfileBlockedException(HttpResponseException ex) {
        return ex.getResponse().getStatus() == 403
                && ex.getResponse().getBody(String.class).contains("PROFILE_BLOCKED");
    }
}
