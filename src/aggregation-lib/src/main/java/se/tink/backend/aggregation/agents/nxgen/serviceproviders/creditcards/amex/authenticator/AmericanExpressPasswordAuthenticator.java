package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.authenticator;

import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.AmericanExpressApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.AmericanExpressConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.AmericanExpressConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.authenticator.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.authenticator.entities.LogonDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.authenticator.entities.LogonRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.authenticator.entities.LogonResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class AmericanExpressPasswordAuthenticator implements PasswordAuthenticator {

    private final AmericanExpressConfiguration config;
    private final AmericanExpressApiClient apiClient;
    private final SessionStorage sessionStorage;
    private static final AggregationLogger LOGGER = new AggregationLogger(AmericanExpressPasswordAuthenticator.class);

    public AmericanExpressPasswordAuthenticator(
            AmericanExpressApiClient apiClient,
            AmericanExpressConfiguration config,
            SessionStorage sessionStorage) {
        this.config = config;
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        LogonRequest request = createLogonRequest(username, password);
        LogonResponse response = apiClient.logon(request);

        LogonDataEntity logonData = response.getLogonData();

        if (logonData.loginSucceed()) {
            List<CardEntity> cardList = response.getSummaryData().getCardList();
            sessionStorage.put(
                    AmericanExpressConstants.Tags.SESSION_ID, logonData.getAmexSession());
            sessionStorage.put(AmericanExpressConstants.Tags.CUPCAKE, logonData.getCupcake());
            sessionStorage.put(AmericanExpressConstants.Tags.CARD_LIST, cardList);
        } else if (logonData.loginFailed()) {
            handleAuthenticationFail(logonData);
        } else {
            Integer status = logonData.getStatus();
            LOGGER.warn(String.format("unknown authentication status: (%d) with message: %s",
                    status, logonData.toString()));
        }
    }

    private void handleAuthenticationFail(LogonDataEntity logonData)
            throws AuthorizationException, LoginException {
        if (logonData.isRevoked()) {
            throw AuthorizationError.ACCOUNT_BLOCKED.exception();
        }

        if (logonData.isIncorrect()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();

        } else {
            throw new IllegalStateException(
                    String.format(
                            "#login-refactoring - AMEX " + config.getFace() + " - Login failed with message : "
                                    + "(%s) %s", logonData.getStatusCode(), logonData.getMessage()
                    )
            );
        }

    }

    private LogonRequest createLogonRequest(String username, String password) {
        LogonRequest request = new LogonRequest();
        request.setHardwareId(sessionStorage.get(AmericanExpressConstants.Tags.HARDWARE_ID));
        request.setLocale(config.getLocale());
        request.setDeviceModel(AmericanExpressConstants.HeaderValues.DEVICE_MODEL);
        request.setUser(username);
        request.setPassword(password);
        request.setRememberMe(AmericanExpressConstants.RequestValue.TRUE);
        request.setTimeZoneOffsetInMilli(AmericanExpressConstants.RequestValue.TIME_ZONE_OFFSET);
        request.setUserTimeStampInMilli(Long.toString(System.currentTimeMillis()));
        return request;
    }
}
