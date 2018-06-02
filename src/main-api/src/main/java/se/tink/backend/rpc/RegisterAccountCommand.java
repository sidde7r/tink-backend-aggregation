package se.tink.backend.rpc;

import com.google.common.base.Strings;
import java.util.Locale;
import java.util.Optional;
import se.tink.libraries.validation.exceptions.InvalidEmailException;
import se.tink.libraries.validation.exceptions.InvalidLocaleException;
import se.tink.libraries.validation.validators.EmailValidator;
import se.tink.libraries.validation.validators.LocaleValidator;

public class RegisterAccountCommand {
    private String authenticationToken;
    private String email;
    private Locale locale;

    public RegisterAccountCommand(String authenticationToken, String email, String locale)
            throws InvalidEmailException, InvalidLocaleException {
        LocaleValidator.validate(locale);

        // Email is optional. Validate format if user has supplied an email.
        if (!Strings.isNullOrEmpty(email)) {
            final String lowerCaseEmail = email.toLowerCase();
            EmailValidator.validate(lowerCaseEmail);
            this.email = lowerCaseEmail;
        }

        this.authenticationToken = authenticationToken;
        this.locale = createLocale(locale);
    }

    public String getAuthenticationToken() {
        return authenticationToken;
    }

    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }

    public Locale getLocale() {
        return locale;
    }

    private Locale createLocale(String localeString) {
        return new Locale(localeString.substring(0, 2), localeString.substring(3, 5));
    }
}
