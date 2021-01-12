package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator;

import java.util.Optional;
import java.util.function.Supplier;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.BusinessMessageResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.CheckLoginResultResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.EasyPinActivateResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.EasyPinCreateResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.ExecuteSignResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.InitializeLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.InitiateSignResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.legacy.UserInfoResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.IncorrectCardReaderResponseCodeError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.IncorrectOtpError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.InvalidCredentialsError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ServerError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.SessionExpiredError;

public class AuthenticationErrorHandler {

    public static AgentBankApiError getError(CheckLoginResultResponse response, boolean manual) {
        if (manual) {
            return defaultHandling(response, IncorrectCardReaderResponseCodeError::new);
        } else {
            return defaultHandling(response, SessionExpiredError::new);
        }
    }

    public static AgentBankApiError getError(UserInfoResponse response) {
        Optional<String> pewCode = getPewCode(response);

        if (pewCode.isPresent()) {
            String code = pewCode.get();
            if ("PEW0500".equals(code)) {
                return new ServerError();
            }
            return new ServerError();
        }
        throw new IllegalStateException("No error, check error first");
    }

    public static AgentBankApiError getError(EasyPinCreateResponse response) {
        return defaultHandling(response, InvalidCredentialsError::new);
    }

    public static AgentBankApiError getError(EasyPinActivateResponse response) {
        return defaultHandling(response, IncorrectOtpError::new);
    }

    public static AgentBankApiError getError(InitializeLoginResponse response, boolean manual) {
        if (manual) {
            return defaultHandling(response, InvalidCredentialsError::new);
        } else {
            return defaultHandling(response, SessionExpiredError::new);
        }
    }

    public static AgentBankApiError getError(InitiateSignResponse response) {
        // this is unused, however implemented if needed in future
        return defaultHandling(response, InvalidCredentialsError::new);
    }

    public static AgentBankApiError getError(ExecuteSignResponse response) {
        // this is unused, however implemented if needed in future
        return defaultHandling(response, InvalidCredentialsError::new);
    }

    public static AgentBankApiError defaultHandling(
            BusinessMessageResponse<?> response, Supplier<AgentBankApiError> pew0501Supplier) {
        Optional<String> pewCode = getPewCode(response);

        if (pewCode.isPresent()) {
            String code = pewCode.get();
            if ("PEW0500".equals(code)) {
                return new ServerError();
            } else if ("PEW0501".equals(code)) {
                return pew0501Supplier.get();
            }
            return new ServerError();
        }
        throw new IllegalStateException("No error, check error first");
    }

    private static Optional<String> getPewCode(BusinessMessageResponse<?> businessMessageResponse) {
        Object pewCode = businessMessageResponse.getBusinessMessageBulk().getPewCode();
        if (null == pewCode) {
            return Optional.empty();
        }
        if (pewCode instanceof String) {
            return Optional.of((String) pewCode);
        }
        return Optional.empty();
    }
}
