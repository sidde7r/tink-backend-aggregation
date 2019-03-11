package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BBVAApiClient;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BBVAConstants;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BBVAUtils;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc.DeviceActivationRequest;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc.DigitalActivationRequest;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc.GrantingTicketRequest;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc.GrantingTicketResponse;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc.LoginErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc.RegisterTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc.TokenActivationRequest;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc.TokenActivationResponse;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc.TokenAuthCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc.ValidateSubscriptionRequest;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc.ValidateSubscriptionResponse;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.transactional.rpc.CustomerInfoResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BBVAAuthenticator implements MultiFactorAuthenticator, AutoAuthenticator {

    private final BBVAApiClient client;
    private final PersistentStorage storage;
    private static final Logger logger = LoggerFactory.getLogger(BBVAAuthenticator.class);

    public BBVAAuthenticator(BBVAApiClient client, PersistentStorage storage) {
        this.client = client;
        this.storage = storage;
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {

        String phonenumber = credentials.getField(BBVAConstants.FIELDS.USERNAME);
        String password = credentials.getField(BBVAConstants.FIELDS.PASSWORD);
        String cardnumber = credentials.getField(BBVAConstants.FIELDS.CARD_NUMBER);
        String deviceIdentifier = BBVAUtils.generateDeviceId();
        storage.put(BBVAConstants.STORAGE.DEVICE_IDENTIFIER, deviceIdentifier);

        handleLogin(phonenumber, password);

        handleDevice(phonenumber, cardnumber, deviceIdentifier);

        handleLogin(phonenumber, password);

        fetchClientInfo();

        client.getContactToken(phonenumber);

        handleTokenAuthentication(deviceIdentifier, phonenumber);

        storage.put(BBVAConstants.STORAGE.CARD_NUMBER, cardnumber);
        storage.put(BBVAConstants.STORAGE.PASSWORD, password);
        storage.put(BBVAConstants.STORAGE.PHONE_NUMBER, phonenumber);
    }

    private void fetchClientInfo() {
        CustomerInfoResponse customerInfo = client.getCustomerInfo();
        storage.put(BBVAConstants.STORAGE.HOLDERNAME, customerInfo.getCustomerName());
    }

    private GrantingTicketResponse handleLogin(String phoneNumber, String password)
            throws LoginException {
        try {
            return client.grantTicket(new GrantingTicketRequest(phoneNumber, password));
        } catch (HttpResponseException e) {
            LoginErrorResponse err = e.getResponse().getBody(LoginErrorResponse.class);

            if (BBVAConstants.ERROR.NO_ACCOUNT_FOUND_CODE.equalsIgnoreCase(err.getErrorCode())
                    || BBVAConstants.ERROR.INCORRECT_CREDENTIALS_CODE.equalsIgnoreCase(
                            err.getErrorCode())) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }

            logger.error(
                    "{} LoginError: {}", BBVAConstants.LOGGING.UNKNOWN_LOGIN_ERROR, e.toString());
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }

    private void handleDevice(String phonenumber, String cardnumber, String deviceIdentifier) {
        ValidateSubscriptionResponse validationRespone =
                client.validateSubscription(
                        new ValidateSubscriptionRequest(phonenumber, cardnumber));
        String customerId = validationRespone.getData().getCustomerId();

        String boundary = BBVAUtils.generateBoundary();

        DeviceActivationRequest deviceActivationRequest =
                new DeviceActivationRequest(boundary, phonenumber, deviceIdentifier, cardnumber);
        client.activateDevice(deviceActivationRequest, boundary);

        DigitalActivationRequest digitalActivationRequest =
                new DigitalActivationRequest(phonenumber, cardnumber, deviceIdentifier);
        client.digitalActivation(customerId, digitalActivationRequest);
    }

    private void handleTokenAuthentication(String deviceIdentifier, String phoneNumber) {
        TokenAuthCodeResponse codeResponse = client.getTokenAuthCode(deviceIdentifier);

        String tokenActivationId = codeResponse.getData().getId();
        String tokenAuthCode = codeResponse.getData().getSoftwareTokenAuthCode();
        String tokenAuthCodeHash = BBVAUtils.generateTokenHash(tokenAuthCode);
        String salt = BBVAUtils.generateSalt();

        String authenticationCode =
                BBVAUtils.generateAuthenticationCode(
                        tokenAuthCode,
                        tokenAuthCodeHash,
                        BBVAConstants.APPLICATION_CODE,
                        BBVAConstants.APPLICATION_CODE_VERSION,
                        salt,
                        BBVAConstants.ENCRYPTION.PUBLIC_KEY_HEX_DER);

        TokenActivationRequest tokenActivationRequest =
                new TokenActivationRequest(deviceIdentifier, salt, authenticationCode);

        TokenActivationResponse tokenActivationRespone =
                client.getTokenWithHash(
                        tokenActivationId, tokenAuthCodeHash, tokenActivationRequest);

        RegisterTokenRequest registerTokenRequest =
                new RegisterTokenRequest(
                        phoneNumber, deviceIdentifier, codeResponse.getData().getId());

        client.registerToken(registerTokenRequest);

        client.updateDevice(deviceIdentifier);
    }

    @Override
    public void autoAuthenticate()
            throws SessionException, BankServiceException, AuthorizationException {

        // Sessions seem to be active for a long time. Not sure if we need autoAuthenticate
        // Adding logging for now
        logger.info("{}", BBVAConstants.LOGGING.AUTO_AUTH);

        String phonenumber = storage.get(BBVAConstants.STORAGE.PHONE_NUMBER);
        String password = storage.get(BBVAConstants.STORAGE.PASSWORD);
        String deviceIdentifier = storage.get(BBVAConstants.STORAGE.DEVICE_IDENTIFIER);
        String phoneNumber = storage.get(BBVAConstants.STORAGE.PHONE_NUMBER);

        try {
            client.grantTicket(new GrantingTicketRequest(phonenumber, password));
        } catch (HttpResponseException e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        fetchClientInfo();

        TokenAuthCodeResponse codeResponse = client.getTokenAuthCode(deviceIdentifier);
        RegisterTokenRequest registerTokenRequest =
                new RegisterTokenRequest(
                        phoneNumber, deviceIdentifier, codeResponse.getData().getId());

        client.registerToken(registerTokenRequest);
        client.updateDevice(deviceIdentifier);
    }
}
