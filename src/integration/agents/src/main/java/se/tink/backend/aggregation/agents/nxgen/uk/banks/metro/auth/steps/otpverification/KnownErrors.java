package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.otpverification;

import static io.vavr.API.$;

import agents_platform_agents_framework.org.springframework.http.ResponseEntity;
import io.vavr.API;
import io.vavr.API.Match.Case;
import io.vavr.control.Option;
import java.util.Arrays;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.error.UnknownError;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.ErrorResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AccountBlockedError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.DeviceRegistrationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.IncorrectOtpError;
import se.tink.libraries.serialization.utils.SerializationUtils;

enum KnownErrors {
    INVALID_OTP(
            API.Case(
                    $(
                            res ->
                                    !Option.of(res)
                                            .filter(ResponseEntity::hasBody)
                                            .map(
                                                    r ->
                                                            SerializationUtils
                                                                    .deserializeFromString(
                                                                            r.getBody(),
                                                                            ErrorResponse.class))
                                            .filter(er -> er.hasErrorCode(7))
                                            .isEmpty()),
                    new IncorrectOtpError())),
    ACCOUNT_BLOCKED(
            API.Case(
                    $(
                            res ->
                                    !Option.of(res)
                                            .filter(ResponseEntity::hasBody)
                                            .map(
                                                    r ->
                                                            SerializationUtils
                                                                    .deserializeFromString(
                                                                            r.getBody(),
                                                                            ErrorResponse.class))
                                            .filter(ErrorResponse::isAccountLocked)
                                            .isEmpty()),
                    new AccountBlockedError())),
    TOO_MANY_DEVICES(
            API.Case(
                    $(
                            res ->
                                    !API.Option(res)
                                            .filter(ResponseEntity::hasBody)
                                            .map(
                                                    r ->
                                                            SerializationUtils
                                                                    .deserializeFromString(
                                                                            r.getBody(),
                                                                            ErrorResponse.class))
                                            .filter(err -> err.hasErrorCode(4001))
                                            .isEmpty()),
                    new DeviceRegistrationError())),
    UNKNOWN_ERROR(
            API.Case(
                    $(res -> res.getStatusCode().isError()),
                    res -> new UnknownError(res.getStatusCode(), res.getBody())));

    private final Case<ResponseEntity<String>, AgentBankApiError> definitionOfCase;

    KnownErrors(Case<ResponseEntity<String>, AgentBankApiError> definitionOfCase) {
        this.definitionOfCase = definitionOfCase;
    }

    private Case<ResponseEntity<String>, AgentBankApiError> getCase() {
        return definitionOfCase;
    }

    static Case<ResponseEntity<String>, AgentBankApiError>[] getCases() {
        return Arrays.stream(values()).map(KnownErrors::getCase).toArray(Case[]::new);
    }
}
