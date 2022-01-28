package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants.EnrollmentValues.ENROLLMENT_OK;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants.EnrollmentValues.ENROLLMENT_REQUIRED;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants.EnrollmentValues.MAX_ENROLLMENT_REQUESTS;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants.EnrollmentValues.VALIDATION_TYPE_PUSH;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants.EnrollmentValues.VALIDATION_TYPE_SCA;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants.EnrollmentValues.VALIDATION_TYPE_SMS;

import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants.DefaultRequestParams;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc.EnrollmentResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc.ImaginSessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc.ScaEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc.SmsEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.libraries.cryptography.LaCaixaPasswordHash;

public class ImaginBankPasswordAuthenticator implements MultiFactorAuthenticator {
    private final ImaginBankApiClient apiClient;
    private final ImaginBankSessionStorage sessionStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;

    public ImaginBankPasswordAuthenticator(
            ImaginBankApiClient apiClient,
            ImaginBankSessionStorage sessionStorage,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        final String username = credentials.getField(Field.Key.USERNAME);
        final String password = credentials.getField(Field.Key.PASSWORD);
        sessionStorage.setUsername(username);
        // Requests a session ID from the server in the form of a cookie.
        // Also gets seed for password hashing.
        SessionResponse sessionResponse = apiClient.initializeSession(username);
        ImaginSessionResponse imaginSessionResponse = sessionResponse.getResImagin();

        if (imaginSessionResponse == null) {
            throw LoginError.NOT_SUPPORTED.exception(
                    "Unsupported flow for userType = " + sessionResponse.getUserType());
        }

        // Initialize password hasher with seed from initialization request.
        final String passwordHash =
                LaCaixaPasswordHash.hash(
                        imaginSessionResponse.getSeed(),
                        Integer.parseInt(imaginSessionResponse.getIterations()),
                        password);

        // Construct login request from username and hashed password
        LoginResponse loginResponse =
                apiClient.login(
                        new LoginRequest(
                                username,
                                sessionResponse.getUserType(),
                                DefaultRequestParams.EXISTS_USER,
                                passwordHash,
                                ImaginBankConstants.DefaultRequestParams.ALTA_IMAGINE,
                                ImaginBankConstants.DefaultRequestParams.DEMO));

        sessionStorage.setLoginResponse(loginResponse);
        if (loginResponse.getImaginLoginResponse() != null
                && ENROLLMENT_REQUIRED.equals(
                        loginResponse.getImaginLoginResponse().getEnrollmentIndicator())) {
            enrolDevice(password);
        }
    }

    /**
     * Linking device
     *
     * @param password
     */
    private void enrolDevice(String password) {
        EnrollmentResponse enrollmentResponse = apiClient.initEnrollment();
        int requestsCounter = 0;
        while (!ENROLLMENT_OK.equals(enrollmentResponse.getStatus())
                && StringUtils.isNotEmpty(enrollmentResponse.getValidationType())
                && requestsCounter < MAX_ENROLLMENT_REQUESTS) {
            if (VALIDATION_TYPE_SCA.equals(enrollmentResponse.getValidationType())) {
                ScaEntity scaEntity = enrollmentResponse.getPin1SCA();
                final String secondPasswordHash =
                        LaCaixaPasswordHash.hash(
                                scaEntity.getSeed(),
                                Integer.parseInt(scaEntity.getIterations()),
                                password);
                enrollmentResponse = apiClient.doPasswordEnrollment(secondPasswordHash);
            } else if (VALIDATION_TYPE_SMS.equals(enrollmentResponse.getValidationType())
                    || VALIDATION_TYPE_PUSH.equals(enrollmentResponse.getValidationType())) {
                SmsEntity smsEntity = enrollmentResponse.getSms();
                String otpCode = supplementalInformationHelper.waitForOtpInput();
                final String thirdPasswordHash =
                        LaCaixaPasswordHash.hash(
                                smsEntity.getSeed(),
                                Integer.parseInt(smsEntity.getIterations()),
                                otpCode);
                enrollmentResponse = apiClient.doOtpEnrollment(thirdPasswordHash);
            }
            requestsCounter++;
        }
        if (!ENROLLMENT_OK.equals(enrollmentResponse.getStatus())) {
            throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
        }
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }
}
