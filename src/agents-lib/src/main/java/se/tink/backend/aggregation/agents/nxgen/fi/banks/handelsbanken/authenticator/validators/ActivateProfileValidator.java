package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.authenticator.validators;

import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.ActivateProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.validators.HandelsbankenValidator;

public class ActivateProfileValidator extends HandelsbankenValidator<ActivateProfileResponse>{

    public ActivateProfileValidator(ActivateProfileResponse activateProfile) {
        super(activateProfile);
    }

    public void validate() throws LoginException {
        if (getResponse().isInCreatePincodeFlow()) {
            HandelsbankenConstants.DeviceAuthentication.OtherUserError.PINCODE_CREATION_NEEDED.throwException();
        }
    }

}
