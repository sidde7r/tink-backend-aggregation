package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.errorhandling;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaConstants.Response;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.AssertResponseData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.ErrorsEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.ResponseDataEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.BaseResponse;

public class DetailedAuthenticationErrorHandler extends ResponseErrorHandler {

    @Override
    void process(BaseResponse<?> response) {
        if (response.getData() instanceof AssertResponseData) {
            AssertResponseData data = (AssertResponseData) response.getData();
            tryAnalyzeErrorAndThrow(data.getData());
        }
    }

    private void tryAnalyzeErrorAndThrow(ResponseDataEntity data) {
        Optional.ofNullable(data)
                .map(ResponseDataEntity::getError)
                .map(ErrorsEntity::getMsgCd)
                .ifPresent(this::throwDetailedException);
    }

    private void throwDetailedException(String msgCd) {
        switch (msgCd) {
            case Response.INCORRECT_CARD_NUMBER_CODE:
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            case Response.DEVICES_LIMIT_REACHED_CODE:
                throw LoginError.REGISTER_DEVICE_ERROR.exception();
            case Response.NOT_AN_ACTIVE_BANK_USER:
                throw LoginError.NOT_CUSTOMER.exception();
            case Response.INVALID_OTP_CODE:
                throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
            default:
                throw LoginError.DEFAULT_MESSAGE.exception();
        }
    }
}
