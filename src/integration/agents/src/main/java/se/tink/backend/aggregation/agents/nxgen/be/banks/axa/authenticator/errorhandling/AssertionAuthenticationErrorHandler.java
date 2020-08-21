package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.errorhandling;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.AssertResponseData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.BaseResponse;

public class AssertionAuthenticationErrorHandler extends ResponseErrorHandler {

    @Override
    void process(BaseResponse<?> response) {
        if (response.getData() instanceof AssertResponseData) {
            AssertResponseData data = (AssertResponseData) response.getData();
            Integer errorCode = Optional.ofNullable(data.getAssertionErrorCode()).orElse(0);
            if (errorCode != 0) {
                throw LoginError.DEFAULT_MESSAGE.exception();
            }
        }
    }
}
