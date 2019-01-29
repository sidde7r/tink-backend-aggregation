package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.authenticator;

import com.google.common.base.Strings;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import se.tink.backend.agents.rpc.Field;
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
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.agents.rpc.Credentials;

public class IngAuthenticator implements Authenticator {

    private static final Pattern NIE_PATTERN = Pattern.compile("(?i)^[XY].+[A-Z]$");

    private final IngApiClient apiClient;
    private final SessionStorage sessionStorage;

    public IngAuthenticator(IngApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    // Currently only know the difference between NIE and NON_NIE types (NON_NIE might contain more types).
    private static int getUsernameType(String username) {
        if (NIE_PATTERN.matcher(username).matches()) {
            return IngConstants.UsernameType.NIE;
        }

        return IngConstants.UsernameType.NON_NIE;
    }

    @Override
    public void authenticate(Credentials credentials) throws AuthenticationException, AuthorizationException {

        String username = credentials.getField(Field.Key.USERNAME);
        String dateOfBirth = credentials.getField(IngConstants.DATE_OF_BIRTH);
        String pin = credentials.getField(Field.Key.PASSWORD);

        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(dateOfBirth) || Strings.isNullOrEmpty(pin)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        LoginID loginId = LoginID.create(username, dateOfBirth, getUsernameType(username));

        LoginPinPad pinpad;
        try {
            pinpad = apiClient.postLoginRestSession(loginId);
        } catch (HttpResponseException hre) {

            HttpResponse response = hre.getResponse();

            if (response.getStatus() == HttpStatus.SC_BAD_REQUEST) {
                ErrorResponse errorResponse = response.getBody(ErrorResponse.class);
                Optional<String> errorSummary = errorResponse.getErrorSummary();

                if (errorResponse.hasErrorCode(IngConstants.ErrorCode.INVALID_LOGIN_DOCUMENT_TYPE)) {
                    // This should not happen, if it does: The method `getUsernameType` is wrong.
                    throw new IllegalStateException(String.format("Invalid username type: %s",
                            errorSummary.orElse(null)));
                }
                // Fall through and re-throw original exception.
            }

            // Re-throw the exception.
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
        List<Integer> identifiedPositions = positionsOfDigitsToIdentify.stream()
                .map(endIndex -> Integer.valueOf(pin.substring(endIndex - 1, endIndex)))
                .map(pinPadNumbers::indexOf)
                .collect(Collectors.toList());

        return new LoginPinPositions(identifiedPositions);
    }
}
