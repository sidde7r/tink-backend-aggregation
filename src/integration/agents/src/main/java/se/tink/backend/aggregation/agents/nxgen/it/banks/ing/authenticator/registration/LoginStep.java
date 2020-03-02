package se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration;

import static java.time.LocalDate.parse;
import static java.time.format.DateTimeFormatter.ofPattern;
import static se.tink.backend.agents.rpc.Field.Key.PASSWORD;
import static se.tink.backend.agents.rpc.Field.Key.USERNAME;
import static se.tink.backend.aggregation.agents.nxgen.it.banks.ing.IngAgentConstants.DATE_OF_BIRTH;
import static se.tink.backend.aggregation.agents.nxgen.it.banks.ing.IngAgentConstants.DATE_OF_BIRTH_FORMAT;
import static se.tink.backend.aggregation.agents.nxgen.it.banks.ing.IngAgentConstants.DEVICE_ID_LENGTH;
import static se.tink.backend.aggregation.agents.nxgen.it.banks.ing.IngAgentConstants.Storage.DEVICE_ID;
import static se.tink.backend.aggregation.agents.nxgen.it.banks.ing.IngAgentConstants.Storage.JSESSION_ID;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.RandomDataProvider;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.Login1ExternalApiCall.Result;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.scaffold.UserInteractionStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.scaffold.ExternalApiCallResult;
import se.tink.backend.aggregation.nxgen.scaffold.SimpleExternalApiCall;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@Slf4j
@RequiredArgsConstructor
public class LoginStep implements UserInteractionStep {

    private static final DateTimeFormatter FORMATTER = ofPattern(DATE_OF_BIRTH_FORMAT);

    private final SessionStorage sessionStorage;
    private final RandomDataProvider randomDataProvider;
    private final Login1ExternalApiCall call1;
    private final Login2ExternalApiCall call2;

    @Override
    public SteppableAuthenticationResponse execute(SteppableAuthenticationRequest request)
            throws LoginException {
        try {
            Credentials credentials = request.getPayload().getCredentials();
            Login1ExternalApiCall.Result login1Result = executeLogin1Call(credentials);
            Login2ExternalApiCall.Result login2Result =
                    executeLogin2Call(credentials, login1Result);
            storeJSessionId(login2Result.getJSessionId());

            return SteppableAuthenticationResponse.finalResponse();
        } catch (IllegalArgumentException | DateTimeParseException ex) {
            throw LoginError.INCORRECT_CREDENTIALS.exception(ex);
        }
    }

    private Result executeLogin1Call(Credentials credentials) throws LoginException {
        return executeCall(call1, buildArg1(credentials));
    }

    private Login2ExternalApiCall.Result executeLogin2Call(Credentials credentials, Result result1)
            throws LoginException {
        return executeCall(call2, buildArg2(credentials, result1));
    }

    private void storeJSessionId(String jSessionId) {
        sessionStorage.put(JSESSION_ID, jSessionId);
    }

    private Login1ExternalApiCall.Arg buildArg1(Credentials credentials) {
        return new Login1ExternalApiCall.Arg()
                .setPersonId(getUsername(credentials))
                .setBirthDate(getDateOfBirth(credentials))
                .setDeviceId(getOrGenerateDeviceId());
    }

    private String getOrGenerateDeviceId() {
        return sessionStorage.computeIfAbsent(DEVICE_ID, x -> generateNewDeviceId());
    }

    private String generateNewDeviceId() {
        return randomDataProvider.generateRandomBase64UrlEncoded(DEVICE_ID_LENGTH);
    }

    private String getUsername(Credentials credentials) {
        return getRequiredStringArgument(credentials, USERNAME);
    }

    private LocalDate getDateOfBirth(Credentials credentials) {
        return Optional.ofNullable(credentials)
                .map(c -> c.getField(DATE_OF_BIRTH))
                .map(str -> parse(str, FORMATTER))
                .orElseThrow(IllegalArgumentException::new);
    }

    private <T, R> R executeCall(SimpleExternalApiCall<T, R> call, T arg) throws LoginException {
        return Optional.ofNullable(call.execute(arg))
                .filter(ExternalApiCallResult::is2xxSuccess)
                .map(ExternalApiCallResult::getResult)
                .orElseThrow(LoginError.INCORRECT_CREDENTIALS::exception);
    }

    private Login2ExternalApiCall.Arg buildArg2(Credentials credentials, Result result1) {
        return new Login2ExternalApiCall.Arg()
                .setDeviceId(getOrGenerateDeviceId())
                .setPin(getPin(credentials))
                .setPinKeyboardMap(result1.getPinKeyboardMap())
                .setPinPositions(result1.getPinNumberPositions());
    }

    private String getPin(Credentials credentials) {
        return getRequiredStringArgument(credentials, PASSWORD);
    }

    private String getRequiredStringArgument(Credentials credentials, Key fieldKey) {
        return Optional.ofNullable(credentials)
                .map(c -> c.getField(fieldKey))
                .orElseThrow(IllegalArgumentException::new);
    }
}
