package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BbvaMxApiClient;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BbvaMxConstants;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BbvaMxUtils;
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
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.transactional.rpc.IdentityDataResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BbvaMxAuthenticator implements Authenticator {

    private final BbvaMxApiClient client;
    private final PersistentStorage storage;
    private static final Logger logger = LoggerFactory.getLogger(BbvaMxAuthenticator.class);

    public BbvaMxAuthenticator(BbvaMxApiClient client, PersistentStorage storage) {
        this.client = client;
        this.storage = storage;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {

        String phonenumber = credentials.getField(BbvaMxConstants.FIELDS.USERNAME);
        String password = credentials.getField(BbvaMxConstants.FIELDS.PASSWORD);
        String cardnumber = credentials.getField(BbvaMxConstants.FIELDS.CARD_NUMBER);
        String deviceIdentifier = BbvaMxUtils.generateDeviceId();
        storage.put(BbvaMxConstants.STORAGE.DEVICE_IDENTIFIER, deviceIdentifier);

        handleLogin(phonenumber, password);

        handleDevice(phonenumber, cardnumber, deviceIdentifier);

        handleLogin(phonenumber, password);

        fetchClientInfo();

        client.getContactToken(phonenumber);

        handleTokenAuthentication(deviceIdentifier, phonenumber);

        storage.put(BbvaMxConstants.STORAGE.CARD_NUMBER, cardnumber);
        storage.put(BbvaMxConstants.STORAGE.PASSWORD, password);
        storage.put(BbvaMxConstants.STORAGE.PHONE_NUMBER, phonenumber);
    }

    private void fetchClientInfo() {
        IdentityDataResponse customerInfo = client.getIdentityData();
        storage.put(BbvaMxConstants.STORAGE.HOLDERNAME, customerInfo.getCustomerName());
    }

    private GrantingTicketResponse handleLogin(String phoneNumber, String password)
            throws LoginException, AuthorizationException {
        try {
            return client.grantTicket(new GrantingTicketRequest(phoneNumber, password));
        } catch (HttpResponseException e) {
            LoginErrorResponse err = e.getResponse().getBody(LoginErrorResponse.class);

            if (BbvaMxConstants.ERROR.NO_ACCOUNT_FOUND_CODE.equalsIgnoreCase(err.getErrorCode())
                    || BbvaMxConstants.ERROR.INCORRECT_CREDENTIALS_CODE.equalsIgnoreCase(
                            err.getErrorCode())) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }

            if (BbvaMxConstants.ERROR.USER_BLOCKED.equalsIgnoreCase(err.getErrorCode())) {
                throw AuthorizationError.ACCOUNT_BLOCKED.exception();
            }

            logger.error(
                    "{} LoginError: {}", BbvaMxConstants.LOGGING.UNKNOWN_LOGIN_ERROR, e.toString());
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }

    private void handleDevice(String phonenumber, String cardnumber, String deviceIdentifier) {
        ValidateSubscriptionResponse validationRespone =
                client.validateSubscription(
                        new ValidateSubscriptionRequest(phonenumber, cardnumber));
        String customerId = validationRespone.getData().getCustomerId();

        String boundary = BbvaMxUtils.generateBoundary();

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
        String tokenAuthCodeHash = BbvaMxUtils.generateTokenHash(tokenAuthCode);
        String salt = BbvaMxUtils.generateSalt();

        String authenticationCode =
                BbvaMxUtils.generateAuthenticationCode(
                        tokenAuthCode,
                        tokenAuthCodeHash,
                        BbvaMxConstants.APPLICATION_CODE,
                        BbvaMxConstants.APPLICATION_CODE_VERSION,
                        salt,
                        BbvaMxConstants.ENCRYPTION.PUBLIC_KEY_HEX_DER);

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
}
