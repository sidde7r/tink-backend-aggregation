package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.authenticator;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
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

        LoginID userID = new LoginID(username, dateOfBirth);

        LoginPinPad pinpad = apiClient.postLoginRestSession(userID);

        LoginPinPositions positions = this.positions(pinpad, credentials);

        LoginTicket loginTicket = apiClient.putLoginRestSession(positions);

        String ticket = loginTicket.getTicket();

        sessionStorage.put(IngConstants.Query.TICKET, ticket);

        if (!apiClient.postApiLoginAuthResponse(ticket)) {
            throw AuthorizationError.UNAUTHORIZED.exception();
        }
    }

    private LoginPinPositions positions(LoginPinPad pinPad, Credentials credentials) throws LoginException {

        // A string of 6 digits
        String pin = credentials.getField(Field.Key.PASSWORD);

        // 1-based positions of the pin digits to respond with
        List<Integer> positionsOfDigitsToIdentify = pinPad.getPinPositions();
        int numberOfDigitsToIdentify = positionsOfDigitsToIdentify.size();

        // The pin pad (keyboard) on which we shall identify the digits
        List<Integer> pinPadNumbers = pinPad.getPinPadNumbers();

        // List of indices pointing to where on the pin pad the digits are
        List<Integer> identifiedPositions = positionsOfDigitsToIdentify.stream()
                .map(endIndex -> Integer.valueOf(pin.substring(endIndex - 1, endIndex)))
                .map(digitToFind -> pinPadNumbers.indexOf(digitToFind))
                .collect(Collectors.toList());

        return new LoginPinPositions(identifiedPositions);
    }
}
