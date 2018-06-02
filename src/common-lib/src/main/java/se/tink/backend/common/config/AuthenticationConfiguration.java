package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import se.tink.backend.core.Market;
import se.tink.libraries.auth.AuthenticationMethod;
import se.tink.libraries.auth.HttpAuthenticationMethod;
import se.tink.libraries.auth.encryption.HashingAlgorithm;

public class AuthenticationConfiguration {
    private final static ImmutableList<AuthenticationMethod> DEFAULT_LOGIN_METHODS = ImmutableList
            .of(AuthenticationMethod.EMAIL_AND_PASSWORD);

    private final static ImmutableList<AuthenticationMethod> DEFAULT_REGISTRATION_METHODS = ImmutableList
            .of(AuthenticationMethod.EMAIL_AND_PASSWORD);

    @JsonProperty
    private boolean authorizedDeviceRequired = true;

    @JsonProperty
    private List<HttpAuthenticationMethod> methods = Lists.newArrayList();

    @JsonProperty
    private HashingAlgorithm userPasswordHashAlgorithm = HashingAlgorithm.SCRYPT;

    @JsonProperty
    private int authenticationErrorThreshold = 5;

    @JsonProperty
    private UserSessionConfiguration sessions = new UserSessionConfiguration();

    @JsonProperty
    private Map<Market.Code, List<AuthenticationMethod>> marketRegisterMethods = Maps.newHashMap();

    @JsonProperty
    private Map<Market.Code, List<AuthenticationMethod>> marketLoginMethods = Maps.newHashMap();

    @JsonProperty
    private Map<Market.Code, List<AuthenticationMethod>> gdprMarketLoginMethods = Maps.newHashMap();

    public boolean isAuthorizedDeviceRequired() {
        return authorizedDeviceRequired;
    }

    public List<HttpAuthenticationMethod> getMethods() {
        return methods;
    }

    public void setUserPasswordHashAlgorithm(HashingAlgorithm userPasswordHashAlgorithm) {
        this.userPasswordHashAlgorithm = userPasswordHashAlgorithm;
    }

    public HashingAlgorithm getUserPasswordHashAlgorithm() {
        return userPasswordHashAlgorithm;
    }

    public List<AuthenticationMethod> getMarketRegisterMethods(Market.Code marketCode) {
        List<AuthenticationMethod> methods = marketRegisterMethods.get(marketCode);
        if (methods != null) {
            return methods;
        } else {
            return DEFAULT_REGISTRATION_METHODS;
        }
    }

    public List<AuthenticationMethod> getMarketLoginMethods(Market.Code marketCode) {
        List<AuthenticationMethod> methods = marketLoginMethods.get(marketCode);
        if (methods != null) {
            return methods;
        } else {
            return DEFAULT_LOGIN_METHODS;
        }
    }

    public List<AuthenticationMethod> getGdprMarketLoginMethods(Market.Code marketCode) {
        List<AuthenticationMethod> methods = gdprMarketLoginMethods.get(marketCode);
        if (methods != null) {
            return methods;
        } else {
            return DEFAULT_LOGIN_METHODS;
        }
    }

    public int getAuthenticationErrorThreshold() {
        return authenticationErrorThreshold;
    }

    public void setMarketRegisterMethods(Map<Market.Code, List<AuthenticationMethod>> marketRegisterMethods) {
        this.marketRegisterMethods = marketRegisterMethods;
    }

    public void setMarketLoginMethods(Map<Market.Code, List<AuthenticationMethod>> marketLoginMethods) {
        this.marketLoginMethods = marketLoginMethods;
    }

    public UserSessionConfiguration getSessions() {
        return sessions;
    }
}
