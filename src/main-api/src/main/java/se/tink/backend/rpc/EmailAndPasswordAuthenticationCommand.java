package se.tink.backend.rpc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.libraries.validation.exceptions.InvalidEmailException;
import se.tink.libraries.validation.validators.EmailValidator;

public class EmailAndPasswordAuthenticationCommand {
    private String email;
    private String password;
    private String market;
    private String clientId;
    private String oauth2ClientId;
    private String deviceId;
    private String userAgent;

    public EmailAndPasswordAuthenticationCommand(String email, String password, String market, String clientId,
            String oauth2ClientId, String deviceId, String userAgent) throws InvalidEmailException {

        final String lowerCaseEmail = !Strings.isNullOrEmpty(email) ? email.toLowerCase() : email;

        validate(lowerCaseEmail, password);

        this.email = lowerCaseEmail;
        this.password = password;
        this.market = market;
        this.clientId = clientId;
        this.oauth2ClientId = oauth2ClientId;
        this.deviceId = deviceId;
        this.userAgent = userAgent;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getMarket() {
        return market;
    }

    public String getClientId() {
        return clientId;
    }

    public String getOauth2ClientId() {
        return oauth2ClientId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getUserAgent() {
        return userAgent;
    }

    private void validate(String email, String password) throws InvalidEmailException {
        EmailValidator.validate(email);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(password), "Invalid password");
    }
}
