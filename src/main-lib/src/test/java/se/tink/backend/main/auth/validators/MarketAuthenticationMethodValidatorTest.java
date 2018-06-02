package se.tink.backend.main.auth.validators;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import se.tink.backend.common.config.AuthenticationConfiguration;
import se.tink.backend.core.Market;
import se.tink.backend.main.auth.exceptions.IllegalAuthenticationMethodException;
import se.tink.libraries.auth.AuthenticationMethod;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MarketAuthenticationMethodValidatorTest {
    @Test
    public void validateAllowedMethodForLogin() {
        Market market = new Market();
        market.setCode("NL");

        Map<Market.Code, List<AuthenticationMethod>> loginMethods = Maps.newHashMap();

        loginMethods.put(Market.Code.NL, ImmutableList.of(AuthenticationMethod.BANKID));

        AuthenticationConfiguration configuration = new AuthenticationConfiguration();
        configuration.setMarketLoginMethods(loginMethods);

        MarketAuthenticationMethodValidator validator = new MarketAuthenticationMethodValidator(configuration);

        validator.validateForLogin(market, AuthenticationMethod.BANKID);
    }

    @Test
    public void validateNotAllowedMethodForLogin() {
        Market market = new Market();
        market.setCode("NL");

        AuthenticationConfiguration configuration = new AuthenticationConfiguration();

        MarketAuthenticationMethodValidator validator = new MarketAuthenticationMethodValidator(configuration);

        assertThatThrownBy(() -> {
            validator.validateForLogin(market, AuthenticationMethod.BANKID);
        }).isInstanceOf(IllegalAuthenticationMethodException.class);
    }

    @Test
    public void validateAllowedMethodForRegister() {
        Market market = new Market();
        market.setCode("NL");

        Map<Market.Code, List<AuthenticationMethod>> registerMethods = Maps.newHashMap();

        registerMethods.put(Market.Code.NL, ImmutableList.of(AuthenticationMethod.BANKID));

        AuthenticationConfiguration configuration = new AuthenticationConfiguration();
        configuration.setMarketRegisterMethods(registerMethods);

        MarketAuthenticationMethodValidator validator = new MarketAuthenticationMethodValidator(configuration);

        validator.validateForRegistration(market, AuthenticationMethod.BANKID);
    }

    @Test
    public void validateNotAllowedMethodForRegister() {
        Market market = new Market();
        market.setCode("NL");

        Map<Market.Code, List<AuthenticationMethod>> registerMethods = Maps.newHashMap();

        registerMethods.put(Market.Code.NL, ImmutableList.of(AuthenticationMethod.BANKID));

        AuthenticationConfiguration configuration = new AuthenticationConfiguration();
        configuration.setMarketRegisterMethods(registerMethods);

        MarketAuthenticationMethodValidator validator = new MarketAuthenticationMethodValidator(configuration);

        assertThatThrownBy(() -> {
            validator.validateForRegistration(market, AuthenticationMethod.EMAIL_AND_PASSWORD);
        }).isInstanceOf(IllegalAuthenticationMethodException.class);
    }

    @Test
    public void validateLoginAsAllowedMethodsForAuthentication() {
        Market market = new Market();
        market.setCode("SE");

        Map<Market.Code, List<AuthenticationMethod>> methods = Maps.newHashMap();

        methods.put(Market.Code.SE, ImmutableList.of(AuthenticationMethod.BANKID));

        AuthenticationConfiguration configuration = new AuthenticationConfiguration();
        configuration.setMarketRegisterMethods(Maps.newHashMap()); // No methods allowed
        configuration.setMarketLoginMethods(methods);

        MarketAuthenticationMethodValidator validator = new MarketAuthenticationMethodValidator(configuration);

        validator.validateForAuthentication(market, AuthenticationMethod.BANKID);
    }

    @Test
    public void validateRegisterAsAllowedMethodsForAuthentication() {
        Market market = new Market();
        market.setCode("SE");

        Map<Market.Code, List<AuthenticationMethod>> methods = Maps.newHashMap();

        methods.put(Market.Code.SE, ImmutableList.of(AuthenticationMethod.BANKID));

        AuthenticationConfiguration configuration = new AuthenticationConfiguration();
        configuration.setMarketRegisterMethods(methods);
        configuration.setMarketLoginMethods(Maps.newHashMap()); // No methods allowed

        MarketAuthenticationMethodValidator validator = new MarketAuthenticationMethodValidator(configuration);

        validator.validateForAuthentication(market, AuthenticationMethod.BANKID);
    }

    @Test
    public void validateNotAllowedMethodsForAuthentication() {
        Market market = new Market();
        market.setCode("SE");

        AuthenticationConfiguration configuration = new AuthenticationConfiguration();
        configuration.setMarketRegisterMethods(Maps.newHashMap()); // No methods allowed
        configuration.setMarketLoginMethods(Maps.newHashMap()); // No methods allowed

        MarketAuthenticationMethodValidator validator = new MarketAuthenticationMethodValidator(configuration);

        assertThatThrownBy(() -> {
            validator.validateForAuthentication(market, AuthenticationMethod.BANKID);
        }).isInstanceOf(IllegalAuthenticationMethodException.class);
    }

    @Test
    public void validateNullInput() {
        AuthenticationConfiguration configuration = new AuthenticationConfiguration();
        configuration.setMarketRegisterMethods(Maps.newHashMap()); // No methods allowed
        configuration.setMarketLoginMethods(Maps.newHashMap()); // No methods allowed

        MarketAuthenticationMethodValidator validator = new MarketAuthenticationMethodValidator(configuration);

        assertThatThrownBy(() -> {
            validator.validateForAuthentication(null, AuthenticationMethod.BANKID);
        }).isInstanceOf(NullPointerException.class).hasMessage("Market must not be null.");
    }
}
