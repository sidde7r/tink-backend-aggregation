package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SdcPhoneNumbersEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SessionStorageAgreement;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SessionStorageAgreements;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc.AgreementsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc.ChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc.InvalidPinResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc.SdcAgreementServiceConfigurationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.SelectAgreementRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.smsotp.SmsOtpAuthenticatorPassword;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
public class SdcSmsOtpAuthenticator
        implements SmsOtpAuthenticatorPassword<SdcSmsOtpAuthenticator.InitValues> {

    private final SdcApiClient apiClient;
    private final SdcSessionStorage sessionStorage;
    private final Credentials credentials;
    private final SdcConfiguration agentConfiguration;
    private final SdcPersistentStorage persistentStorage;

    public SdcSmsOtpAuthenticator(
            SdcApiClient apiClient,
            SdcSessionStorage sessionStorage,
            SdcConfiguration agentConfiguration,
            Credentials credentials,
            SdcPersistentStorage persistentStorage) {

        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.credentials = credentials;
        this.agentConfiguration = agentConfiguration;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public InitValues init(String username, String password)
            throws AuthenticationException, AuthorizationException {

        try {
            ChallengeResponse challenge = this.apiClient.getChallenge();
            SdcDevice device = new SdcDevice(this.persistentStorage);

            AgreementsResponse agreementsResponse = this.apiClient.pinLogon(username, password);
            if (agreementsResponse.isEmpty()) {
                log.warn(
                        "tag={} User was able to login, but has no agreements?",
                        SdcConstants.Session.LOGIN);
            }

            SessionStorageAgreements agreements = agreementsResponse.toSessionStorageAgreements();

            // store phone number used during device pinning
            SdcPhoneNumbersEntity phoneNumber =
                    findPhoneNumber(agreements)
                            .orElseThrow(() -> new IllegalStateException("Missing phone number"));

            HttpResponse response =
                    this.apiClient.pinDevice(device, phoneNumber.toString(this.agentConfiguration));
            String transId = response.getHeaders().getFirst(SdcConstants.Headers.X_SDC_TRANS_ID);
            this.apiClient.sendOTPRequest(transId);
            this.sessionStorage.setAgreements(agreements);

            return new InitValues(device, challenge, transId);
        } catch (HttpResponseException e) {
            log.info("tag={}", SdcConstants.HTTP_RESPONSE_LOGGER, e);
            handleErrors(e);
            throw e;
        }
    }

    public void handleErrors(HttpResponseException e)
            throws AuthenticationException, AuthorizationException {
        // sdc responds with internal server error when bad credentials

        Optional<InvalidPinResponse> invalidPin = InvalidPinResponse.from(e);
        if (invalidPin.isPresent()) {
            throw invalidPin.get().exception(e);
        }
        if (SdcConstants.Authentication.isInternalError(e)) {
            // errorMessage is null safe
            String errorMessage =
                    Optional.ofNullable(
                                    e.getResponse()
                                            .getHeaders()
                                            .getFirst(SdcConstants.Headers.X_SDC_ERROR_MESSAGE))
                            .orElse("");
            if (this.agentConfiguration.isNotCustomer(errorMessage)) {
                throw LoginError.NOT_CUSTOMER.exception(e);
            }
            if (this.agentConfiguration.isDeviceRegistrationNotAllowed(errorMessage)) {
                throw new IllegalStateException(
                        "This bank does not support device registration! Configure this provider to use PIN instead of SMS",
                        e);
            } else if (this.agentConfiguration.isLoginError(errorMessage)) {
                log.info(errorMessage, e);

                // if user is blocked throw more specific exception
                if (this.agentConfiguration.isUserBlocked(errorMessage)) {
                    throw AuthorizationError.ACCOUNT_BLOCKED.exception(e);
                }

                throw LoginError.INCORRECT_CREDENTIALS.exception(e);
            }
        }
    }

    @Override
    public void authenticate(String otp, InitValues initValues)
            throws AuthenticationException, AuthorizationException {
        credentials.setSensitivePayload(SdcConstants.Storage.OTP, otp);
        try {
            this.apiClient.signOTP(
                    initValues.transId, otp, this.credentials.getField(Field.Key.PASSWORD));
        } catch (HttpResponseException hre) {
            String errorMessage =
                    Optional.ofNullable(
                                    hre.getResponse()
                                            .getHeaders()
                                            .getFirst(SdcConstants.Headers.X_SDC_ERROR_MESSAGE))
                            .orElse("");

            if (SdcConstants.ErrorMessage.isPinInvalid(errorMessage)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception(hre);
            }

            throw hre;
        }
        this.persistentStorage.putSignedDeviceId(initValues.device.getDeviceId());

        DeviceToken deviceToken = new DeviceToken(initValues.challenge, initValues.device);

        // enable app (bankClient) using a device token
        this.apiClient.setDeviceToken(deviceToken);
    }

    private Optional<SdcPhoneNumbersEntity> findPhoneNumber(SessionStorageAgreements agreements) {
        SessionStorageAgreement agreement =
                agreements.stream()
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No agreement found"));

        HttpResponse response =
                this.apiClient.internalSelectAgreement(
                        new SelectAgreementRequest()
                                .setUserNumber(agreement.getUserNumber())
                                .setAgreementNumber(agreement.getAgreementId()));

        return response.getBody(SdcAgreementServiceConfigurationResponse.class)
                .findFirstPhoneNumber();
    }

    public static class InitValues {
        private final SdcDevice device;
        private final ChallengeResponse challenge;
        private final String transId;

        InitValues(SdcDevice device, ChallengeResponse challenge, String transId) {
            this.device = device;
            this.challenge = challenge;
            this.transId = transId;
        }
    }
}
