package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.authenticator;

import com.google.common.base.Strings;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.authenticator.rpc.LoginID;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.authenticator.rpc.LoginPinPad;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.authenticator.rpc.LoginPinPositions;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.authenticator.rpc.LoginTicket;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.Field;

public class IngAuthenticator implements Authenticator {

    private final IngApiClient apiClient;
    private final SessionStorage sessionStorage;

    public IngAuthenticator(IngApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(Credentials credentials) throws AuthenticationException, AuthorizationException {

        String username = credentials.getField(Field.Key.USERNAME);
        String dateOfBirth = credentials.getField(IngConstants.DATE_OF_BIRTH);
        String pin = credentials.getField(Field.Key.PASSWORD);

        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(dateOfBirth) || Strings.isNullOrEmpty(pin)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        LoginID userID = new LoginID(username, dateOfBirth);

        LoginPinPad pinpad = apiClient.postLoginRestSession(userID);
        if (pinpad.hasError()) {
            if (pinpad.hasErrorCode(IngConstants.ErrorCode.INVALID_LOGIN_DOCUMENT_TYPE)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }

            Optional<String> errorSummary = pinpad.getErrorSummary();
            throw new IllegalStateException(String.format("Unknown login error: %s", errorSummary.orElse("null")));
        }

        LoginPinPositions positions = this.positions(pinpad, pin);

        LoginTicket loginTicket = apiClient.putLoginRestSession(positions);

        String ticket = loginTicket.getTicket();

        sessionStorage.put(IngConstants.Query.TICKET, ticket);

        if (!apiClient.postApiLoginAuthResponse(ticket)) {
            throw AuthorizationError.UNAUTHORIZED.exception();
        }
    }

    private LoginPinPositions positions(LoginPinPad pinPad, String pin) {

        // 1-based positions of the pin digits to respond with
        List<Integer> positionsOfDigitsToIdentify = pinPad.getPinPositions();

        // The pin pad (keyboard) on which we shall identify the digits
        List<Integer> pinPadNumbers = pinPad.getPinPadNumbers();

        // List of indices pointing to where on the pin pad the digits are
        List<Integer> identifiedPositions = positionsOfDigitsToIdentify.stream()
                .map(endIndex -> Integer.valueOf(pin.substring(endIndex - 1, endIndex)))
                .map(pinPadNumbers::indexOf)
                .collect(Collectors.toList());

        return new LoginPinPositions(identifiedPositions);
    }
}
