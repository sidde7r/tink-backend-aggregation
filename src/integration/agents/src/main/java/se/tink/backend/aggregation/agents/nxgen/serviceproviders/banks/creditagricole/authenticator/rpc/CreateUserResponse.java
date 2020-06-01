package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.rpc.DefaultResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreateUserResponse extends DefaultResponse {

    private String serverRefDate;
    private List<AuthenticationMethods> availableAuthenticationMethods;
    private PhoneNumber availablePhoneNumber;

    public String getServerRefDate() {
        return serverRefDate;
    }

    public void setServerRefDate(String serverRefDate) {
        this.serverRefDate = serverRefDate;
    }

    public List<AuthenticationMethods> getAvailableAuthenticationMethods() {
        return availableAuthenticationMethods;
    }

    public void setAvailableAuthenticationMethods(
            List<AuthenticationMethods> availableAuthenticationMethods) {
        this.availableAuthenticationMethods = availableAuthenticationMethods;
    }

    public PhoneNumber getAvailablePhoneNumber() {
        return availablePhoneNumber;
    }

    public void setAvailablePhoneNumber(PhoneNumber availablePhoneNumber) {
        this.availablePhoneNumber = availablePhoneNumber;
    }

    public static class AuthenticationMethods {

        private String name;
        private String authenticationLevel;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAuthenticationLevel() {
            return authenticationLevel;
        }

        public void setAuthenticationLevel(String authenticationLevel) {
            this.authenticationLevel = authenticationLevel;
        }
    }

    public static class PhoneNumber {

        private String reliablePhoneNumber;
        private String countryCallingCode;

        public String getReliablePhoneNumber() {
            return reliablePhoneNumber;
        }

        public void setReliablePhoneNumber(String reliablePhoneNumber) {
            this.reliablePhoneNumber = reliablePhoneNumber;
        }

        public String getCountryCallingCode() {
            return countryCallingCode;
        }

        public void setCountryCallingCode(String countryCallingCode) {
            this.countryCallingCode = countryCallingCode;
        }
    }
}
