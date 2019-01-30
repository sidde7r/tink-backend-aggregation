package se.tink.backend.aggregation.agents.banks.se.marginalenbank;

import se.tink.backend.aggregation.agents.banks.crosskey.errors.AbstractCrossKeyErrorHandler;
import se.tink.backend.aggregation.agents.banks.crosskey.errors.exceptions.UnexpectedErrorException;

public class MarginalenBankErrorHandler extends AbstractCrossKeyErrorHandler {

    @Override
    public void handleError(String errorCode) throws Exception {
        switch (errorCode) {
        case "UNEXPECTED_ERROR":
            throw new UnexpectedErrorException("Encountered a technical error, please try again later");
        default:
            super.handleError(errorCode);
        }
    }
}
