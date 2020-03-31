package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants.Logging;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.CreateSessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.PutRestSessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AuthenticationControllerType;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.identitydata.countries.EsIdentityDocumentType;

public class IngAuthenticator implements Authenticator, AuthenticationControllerType {
    private static final AggregationLogger LOGGER = new AggregationLogger(IngAuthenticator.class);

    private static final Pattern NIF_PATTERN = Pattern.compile("^[0-9]{8}[a-zA-Z]$");
    private static final Pattern NIE_PATTERN = Pattern.compile("^[xyzXYZ][0-9]{7}[a-zA-Z]$");
    private static final Pattern PASSPORT_PATTERN = Pattern.compile("^[a-zA-Z0-9]{6,10}$");
    private final PersistentStorage persistentStorage;

    private IngApiClient apiClient;

    public IngAuthenticator(IngApiClient apiClient, PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        String username = credentials.getField(Field.Key.USERNAME);
        String dob = credentials.getField(IngConstants.DATE_OF_BIRTH);
        String password = credentials.getField(Field.Key.PASSWORD);

        try {
            Preconditions.checkArgument(!isNullOrEmpty(username), "Username is null or empty");
            Preconditions.checkArgument(!isNullOrEmpty(dob), "DOB is null or empty");
            Preconditions.checkArgument(!isNullOrEmpty(password), "Password is null or empty");
        } catch (IllegalArgumentException ex) {
            throw LoginError.INCORRECT_CREDENTIALS.exception(ex);
        }

        final CreateSessionResponse response;
        try {
            response =
                    apiClient.postLoginRestSession(
                            username, getUsernameType(username), dob, getDeviceId());
        } catch (HttpResponseException hre) {
            if (hre.getResponse().getStatus() == HttpStatus.SC_BAD_REQUEST) {
                final ErrorResponse errorResponse = hre.getResponse().getBody(ErrorResponse.class);
                if (errorResponse.hasErrorField(ErrorCodes.LOGIN_DOCUMENT_FIELD)) {
                    // login document didn't pass server-side validation
                    throw LoginError.INCORRECT_CREDENTIALS.exception(hre);
                }
            } else if (hre.getResponse().getStatus() == HttpStatus.SC_FORBIDDEN) {
                final ErrorResponse errorResponse = hre.getResponse().getBody(ErrorResponse.class);
                if (errorResponse.hasErrorCode(ErrorCodes.MOBILE_VALIDATION_ENROLLMENT_REQUIRED)) {
                    // mobile validation enrollment required
                    throw LoginError.NO_ACCESS_TO_MOBILE_BANKING.exception(hre);
                } else if (errorResponse.hasErrorCode(ErrorCodes.GENERIC_LOCK)) {
                    // account blocked
                    throw AuthorizationError.ACCOUNT_BLOCKED.exception(hre);
                }
            }
            throw hre;
        }

        if (!response.hasPinPad()) {
            // When the app receives a response with no pinpad and "view":"OTHERS", it does a POST
            // to /genoma_login/rest/pin-recovery/ and returns to the initial view
            LOGGER.error("Unexpected login response");
            throw AuthorizationError.ACCOUNT_BLOCKED.exception();
        }

        List<Integer> pinPositions =
                getPinPositionsForPassword(
                        getPasswordStringAsIntegerList(password),
                        response.getPinPadNumbers(),
                        response.getPinPositions());

        try {
            PutRestSessionResponse putSessionResponse =
                    apiClient.putLoginRestSession(pinPositions, response.getProcessId());
            apiClient.postLoginAuthResponse(putSessionResponse.getTicket());
        } catch (HttpResponseException hre) {
            handleErrors(hre);
        }
    }

