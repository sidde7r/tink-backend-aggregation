package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator.rpc.bankid;

import java.util.function.Supplier;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.http.URL;

public class InitBankIdResponse extends BaseResponse {

    public InitBankIdResponse validate(Supplier<InitBankIdResponse> retryAction) throws BankIdException {

        String initCode = getCode();

        if (HandelsbankenSEConstants.BankIdAuthentication.UNKNOWN_BANKID.equals(initCode)) {
            throw BankIdError.USER_VALIDATION_ERROR.exception();
        }
        if (HandelsbankenSEConstants.BankIdAuthentication.CANCELLED.equals(initCode)) {
            return retryAction.get();
        }

        return this;
    }

    public URL toAuthenticate() {
        return findLink(HandelsbankenConstants.URLS.Links.AUTHENTICATE);
    }
}
