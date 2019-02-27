package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants.Logging;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.CreateSessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.PutRestSessionResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.libraries.serialization.utils.SerializationUtils;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;

public class IngAuthenticator implements Authenticator {
    private static final AggregationLogger LOGGER = new AggregationLogger(IngAuthenticator.class);

    private static final Pattern NIF_PATTERN = Pattern.compile("^[0-9]{8}[a-zA-Z]$");
    private static final Pattern NIE_PATTERN = Pattern.compile("^[a-zA-Z][0-9]{7}[a-zA-Z]$");
    private static final Pattern PASSPORT_PATTERN = Pattern.compile("^[a-zA-Z]{2}[0-9]{6}$");

    private IngApiClient apiClient;

    public IngAuthenticator(IngApiClient apiClient) {
        this.apiClient = apiClient;
    }


    @Override
    public void authenticate(Credentials credentials) throws AuthenticationException, AuthorizationException {
        String username = credentials.getField(Field.Key.USERNAME);
        String dob = credentials.getField(IngConstants.DATE_OF_BIRTH);
        String password = credentials.getField(Field.Key.PASSWORD);

        try {
            Preconditions.checkArgument(!isNullOrEmpty(username), "Username is null or empty");
            Preconditions.checkArgument(!isNullOrEmpty(dob), "DOB is null or empty");
            Preconditions.checkArgument(!isNullOrEmpty(password), "Password is null or empty");
        } catch (IllegalArgumentException ex) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        CreateSessionResponse response = apiClient.postLoginRestSession(username, getUsernameType(username), dob);

        List<Integer> pinPositions = getPinPositionsForPassword(getPasswordStringAsIntegerList(password),
                response.getPinPadNumbers(),
                response.getPinPositions());

        PutRestSessionResponse putSessionResponse = apiClient.putLoginRestSession(pinPositions);

        apiClient.postLoginAuthResponse(putSessionResponse.getTicket());
    }

    /**
     * Map the 6 digits of a users password, along with the metadata about how the pinpad should be displayed and which
     * parts of the password the user is being challenged for, to a list of integer indicies for the buttons the user
     * would have pressed to enter the password.
     *
     * i.e.
     *  getPinPositionsForPassword([1,2,3,4,5,6], [9,8,7,6,5,4,3,2,1,0], [1, 3, 5]) -> [8, 6, 4]
     * @param password The user's 6-character password, split into a list of integers between 0-9
     * @param pinPadNumbers the order in which the numbers on the pinpad are displayed to the user.
     * @param pinPositions The indices within the password that the user is being challenged for. ING supplies these
     *                     indexed from 1
     * @return the position on the pinpad the user would have to have pressed to have entered the challenged-for digits
     * of their password. ING expects a 0-based index
     */
    @VisibleForTesting
    static List<Integer> getPinPositionsForPassword(final List<Integer> password,
                                                    final List<Integer> pinPadNumbers,
                                                    final List<Integer> pinPositions) throws LoginException {

        // first, reverse the mapping of the pinpad numbers. This provides a direct mapping from "number in password"
        // to "expected key index"
        Integer[] pwdNumberToIndex = new Integer[10];
        try {
            for (int ii = 0; ii < pinPadNumbers.size(); ii++) {
                pwdNumberToIndex[pinPadNumbers.get(ii)] = ii;
            }

            for (Integer ii : pwdNumberToIndex) {
                if (ii == null) {
                    throw new IllegalArgumentException("Invalid pinpad numbers - not all numbers were mapped");
                }
            }
        } catch (IllegalArgumentException | IndexOutOfBoundsException ex) {
            LOGGER.infoExtraLong(SerializationUtils.serializeToString(pinPadNumbers), Logging.INVALID_PINPAD_NUMBERS, ex);
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        // then, lookup the required indexes in the map
        try {
            return pinPositions
                    .stream()
                    .map(
                            idx ->
                                    pwdNumberToIndex[password.get(idx - 1)]
                    )
                    .collect(Collectors.toList());
        } catch (IndexOutOfBoundsException ex) {
            LOGGER.infoExtraLong(SerializationUtils.serializeToString(pinPositions), Logging.MISSING_PINPAD_POSITION, ex);
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }

    @VisibleForTesting
    static List<Integer> getPasswordStringAsIntegerList(String password) throws LoginException {
        try {
            return password
                    .codePoints().mapToObj(c -> String.valueOf((char) c)) // map to a stream of 1-character strings
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException ex) {
            LOGGER.infoExtraLong("Non numeric character encountered in password", Logging.NON_NUMERIC_PASSWORD, ex);
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }

    /**
     * Validates username type and returns the corresponding document type number.
     *
     * NIF rule: 8 digits followed by 1 letter, DDDDDDDDL
     * NIE rule: 1 letter followed by 7 digits followed by 1 letter, LDDDDDDDL
     * PASSPORT rule: 2 letters followed by 6 digits, LLDDDDDD
     *
     * These are the only possible formats that I've found that the ING accept on the frontend side. Anything I try
     * outside these formats result in "incorrect format" error.
     */
    private static int getUsernameType(String username) throws LoginException {

        if (NIF_PATTERN.matcher(username).matches()) {
            return IngConstants.UsernameTypes.NIF;
        }

        if (NIE_PATTERN.matcher(username).matches()) {
            return IngConstants.UsernameTypes.NIE;
        }

        if (PASSPORT_PATTERN.matcher(username).matches()) {
            return IngConstants.UsernameTypes.PASSPORT;
        }

        // Shouldn't happen since the same regex is in the provider configuration for username
        throw LoginError.INCORRECT_CREDENTIALS.exception();
    }
}