    /**
     * Map the 6 digits of a users password, along with the metadata about how the pinpad should be
     * displayed and which parts of the password the user is being challenged for, to a list of
     * integer indicies for the buttons the user would have pressed to enter the password.
     *
     * <p>i.e. getPinPositionsForPassword([1,2,3,4,5,6], [9,8,7,6,5,4,3,2,1,0], [1, 3, 5]) -> [8, 6,
     * 4]
     *
     * @param password The user's 6-character password, split into a list of integers between 0-9
     * @param pinPadNumbers the order in which the numbers on the pinpad are displayed to the user.
     * @param pinPositions The indices within the password that the user is being challenged for.
     *     ING supplies these indexed from 1
     * @return the position on the pinpad the user would have to have pressed to have entered the
     *     challenged-for digits of their password. ING expects a 0-based index
     */
    @VisibleForTesting
    static List<Integer> getPinPositionsForPassword(
            final List<Integer> password,
            final List<Integer> pinPadNumbers,
            final List<Integer> pinPositions)
            throws LoginException {

        // first, reverse the mapping of the pinpad numbers. This provides a direct mapping from
        // "number in password"
        // to "expected key index"
        Integer[] pwdNumberToIndex = new Integer[10];
        try {
            for (int ii = 0; ii < pinPadNumbers.size(); ii++) {
                pwdNumberToIndex[pinPadNumbers.get(ii)] = ii;
            }

            for (Integer ii : pwdNumberToIndex) {
                if (ii == null) {
                    throw new IllegalArgumentException(
                            "Invalid pinpad numbers - not all numbers were mapped");
                }
            }
        } catch (IllegalArgumentException | IndexOutOfBoundsException ex) {
            throw LoginError.INCORRECT_CREDENTIALS.exception(ex);
        }

        // then, lookup the required indexes in the map
        try {
            return pinPositions.stream()
                    .map(idx -> pwdNumberToIndex[password.get(idx - 1)])
                    .collect(Collectors.toList());
        } catch (IndexOutOfBoundsException ex) {
            throw LoginError.INCORRECT_CREDENTIALS.exception(ex);
        }
    }

    @VisibleForTesting
    static List<Integer> getPasswordStringAsIntegerList(String password) throws LoginException {
        try {
            return password.codePoints()
                    .mapToObj(
                            c -> String.valueOf((char) c)) // map to a stream of 1-character strings
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException ex) {
            LOGGER.infoExtraLong(
                    "Non numeric character encountered in password",
                    Logging.NON_NUMERIC_PASSWORD,
                    ex);
            throw LoginError.INCORRECT_CREDENTIALS.exception(ex);
        }
    }

    /**
     * Validates username type and returns the corresponding document type number.
     *
     * <p>NIF rule: 8 digits followed by checksum letter: DDDDDDDDC
     *
     * <p>NIE rule: X, Y or Z followed by 7 digits, followed by checksum letter: XDDDDDDDC
     *
     * <p>PASSPORT rule: 6 to 10 letters and digits: WWWWWWWWWW
     *
     * <p>Note: a NIF or NIE with a wrong checksum letter will be matched as a valid passport.
     */
    @VisibleForTesting
    static int getUsernameType(String username) throws LoginException {

        if (NIF_PATTERN.matcher(username).matches()
                && EsIdentityDocumentType.isValidNif(username.toUpperCase(Locale.ROOT))) {
            return IngConstants.UsernameTypes.NIF;
        }

        if (NIE_PATTERN.matcher(username).matches()
                && EsIdentityDocumentType.isValidNie(username.toUpperCase(Locale.ROOT))) {
            return IngConstants.UsernameTypes.NIE;
        }

        if (PASSPORT_PATTERN.matcher(username).matches()) {
            return IngConstants.UsernameTypes.PASSPORT;
        }

        // Shouldn't happen since the same regex is in the provider configuration for username
        throw LoginError.INCORRECT_CREDENTIALS.exception();
    }

    private String getDeviceId() {
        if (!persistentStorage.containsKey(Storage.DEVICE_ID)) {
            persistentStorage.put(Storage.DEVICE_ID, RandomUtils.generateRandomHexEncoded(20));
        }
        return persistentStorage.get(Storage.DEVICE_ID);
    }

    private void handleErrors(HttpResponseException hre) throws LoginException {
        switch (hre.getResponse().getStatus()) {
            case HttpStatus.SC_FORBIDDEN:
                throw LoginError.INCORRECT_CREDENTIALS.exception(hre);
            case HttpStatus.SC_CONFLICT:
                throw BankServiceError.BANK_SIDE_FAILURE.exception(hre);
            default:
                throw hre;
        }
    }

    @Override
    public boolean isManualAuthentication(CredentialsRequest request) {
        return request.isUpdate() || request.isCreate();
    }
}
