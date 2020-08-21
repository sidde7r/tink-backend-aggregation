package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.errorhandling;

import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.BaseResponse;

public abstract class ResponseErrorHandler {

    ResponseErrorHandler nextHandler;

    public void handleError(BaseResponse<?> response) {
        process(response);
        if (nextHandler != null) {
            nextHandler.handleError(response);
        }
    }

    abstract void process(BaseResponse<?> response);
}
