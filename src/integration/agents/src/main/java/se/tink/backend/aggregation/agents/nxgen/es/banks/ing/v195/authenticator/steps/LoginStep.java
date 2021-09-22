package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.steps;

import com.google.api.client.util.Preconditions;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants.DeviceAction;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants.Logging;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants.ScaConfig;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngUtils;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.AuthenticationErrorHandler;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.CreateSessionRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.CreateSessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.PutRestSessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AbstractAuthenticationStep;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.identitydata.countries.EsIdentityDocumentType;

@Slf4j
@RequiredArgsConstructor
public class LoginStep extends AbstractAuthenticationStep {
    private static final Pattern NIF_PATTERN = Pattern.compile("^[0-9]{8}[a-zA-Z]$");
    private static final Pattern NIE_PATTERN = Pattern.compile("^[xyzXYZ][0-9]{7}[a-zA-Z]$");
    private static final Pattern PASSPORT_PATTERN = Pattern.compile("^[a-zA-Z0-9]{6,10}$");

    private final IngApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;
    private final RandomValueGenerator randomValueGenerator;
    private final Map<Integer, String> scaTypeToStepId;
    private final boolean isUserAvailableForInteraction;
    private boolean isMobileActivationNeeded;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        final Credentials credentials = request.getCredentials();
        CreateSessionResponse sessionResponse = getSession(credentials);
        sessionStorage.put(Storage.LOGIN_PROCESS_ID, sessionResponse.getProcessId());

        if (!sessionResponse.hasPinPad()) {
            // When the app receives a response with no pinpad and "view":"OTHERS", it does a POST
            // to /genoma_login/rest/pin-recovery/ and returns to the initial view
            log.error("Response did not contain pinpad");
            persistentStorage.remove(Storage.CREDENTIALS_TOKEN);
            throw AuthorizationError.ACCOUNT_BLOCKED.exception();
        }

        final String password =
                Optional.ofNullable(Strings.emptyToNull(credentials.getField(Field.Key.PASSWORD)))
                        .orElseThrow(
                                () ->
                                        LoginError.INCORRECT_CREDENTIALS.exception(
                                                "You didn't provide a password"));

        final List<Integer> pinPositions =
                getPinPositionsForPassword(
                        getPasswordStringAsIntegerList(password),
                        sessionResponse.getPinPadNumbers(),
                        sessionResponse.getPinPositions());

        PutRestSessionResponse updatedSession =
                updateSession(pinPositions, sessionResponse.getProcessId());

        sessionStorage.put(Storage.PERSON_ID, updatedSession.getPersonId());

        return routeAuthentication(updatedSession);
    }

    private CreateSessionResponse getSession(Credentials credentials) {
        try {
            return apiClient.postLoginRestSession(
                    createSessionRequest(credentials, DeviceAction.MOBILE_PHONE));
        } catch (HttpResponseException hre) {
            persistentStorage.remove(Storage.CREDENTIALS_TOKEN);
            final ErrorResponse errorResponse = hre.getResponse().getBody(ErrorResponse.class);
            if (errorResponse.hasErrorCode(ErrorCodes.MOBILE_VALIDATION_ENROLLMENT_REQUIRED)) {
                log.info("Mobile validation is needed");
                isMobileActivationNeeded = true;
                return apiClient.postLoginRestSession(
                        createSessionRequest(credentials, DeviceAction.MOBILE_VALIDATION));
            } else {
                AuthenticationErrorHandler.handlePostSessionErrors(hre);
                throw hre;
            }
        }
    }

    private PutRestSessionResponse updateSession(List<Integer> pinPositions, String processId) {
        try {
            return apiClient.putLoginRestSession(pinPositions, processId);
        } catch (HttpResponseException hre) {
            AuthenticationErrorHandler.handlePutSessionErrors(hre);
            throw hre;
        }
    }

    private AuthenticationStepResponse routeAuthentication(PutRestSessionResponse sessionResponse) {
        if (!Strings.isNullOrEmpty(sessionResponse.getTicket())) {
            persistentStorage.put(Storage.CREDENTIALS_TOKEN, sessionResponse.getRememberMeToken());

            if (isMobileActivationNeeded) {
                apiClient.postLoginAuthResponse(
                        sessionResponse.getTicket(), DeviceAction.MOBILE_VALIDATION);
                return AuthenticationStepResponse.executeStepWithId(
                        MobileValidationStep.class.getName());
            }

            apiClient.postLoginAuthResponse(sessionResponse.getTicket(), DeviceAction.MOBILE_PHONE);
            return AuthenticationStepResponse.authenticationSucceeded();
        } else if (sessionResponse.getResultCode() == ScaConfig.NEXT_STEP
                && sessionResponse.getNextValMethod() != null) {
            return handleSca(sessionResponse.getNextValMethod());
        }
        log.error("Did not get login ticket or SCA method.");
        throw LoginError.NOT_SUPPORTED.exception();
    }

    private AuthenticationStepResponse handleSca(Integer scaMethod)
            throws SessionException, LoginException {
        if (!isUserAvailableForInteraction) {
            log.warn("Got SCA on non-manual refresh");
            throw SessionError.SESSION_EXPIRED.exception();
        }
        if (scaTypeToStepId.containsKey(scaMethod)) {
            final String nextStepId = scaTypeToStepId.get(scaMethod);
            log.info("Handling SCA method {} ({})", scaMethod, nextStepId);
            return AuthenticationStepResponse.executeStepWithId(nextStepId);
        } else {
            log.warn("Unsupported SCA method: {}", scaMethod);
            throw LoginError.NOT_SUPPORTED.exception();
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
            log.warn(
                    "Non numeric character encountered in password",
                    Logging.NON_NUMERIC_PASSWORD,
                    ex);
            throw LoginError.INCORRECT_CREDENTIALS.exception(ex);
        }
    }

    private CreateSessionRequest createSessionRequest(Credentials credentials, String deviceAction)
            throws LoginException {
        if (persistentStorage.containsKey(Storage.CREDENTIALS_TOKEN)) {
            return CreateSessionRequest.fromCredentialsToken(
                    persistentStorage.get(Storage.CREDENTIALS_TOKEN), getDeviceId());
        }

        final String username = Strings.emptyToNull(credentials.getField(Field.Key.USERNAME));
        final String dateOfBirth =
                Strings.emptyToNull(credentials.getField(IngConstants.DATE_OF_BIRTH));

        Preconditions.checkNotNull(username);
        Preconditions.checkNotNull(dateOfBirth);

        final LocalDate birthday = LocalDate.parse(dateOfBirth, IngUtils.BIRTHDAY_INPUT);
        return CreateSessionRequest.fromUsername(
                username, getUsernameType(username), birthday, getDeviceId(), deviceAction);
    }

    private String getDeviceId() {
        if (!persistentStorage.containsKey(Storage.DEVICE_ID)) {
            persistentStorage.put(
                    Storage.DEVICE_ID, randomValueGenerator.generateRandomHexEncoded(20));
        }
        return persistentStorage.get(Storage.DEVICE_ID);
    }
}
