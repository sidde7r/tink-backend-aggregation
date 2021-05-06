package se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.authenticator;

import com.google.common.base.Strings;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.HandelsbankenSEConstants.BankIdAuthentication;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.HandelsbankenSEConstants.DeviceAuthentication;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.authenticator.bankid.AuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.authenticator.bankid.AuthorizeMandateRequest;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.authenticator.bankid.InitBankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.authenticator.bankid.InitBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities.Mandate;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.ApplicationEntryPointResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.AuthorizeResponse;
import se.tink.backend.aggregation.agents.utils.business.OrganisationNumberSeLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class HandelsbankenBankIdAuthenticator implements BankIdAuthenticator<InitBankIdResponse> {
    private static final Logger LOG =
            LoggerFactory.getLogger(HandelsbankenBankIdAuthenticator.class);

    private final HandelsbankenSEApiClient client;
    private final HandelsbankenPersistentStorage persistentStorage;
    private final HandelsbankenSessionStorage sessionStorage;
    private final String organisationNumber;

    private int pollCount;
    private String autoStartToken;
    private String lastWaitingResult;

    public HandelsbankenBankIdAuthenticator(
            HandelsbankenSEApiClient client,
            HandelsbankenPersistentStorage persistentStorage,
            HandelsbankenSessionStorage sessionStorage,
            String organisationNumber) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
        this.organisationNumber = organisationNumber;
    }

    @Override
    public InitBankIdResponse init(String ssn) throws BankIdException, AuthorizationException {
        pollCount = 0;
        OrganisationNumberSeLogger.logIfUnknownOrgnumber(organisationNumber);

        return refreshAutostartToken();
    }

    @Override
    public BankIdStatus collect(InitBankIdResponse initBankId)
            throws AuthenticationException, AuthorizationException {
        if (!Strings.isNullOrEmpty(initBankId.getCode())
                && BankIdAuthentication.CANCELLED.equalsIgnoreCase(initBankId.getCode())) {
            // if a bankid signature is running at the time we initiate ours the bank/bankid will
            // cancel both of them.
            return BankIdStatus.CANCELLED;
        }

        AuthenticateResponse authenticate = client.authenticate(initBankId);
        BankIdStatus bankIdStatus = authenticate.toBankIdStatus();
        switch (bankIdStatus) {
            case DONE:
                AuthorizeResponse authorizeResponse = finishAuthorization(authenticate);
                ApplicationEntryPointResponse applicationEntryPoint =
                        client.applicationEntryPoint(authorizeResponse);
                persistentStorage.persist(authorizeResponse);
                sessionStorage.persist(applicationEntryPoint);
                persistentStorage.persist(organisationNumber);
                OrganisationNumberSeLogger.logIfUnknownOrgnumberForSuccessfulLogin(
                        organisationNumber);

                break;
            case WAITING:
                lastWaitingResult = authenticate.getResult();
                pollCount++;
                break;
            case TIMEOUT:
                if (pollCount < 10) {
                    return BankIdStatus.FAILED_UNKNOWN;
                }
                if (BankIdAuthentication.NO_CLIENT.equals(lastWaitingResult)) {
                    lastWaitingResult = null;
                    return BankIdStatus.EXPIRED_AUTOSTART_TOKEN;
                }
                break;
            default:
                LOG.info("Unknown bankID status: {}", bankIdStatus);
                return BankIdStatus.FAILED_UNKNOWN;
        }
        return bankIdStatus;
    }

    private AuthorizeResponse finishAuthorization(AuthenticateResponse authenticate) {
        AuthorizeResponse authorizeResponse = client.authorize(authenticate);

        if (authorizeResponse.getMandates().isEmpty()) {
            throw LoginError.NOT_CUSTOMER.exception();
        }

        // Selection and authorization of mandate is required when user has multiple mandates
        if (HandelsbankenSEConstants.DeviceAuthentication.SELECTION_REQUIRED.equalsIgnoreCase(
                authorizeResponse.getResult())) {
            return authorizeMandate(authorizeResponse);
        }

        validateOrganizationNumber(authorizeResponse);
        return authorizeResponse;
    }

    private AuthorizeResponse authorizeMandate(AuthorizeResponse authorizeResponse) {
        Mandate mandateToAuthorize =
                authorizeResponse.getMandates().stream()
                        .filter(
                                mandate ->
                                        organisationNumber.equalsIgnoreCase(
                                                mandate.getCustomerNumber()))
                        .findAny()
                        .orElseThrow(LoginError.INCORRECT_CREDENTIALS::exception);

        AuthorizeMandateRequest authorizeMandateRequest =
                new AuthorizeMandateRequest()
                        .setAgreementNumbers(mandateToAuthorize.getAgreementNumber());

        return client.authorizeMandate(authorizeResponse, authorizeMandateRequest);
    }

    private void validateOrganizationNumber(AuthorizeResponse authorizeResponse)
            throws AuthorizationException {
        final Mandate mandate = authorizeResponse.getMandates().get(0);
        if (!organisationNumber.equalsIgnoreCase(mandate.getCustomerNumber())) {
            LOG.error("Organization number mismatch");
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }

    @Override
    public InitBankIdResponse refreshAutostartToken() throws BankServiceException {
        InitBankIdRequest initBankIdRequest =
                new InitBankIdRequest().setBidDevice(DeviceAuthentication.DEVICE_ID);
        InitBankIdResponse response = client.initToBank(initBankIdRequest);
        autoStartToken = response.getAutoStartToken();
        return response;
    }

    @Override
    public Optional<String> getAutostartToken() {
        return Optional.ofNullable(autoStartToken);
    }

    @Override
    public Optional<OAuth2Token> getAccessToken() {
        return Optional.empty();
    }

    @Override
    public Optional<OAuth2Token> refreshAccessToken(String refreshToken) {
        return Optional.empty();
    }
}
