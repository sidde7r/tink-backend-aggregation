package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.authenticator;

import com.google.common.base.Strings;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.authenticator.rpc.LoginID;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.authenticator.rpc.LoginPinPad;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.authenticator.rpc.LoginPinPositions;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.authenticator.rpc.LoginTicket;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class IngAuthenticator implements Authenticator {

    private static final Pattern NIF_PATTERN = Pattern.compile("^[0-9]{8}[a-zA-Z]$");
    private static final Pattern NIE_PATTERN = Pattern.compile("^[a-zA-Z][0-9]{7}[a-zA-Z]$");
    private static final Pattern PASSPORT_PATTERN = Pattern.compile("^[a-zA-Z]{2}[0-9]{6}$");

    private final IngApiClient apiClient;
    private final SessionStorage sessionStorage;

    public IngAuthenticator(IngApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    /**
     * Validates username type and returns the corresponding document type number.
     *
     * <p>NIF rule: 8 digits followed by 1 letter, DDDDDDDDL NIE rule: 1 letter followed by 7 digits
     * followed by 1 letter, LDDDDDDDL PASSPORT rule: 2 letters followed by 6 digits, LLDDDDDD
     *
     * <p>These are the only possible formats that I've found that the ING accept on the frontend
     * side. Anything I try outside these formats result in "incorrect format" error.
     */
    public static int getUsernameType(String username) throws LoginException {

        if (NIF_PATTERN.matcher(username).matches()) {
            return IngConstants.UsernameType.NIF;
        }

        if (NIE_PATTERN.matcher(username).matches()) {
            return IngConstants.UsernameType.NIE;
        }

        if (PASSPORT_PATTERN.matcher(username).matches()) {
            return IngConstants.UsernameType.PASSPORT;
        }

        // Shouldn't happen since the same regex is in the provider configuration for username
        throw LoginError.INCORRECT_CREDENTIALS.exception();
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {

        String username = credentials.getField(Field.Key.USERNAME);
        String dateOfBirth = credentials.getField(IngConstants.DATE_OF_BIRTH);
        String pin = credentials.getField(Field.Key.PASSWORD);

        if (Strings.isNullOrEmpty(username)
                || Strings.isNullOrEmpty(dateOfBirth)
                || Strings.isNullOrEmpty(pin)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        LoginID loginId = LoginID.create(username, dateOfBirth, getUsernameType(username));

        LoginPinPad pinpad;
        try {
            pinpad = apiClient.postLoginRestSession(loginId);
        } catch (HttpResponseException hre) {

            HttpResponse response = hre.getResponse();

            if (response.getStatus() == HttpStatus.SC_FORBIDDEN) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }

            throw hre;
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
        List<Integer> identifiedPositions =
                positionsOfDigitsToIdentify.stream()
                        .map(endIndex -> Integer.valueOf(pin.substring(endIndex - 1, endIndex)))
                        .map(pinPadNumbers::indexOf)
                        .collect(Collectors.toList());

        return new LoginPinPositions(identifiedPositions);
    }
}
