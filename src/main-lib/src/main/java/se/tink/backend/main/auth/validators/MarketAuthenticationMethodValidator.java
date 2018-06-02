package se.tink.backend.main.auth.validators;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import se.tink.backend.common.config.AuthenticationConfiguration;
import se.tink.backend.core.Market;
import se.tink.backend.main.auth.exceptions.IllegalAuthenticationMethodException;
import se.tink.libraries.auth.AuthenticationMethod;

public class MarketAuthenticationMethodValidator {
    private final AuthenticationConfiguration authenticationConfiguration;

    @Inject
    public MarketAuthenticationMethodValidator(AuthenticationConfiguration authenticationConfiguration) {
        this.authenticationConfiguration = authenticationConfiguration;
    }

    /**
     * The authentication method needs to be allowed for either login or registration for the specified market.
     */
    public void validateForAuthentication(Market market, AuthenticationMethod method) {
        Preconditions.checkNotNull(market, "Market must not be null.");

        if (!authenticationConfiguration.getMarketRegisterMethods(market.getCode()).contains(method) &&
                !authenticationConfiguration.getMarketLoginMethods(market.getCode()).contains(method)) {
            throw new IllegalAuthenticationMethodException("Authentication", method, market.getCode());
        }
    }

    public void validateForLogin(Market market, AuthenticationMethod method) {
        Preconditions.checkNotNull(market, "Market must not be null.");

        if (!authenticationConfiguration.getMarketLoginMethods(market.getCode()).contains(method)) {
            throw new IllegalAuthenticationMethodException("Login", method, market.getCode());
        }
    }

    public void validateForRegistration(Market market, AuthenticationMethod method) {
        Preconditions.checkNotNull(market, "Market must not be null.");

        if (!authenticationConfiguration.getMarketRegisterMethods(market.getCode()).contains(method)) {
            throw new IllegalAuthenticationMethodException("Registration", method, market.getCode());
        }
    }
}
