package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.error.NemIdError;

public class NemIdPollTimeoutException extends NemIdException {

    public NemIdPollTimeoutException() {
        super(NemIdError.TIMEOUT);
    }
}
