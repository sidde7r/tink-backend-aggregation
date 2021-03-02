package se.tink.backend.aggregation.agents.exceptions.nemid;

public class NemIdPollTimeoutException extends NemIdException {

    public NemIdPollTimeoutException() {
        super(NemIdError.TIMEOUT);
    }
}
