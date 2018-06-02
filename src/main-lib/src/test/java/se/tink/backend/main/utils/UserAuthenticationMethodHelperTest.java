package se.tink.backend.main.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import se.tink.backend.common.config.AuthenticationConfiguration;
import se.tink.backend.core.Market;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.libraries.auth.AuthenticationMethod;
import static org.assertj.core.api.Assertions.assertThat;

public class UserAuthenticationMethodHelperTest {

    @Test
    public void testDefaultMethods() {
        UserAuthenticationMethodHelper helper = createHelper(Market.Code.NL,
                ImmutableList.of(AuthenticationMethod.ABN_AMRO_PIN5));

        // User is on Swedish market and the config is for Netherlands.
        User user = createSwedishUser();
        assertThat(helper.getAvailableLoginMethods(user)).containsOnly(AuthenticationMethod.EMAIL_AND_PASSWORD);
        assertThat(helper.getAuthorizedLoginMethods(user)).isEmpty();
    }

    @Test
    public void testAvailableMethods() {
        UserAuthenticationMethodHelper helper = createHelper(Market.Code.SE,
                ImmutableList.of(AuthenticationMethod.BANKID, AuthenticationMethod.EMAIL_AND_PASSWORD));

        User user = createSwedishUser();

        assertThat(helper.getAvailableLoginMethods(user)).contains(
                AuthenticationMethod.BANKID, AuthenticationMethod.EMAIL_AND_PASSWORD);

        assertThat(helper.getAuthorizedLoginMethods(user)).isEmpty();
    }

    @Test
    public void testUserWithNationalId() {
        UserAuthenticationMethodHelper helper = createHelper(Market.Code.SE,
                ImmutableList.of(AuthenticationMethod.BANKID, AuthenticationMethod.EMAIL_AND_PASSWORD));

        User user = createSwedishUser();
        user.setNationalId("198401141111");

        assertThat(helper.getAuthorizedLoginMethods(user)).containsOnly(AuthenticationMethod.BANKID);
    }

    @Test
    public void testUserWithInvalidEmail() {
        UserAuthenticationMethodHelper helper = createHelper(Market.Code.SE,
                ImmutableList.of(AuthenticationMethod.BANKID, AuthenticationMethod.EMAIL_AND_PASSWORD));

        User user = createSwedishUser();
        user.setUsername("Kalle Anka"); // Not valid email

        assertThat(helper.getAuthorizedLoginMethods(user)).isEmpty();
    }

    @Test
    public void testUserWithoutPassword() {
        UserAuthenticationMethodHelper helper = createHelper(Market.Code.SE,
                ImmutableList.of(AuthenticationMethod.BANKID, AuthenticationMethod.EMAIL_AND_PASSWORD));

        User user = createSwedishUser();
        user.setUsername("erik@tink.se"); // Valid email but missing password
        user.setPassword(null);

        assertThat(helper.getAuthorizedLoginMethods(user)).isEmpty();
    }

    @Test
    public void testUserWithoutEmailAndPassword() {
        UserAuthenticationMethodHelper helper = createHelper(Market.Code.SE,
                ImmutableList.of(AuthenticationMethod.BANKID, AuthenticationMethod.EMAIL_AND_PASSWORD));

        User user = createSwedishUser();
        user.setUsername("erik@tink.se");
        user.setHash("password");

        assertThat(helper.getAuthorizedLoginMethods(user)).containsOnly(AuthenticationMethod.EMAIL_AND_PASSWORD);
    }

    @Test
    public void testUserWithEmailAndPasswordAndNationalId() {
        UserAuthenticationMethodHelper helper = createHelper(Market.Code.SE,
                ImmutableList.of(AuthenticationMethod.BANKID, AuthenticationMethod.EMAIL_AND_PASSWORD));

        User user = createSwedishUser();
        user.setUsername("erik@tink.se");
        user.setHash("password");
        user.setNationalId("198401141935");

        assertThat(helper.getAuthorizedLoginMethods(user))
                .containsOnly(AuthenticationMethod.EMAIL_AND_PASSWORD, AuthenticationMethod.BANKID);
    }

    private User createSwedishUser() {
        User user = new User();

        UserProfile profile = new UserProfile();
        profile.setMarket("SE");

        user.setProfile(profile);

        return user;
    }

    private UserAuthenticationMethodHelper createHelper(Market.Code code, ImmutableList<AuthenticationMethod> methods) {
        AuthenticationConfiguration configuration = new AuthenticationConfiguration();

        Map<Market.Code, List<AuthenticationMethod>> loginMethods = Maps.newHashMap();

        loginMethods.put(code, methods);

        configuration.setMarketLoginMethods(loginMethods);

        return new UserAuthenticationMethodHelper(configuration);
    }
}
