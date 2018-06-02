package se.tink.backend.main.auth.exceptions;

import se.tink.backend.core.Market;
import se.tink.libraries.auth.AuthenticationMethod;

public class IllegalAuthenticationMethodException extends IllegalArgumentException {
    public IllegalAuthenticationMethodException(String type, AuthenticationMethod method, Market.Code code) {
        super(String.format("%s with method %s does not allow for market %s.", type, method, code));
    }
}
