package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator;

import java.util.Optional;
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
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.smsotp.SmsOtpAuthenticatorPassword;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.agents.rpc.Credentials;

public class SdcSmsOtpAuthenticator implements SmsOtpAuthenticatorPassword<SdcSmsOtpAuthenticator.InitValues> {
    private static final AggregationLogger LOGGER = new AggregationLogger(SdcSmsOtpAuthenticator.class);

    private final SdcApiClient apiClient;
    private final SdcSessionStorage sessionStorage;
    private final Credentials credentials;
    private final SdcConfiguration agentConfiguration;
    private final SdcPersistentStorage persistentStorage;

    public SdcSmsOtpAuthenticator(SdcApiClient apiClient, SdcSessionStorage sessionStorage,
            SdcConfiguration agentConfiguration, Credentials credentials, SdcPersistentStorage persistentStorage) {

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
                LOGGER.warnExtraLong("User was able to login, but has no agreements?", SdcConstants.Session.LOGIN);
            }

            SessionStorageAgreements agreements = agreementsResponse.toSessionStorageAgreements();

            // store phone number used during device pinning
            SdcPhoneNumbersEntity phoneNumber = findPhoneNumber(agreements)
                    .orElseThrow(() -> new IllegalStateException("Missing phone number"));


            HttpResponse response = this.apiClient.pinDevice(device, phoneNumber.toString(this.agentConfiguration));
            String transId = response.getHeaders().getFirst(SdcConstants.Headers.X_SDC_TRANS_ID);
            this.apiClient.sendOTPRequest(transId);
            this.sessionStorage.setAgreements(agreements);

            return new InitValues(device, challenge, transId);
        } catch (HttpResponseException e) {
            // sdc responds with internal server error when bad credentials

            Optional<InvalidPinResponse> invalidPin  = InvalidPinResponse.from(e);
            if (invalidPin.isPresent()) {
                throw invalidPin.get().exception();
            }
            if (SdcConstants.Authentication.isInternalError(e)) {
                // errorMessage is null safe
                String errorMessage = Optional
                        .ofNullable(e.getResponse().getHeaders().getFirst(SdcConstants.Headers.X_SDC_ERROR_MESSAGE))
                        .orElse("");
                if (this.agentConfiguration.isNotCustomer(errorMessage)) {
                    throw LoginError.NOT_CUSTOMER.exception();
                } else if (this.agentConfiguration.isLoginError(errorMessage)) {
                    LOGGER.info(errorMessage);

                    // if user is blocked throw more specific exception
                    if (this.agentConfiguration.isUserBlocked(errorMessage)) {
                        throw AuthorizationError.ACCOUNT_BLOCKED.exception();
                    }

                    throw LoginError.INCORRECT_CREDENTIALS.exception();
                }
            }

            throw e;
        }
    }

    @Override
    public void authenticate(String otp, InitValues initValues) throws AuthenticationException, AuthorizationException {

        this.apiClient.signOTP(initValues.transId, otp, this.credentials.getField(Field.Key.PASSWORD));
        this.persistentStorage.putSignedDeviceId(initValues.device.getDeviceId());

        DeviceToken deviceToken = new DeviceToken(initValues.challenge, initValues.device);

        // enable app (bankClient) using a device token
        this.apiClient.setDeviceToken(deviceToken);
    }

    private Optional<SdcPhoneNumbersEntity> findPhoneNumber(SessionStorageAgreements agreements) {
        SessionStorageAgreement agreement = agreements.stream().findFirst().orElseThrow(() -> new IllegalStateException("No agreement found"));

        HttpResponse response = this.apiClient.internalSelectAgreement(new SelectAgreementRequest()
                .setUserNumber(agreement.getUserNumber())
                .setAgreementNumber(agreement.getAgreementId())
        );

        return response.getBody(SdcAgreementServiceConfigurationResponse.class)
                .findFirstPhoneNumber();
    }

    class InitValues {
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
