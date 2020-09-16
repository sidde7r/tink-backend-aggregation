package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception;

public class NemIdPollTimeoutException extends NemIdException {

    public NemIdPollTimeoutException() {
        super(NemIdError.TIMEOUT);
    }
}
